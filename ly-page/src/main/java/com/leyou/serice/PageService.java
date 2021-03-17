package com.leyou.serice;

import com.leyou.feign.BrandClient;
import com.leyou.feign.CategoryClient;
import com.leyou.feign.GoodClient;
import com.leyou.feign.SpecificationClient;
import com.leyou.pojo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PageService {

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodClient goodsClient;

    @Autowired
    private SpecificationClient specClient;

    @Autowired
    private TemplateEngine template;


    public Map<String,Object> loadModel(Long spuId){
        Map<String, Object> model = new HashMap<>();
//        查询spu
        Spu spu = goodsClient.querySpuById(spuId);
//        查询skus
        List<Sku> skus = spu.getSkus();
//        查询spuDetail
        SpuDetail detail = spu.getSpuDetail();
//        查询brand
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
//        查询商品分类
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
//        查询规格参数
        List<SpecGroup> specs = specClient.queryGroupByCid(spu.getCid3());


        model.put("brand", brand);
        model.put("categories", categories);
        model.put("spu", spu);
        model.put("skus", skus);
        model.put("detail", detail);
        model.put("specs", specs);

        return model;
    }

    /**
     * 页面静态化
     */
    public void createHtml(Long spuId){
//        1.context上下文
        Context context = new Context();
        context.setVariables(loadModel(spuId));
//        2.输入流文件地址
        File dest = new File("D:\\tmp\\createHtml",spuId+".html");
        if(dest.exists()){
            dest.deleteOnExit();
        }
        try {
            PrintWriter writer = new PrintWriter(dest,"UTF-8");
            template.process("item",context,writer);
        }catch (Exception e){
            log.info("[静态页服务异常]"+e.getMessage());
        }
    }

    public void deletePage(Long spuId) {
        File dest = new File("D:\\tmp\\createHtml",spuId+".html");
        if(dest.exists()){
            dest.deleteOnExit();
        }
    }
}
