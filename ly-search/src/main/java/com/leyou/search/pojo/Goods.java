package com.leyou.search.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 为什么我们要定义这样一个实体类？因为我们这是一个搜索的微服务，通过elastic进行实时的全局搜索
 * 那实体类的属性有何意义？ 通过参考京东的页面可以发现，当我们在搜索框输入相关的字段时，
 * 1.比如“手机” 会显示所有手机的相关信息，那么这里我们可以直观的看到手机的相关信息：价格，颜色，标题，副标题以及每个spu对应的多个sku的相关消息，
 * 还有隐含的spu，sku的id等
 * 2.因此 我们根据spu为注意设计了该类，并对其属性进行了拓展
 * 3.当然我们可以省略其中的一部分属性，通过我们的id进行再次查询。但是由于这样是两次的异步查询，可以会出现不友好的页面，所以还是先这样定义吧~
 *
 */
@Data
@Document(indexName = "goods", type = "docs", shards = 1, replicas = 0)
public class Goods {
    @Id
    private Long id; // spuId

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String all; // 所有需要被搜索的信息，包含标题，分类，甚至品牌，允许分词查询

    @Field(type = FieldType.Keyword, index = false)
    private String subTitle;// 卖点

    private Long brandId;// 品牌id

    private Long cid1;// 1级分类id
    private Long cid2;// 2级分类id
    private Long cid3;// 3级分类id

    private Date createTime;// 创建时间

    private List<Long> price;// 价格

    @Field(type = FieldType.Keyword, index = false)
    private String skus;// sku信息的json结构

    private Map<String, Object> specs;// 可搜索的规格参数，key是参数名，值是参数值

//   - all：用来进行全文检索的字段，里面包含标题、商品分类信息
//
//- price：价格数组，是所有sku的价格集合。方便根据价格进行筛选过滤
//
//- skus：用于页面展示的sku信息，不索引，不搜索。包含skuId、image、price、title字段
//
//- specs：所有规格参数的集合。key是参数名，值是参数值。
//
//    例如：我们在specs中存储 内存：4G,6G，颜色为红色，转为json就是：
//    {
//    "specs":{
//        "内存":[4G,6G],
//        "颜色":"红色"
//    }
//}
}
