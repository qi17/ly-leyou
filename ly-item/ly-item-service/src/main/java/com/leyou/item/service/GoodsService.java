package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.lyException;
import com.leyou.common.vo.PageResult;
import com.leyou.dto.CartDto;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.pojo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private AmqpTemplate template;


    /**
     * 分页查询spu
     * @param page
     * @param rows
     * @param key
     * @param saleable
     * @return
     */
    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, String key, Boolean saleable) {
//        1.分页
        PageHelper.startPage(page,rows);
//        2.过滤
        Example example = new Example(Spu.class);
        String desc = "%"+key+"%";
        Example.Criteria criteria = example.createCriteria();
        if(StringUtils.isNotBlank(key)){
            criteria.andLike("title", desc);
        }
        if(saleable != null){
            criteria.andEqualTo("saleable",saleable);
        }
//        3.默认排序
        example.setOrderByClause("last_update_time  DESC");

//        4.查找
        List<Spu> list = spuMapper.selectByExample(example);

        //        5.处理品牌和分类名称
        loadCategoryAndBrandName(list);

        if(CollectionUtils.isEmpty(list)){
            throw new lyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
//        将分页的结果返回
        PageInfo<Spu> info = new PageInfo<>(list);

        return new PageResult<>(info.getTotal(),list);
    }

    /**
     * 加载种类和品牌名
     * @param list
     */
    private void loadCategoryAndBrandName(List<Spu> list) {
        for (Spu spu : list) {
//            1.处理品牌名称
             spu.setBname(brandService.queryById(spu.getBrandId()).getName());
//            2.处理分类名称
            List<String> names = categoryService.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream().map(Category::getName).collect(Collectors.toList());
//            拼接字符串
            spu.setCname(StringUtils.join(names,"/"));
        }
    }


    /**
     * 新增商品
     * @param spu
     * @return
     */
    @Transactional
    public void saveGoods(Spu spu) {
//        新增spu
//        spu.setId(null);
        spu.setValid(false);
        spu.setSaleable(true);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        int count = spuMapper.insert(spu);
        if(count != 1){
            throw new lyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
//        新增spu_Detail
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        spuDetailMapper.insert(spuDetail);
//        新增sku和stock
        saveSkuAndStock(spu);
//        发送消息到mq
        template.convertAndSend("item.insert",spu.getId());

    }

    private void saveSkuAndStock(Spu spu) {
            int count;
            List<Sku> skus = spu.getSkus();
             List<Stock> stockList = new ArrayList<>();

             for (Sku sku : skus) {
    //            新增sku
                sku.setCreateTime(new Date());
                sku.setLastUpdateTime(sku.getCreateTime());
                sku.setSpuId(spu.getId());
                count = skuMapper.insert(sku);
                 if(count!=1) {
                     throw new lyException(ExceptionEnum.GOODS_SAVE_ERROR);
                 }
                Stock stock = new Stock();
                 stock.setStock(sku.getStock());
                 stock.setSkuId(sku.getId());
                 stockList.add(stock);
        }
                //批量新增库存
                count = stockMapper.insertList(stockList);
                if(count!=stockList.size())
                    throw new lyException(ExceptionEnum.GOODS_SAVE_ERROR);
    }

    /**
     * 根据spu的id查询商品详情
     * @param spuId
     * @return
     */
    public SpuDetail querySpuDetailById(Long spuId) {

        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(spuId);
        if(spuDetail == null){
            throw new lyException(ExceptionEnum.GOODS_DETAIL_NOT_FOUND);
        }
        return spuDetail;
    }

    /**
     * 根据spu的id查询sku
     * @param spuId
     * @return
     */
    public List<Sku> querySkuListBySid(Long spuId) {
        Sku sku = new Sku();
        sku.setSpuId(spuId);
//        1.查询sku
        List<Sku> skus = skuMapper.select(sku);
        if(CollectionUtils.isEmpty(skus)){
            throw new lyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }

//        2.查询库存stock
//        for (Sku skuList : skus) {
//            Stock stock = stockMapper.selectByPrimaryKey(skuList.getId());
//           if(stock == null){
//               throw new lyException(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
//           }
//           skuList.setStock(stock.getStock());
//        }
//        我们尝试更高级的写法，嘻嘻~
//        1.将sku直接转换为流，然后进行后续操作---》这一步主要操作i就是把sku的id转换为集合
        List<Long> ids = skus.stream().map(Sku::getId).collect(Collectors.toList());
//        2.根据sku的id集合进行查询，返回一个对应的库存集合
        List<Stock> stocks = stockMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(stocks)){
            throw new lyException(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
        }
//        3.为了更好的存储我们的stocks便于复制，因此我们决定将stocks变为map，使用k-v结构进行赋值
        Map<Long, Integer> stockMap = stocks.stream()
                                        .collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        skus.forEach(s->s.setStock(stockMap.get(s.getId())));
        return  skus;
    }

    /**
     * 更新商品
     * @param spu
     */
    public void updateGoods(Spu spu) {
        if(spu.getId() == null)
            throw new lyException(ExceptionEnum.GOODS_ID_CANNOT_BE_NULL);
        Sku sku = new Sku();
//        根据前端穿过来的spu对象中的id查找sku
        sku.setSpuId(spu.getId());
        //查询sku
        List<Sku> skuList = skuMapper.select(sku);
        if(!CollectionUtils.isEmpty(skuList)){
            //删除sku
            skuMapper.delete(sku);
            //删除库存
            List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
            stockMapper.deleteByIdList(ids);
        }
        //修改spu
        spu.setValid(null);
        spu.setSaleable(null);
        spu.setCreateTime(null);
        spu.setLastUpdateTime(new Date());

        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if(count!=1) {
            throw new lyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        //修改detail
        count = spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if(count != 1)
            throw new lyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        //新增sku和库存stock（单独提取成一个方法了，上边已经写过了）
        saveSkuAndStock(spu);

        //发送mq消息（以后会用到）
        template.convertAndSend("item.update",spu.getId());

    }

    public Spu querySpuById(Long id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(spu == null){
            throw new lyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
//        查询sku
        List<Sku> skus = querySkuListBySid(id);
        spu.setSkus(skus);

//       查询 spuDetail
        SpuDetail spuDetail = querySpuDetailById(id);
        spu.setSpuDetail(spuDetail);
        return spu;
    }

    public List<Sku> querySkusByIds(List<Long> ids) {
        List<Sku> skus = skuMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(skus)) {
            throw new lyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //填充库存
        fillStock(ids, skus);
        return skus;
    }
    private void fillStock(List<Long> ids, List<Sku> skus) {
        //批量查询库存
        List<Stock> stocks = stockMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(stocks)) {
            throw new lyException(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
        }
        //首先将库存转换为map，key为sku的ID
        Map<Long, Integer> map = stocks.stream().collect(Collectors.toMap(s -> s.getSkuId(), s -> s.getStock()));

        //遍历skus，并填充库存
        for (Sku sku : skus) {
            sku.setStock(map.get(sku.getId()));
        }
    }

    @Transactional
    public void decreaseStock(List<CartDto> cartDTOS) {
        for (CartDto cartDTO : cartDTOS) {
            int count = stockMapper.decreaseStock(cartDTO.getSkuId(),cartDTO.getNum());
            if (count != 1) {
                throw new lyException(ExceptionEnum.STOCK_NOT_ENOUGH);
            }
        }

    }
}

