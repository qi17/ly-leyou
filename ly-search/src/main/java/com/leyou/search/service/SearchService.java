package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.lyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.pojo.*;
import com.leyou.search.feign.BrandClient;
import com.leyou.search.feign.CategoryClient;
import com.leyou.search.feign.GoodClient;
import com.leyou.search.feign.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import com.sun.xml.internal.bind.v2.TODO;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.spatial3d.geom.GeoOutsideDistance;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {
    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private GoodsRepository repository;

    @Autowired
    private ElasticsearchTemplate template;

    /**
     * ??????goods???????????????????????????spu?????????goods
     * ?????????????????????????????????
     * @param spu
     * @return
     */
    public Goods buildGoods(Spu spu){
        Goods goods = new Goods();

//        ????????????
        List<Category> categories = categoryClient.queryCategoryByIds(
                Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        if(categories.isEmpty()){
            throw new lyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        List<String> names = categories.stream().map(Category::getName).collect(Collectors.toList());

//        ????????????
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        if(brand == null){
            throw new lyException(ExceptionEnum.BRAND_NOT_FIND);
        }
//        ????????????
        String all = spu.getTitle() + StringUtils.join(names," ") + brand.getName();

//        ??????sku
        List<Sku> skuList = goodsClient.querySkuBySpuId(spu.getId());

//        ?????????sku???????????????????????????????????????sku???????????????????????????????????????
        List<Long> prices = new ArrayList<>();
        List<Map<String,Object>>  skus = new ArrayList<>();
        for (Sku sku : skuList) {
            Map<String,Object> map = new HashMap<>();
            map.put("id",sku.getId());
            map.put("title",sku.getTitle());
            map.put("price",sku.getPrice());
            map.put("image",StringUtils.substringBefore(sku.getImages(),","));
            skus.add(map);
//            ????????????
            prices.add(sku.getPrice());
        }

//        ??????????????????
        List<SpecParam> specParams = specificationClient.querySpecParams(null, spu.getCid3(), true);

//        ??????????????????
        SpuDetail spuDetail = goodsClient.querySpuDetailById(spu.getId());
//        ????????????????????????
        Map<Long, String> genericSpec = JsonUtils.toMap(spuDetail.getGenericSpec(), Long.class, String.class);
//        ???????????????????????????
        String json = spuDetail.getSpecialSpec();
        Map<Long, List<String>> specialSpec = JsonUtils.nativeRead(json, new TypeReference<Map<Long, List<String>>>() {});

        Map<String, Object> specs = new HashMap<>();

        for (SpecParam param : specParams) {
            String key =param.getName();
            Object value = "";
            if(param.getGeneric()){
                assert genericSpec != null;
                value = genericSpec.get(param.getId());
                if(param.getNumeric()){
//                    ????????????
                    value = chooseSegment(value.toString(),param);
                }
            }else {
                assert specialSpec != null;
                value = specialSpec.get(param.getId());
            }
            specs.put(key,value);
        }
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setId(spu.getId());
       goods.setAll(all);// ??????sku???????????????
       goods.setPrice(prices);//??????sku???????????????
       goods.setSkus(JsonUtils.toString(skus));//??????sku?????????
       goods.setSpecs(specs);//?????????????????????????????????
       goods.setSubTitle(spu.getSubTitle());
        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "??????";
        // ???????????????
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // ??????????????????
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // ????????????????????????
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "??????";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "??????";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    /**
     * ????????????
     * @param request
     * @return
     */
    public PageResult<Goods> search(SearchRequest request) {
        PageResult pageResult = new PageResult();
        String key =request.getKey();
        if(StringUtils.isEmpty(key)){
            return null;
        }
        int page =request.getPage() - 1;
        int size = request.getSize();
//        ?????????????????????
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
//        0.????????????
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[] {"id","subTitle","skus"},null));
//        1.??????
        queryBuilder.withPageable(PageRequest.of(page,size));
//        2.??????
        QueryBuilder basicQuery = buildBasicQuery(request);
        queryBuilder.withQuery(basicQuery);
//        ??????
//        ???????????????
        String brandName = "brand_name";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandName).field("brandId"));

//        ???????????????
        String categoryName = "category_name";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryName).field("cid3"));

