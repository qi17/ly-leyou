package com.leyou.search;

import com.leyou.SearchApplication;
import com.leyou.common.vo.PageResult;
import com.leyou.pojo.Spu;
import com.leyou.search.feign.GoodClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.SearchService;
import com.netflix.discovery.converters.Auto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SearchApplication.class)
public class ElasticsearchTest {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private GoodClient goodsClient;
    @Autowired
    private SearchService searchService;

    @Test
    public void createIndex(){
        // 创建索引
        this.elasticsearchTemplate.createIndex(Goods.class);
        // 配置映射
        this.elasticsearchTemplate.putMapping(Goods.class);
    }

    @Test
    public void insertData(){
        int page = 1;
        int rows = 100;
        int size = 0;
        do {
            // 查询分页数据得到spu对象
            PageResult<Spu> result = goodsClient.querySpuByPage(page, rows, null, true);
            List<Spu> spus = result.getItems();
            // 通过搜索服务，创建Goods集合
            List<Goods> goods = spus.stream().map(searchService::buildGoods).collect(Collectors.toList());
            goodsRepository.saveAll(goods);
            page++;
            size = spus.size();
        } while (size == 100);
    }
}