//        3?????????:
//        3.1 ????????????????????????????????????????????????????????????????????????????????????????????????
//        Page<Goods> goods = repository.search(queryBuilder.build());
//        3.2 ?????????????????????????????????????????????
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);

//        4.??????
//        ??????????????????
        int totalPages = result.getTotalPages();//?????????
        long totalElements = result.getTotalElements();//????????????
        List<Goods> content = result.getContent();//???????????????
//        ??????????????????
        Aggregations aggs = result.getAggregations();
        List<Category>  categories = parseCategoryAgg(aggs.get(categoryName));
        List<Brand>  brands =  parseBrandAgg(aggs.get(brandName));

//        6.??????????????????
        List<Map<String,Object>> specs = null;
        if(categories != null && categories.size() == 1){
//            ?????????????????????????????????1???????????????????????????
            specs = buildSpecificationAgg(categories.get(0).getId(),basicQuery);
        }
        return  new SearchResult(totalElements,totalPages,content,categories,brands,specs);
    }

    private QueryBuilder buildBasicQuery(SearchRequest request) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
//        match ??????1--??????  ??????2--?????????
        queryBuilder.must(QueryBuilders.matchQuery("all",request.getKey()));
        Map<String,String> map =request.getFilter();
        for(Map.Entry<String,String> entry : map.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            if(!"cid3".equals(key) && !"brandId".equals(key)){
                key = "specs."+key+".keyword";
            }
            queryBuilder.filter(QueryBuilders.termQuery(key,entry.getValue()));
        }
        return queryBuilder;
    }

    private List<Map<String, Object>> buildSpecificationAgg(Long cid, QueryBuilder basicQuery) {

        List<Map<String, Object>> specs = new ArrayList<>();
//        1.?????????????????????????????????
        List<SpecParam> params = specificationClient.querySpecParams(null, cid, true);
//        2.??????
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
//        2.1??????????????????
        queryBuilder.withQuery(basicQuery);
//        2.2 ??????
        for (SpecParam param : params) {
            String name = param.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs."+name+".keyword"));
        }
//        3.????????????
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
//        4.????????????
        Aggregations aggs = result.getAggregations();
        for (SpecParam param : params) {
//            ??????????????????
            String name = param.getName();
            StringTerms terms = aggs.get(name);
            List<String> options = terms.getBuckets()
                    .stream().map(StringTerms.Bucket::getKeyAsString).collect(Collectors.toList());
            Map<String, Object> map = new HashMap<>();
            map.put("k",name);
            map.put("options",options);

            specs.add(map);
        }
        return  specs;
    }

    public List<Brand> parseBrandAgg(LongTerms terms){
        try {
            List<Long> ids = terms.getBuckets()
                    .stream().map(s -> s.getKeyAsNumber().longValue())
                    .collect(Collectors.toList());
            List<Brand> brands = brandClient.queryBrandByIds(ids);
            return brands;
        }catch (Exception e){
            return null;
        }
    }
    public List<Category> parseCategoryAgg(LongTerms terms){
        try {
        List<Long> ids = terms.getBuckets()
                .stream().map(s->s.getKeyAsNumber().longValue())
                .collect(Collectors.toList());
        List<Category> categories = categoryClient.queryCategoryByIds(ids);
        return categories;
        }catch (Exception e){
            return null;
        }
    }

    public void createOrUpdateIndex(Long spuId) {
//        ??????spu
        Spu spu = goodsClient.querySpuById(spuId);
//        ??????spu
        Goods goods = buildGoods(spu);
//        ??????/????????????
        repository.save(goods);
    }

    public void deleteIndex(Long spuId) {
        repository.deleteById(spuId);
    }
}
