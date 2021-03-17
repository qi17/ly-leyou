package com.leyou.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "tb_category")
@Data
public class Category {
    @Id
    @KeySql(useGeneratedKeys = true)//使用别名
    private Long id;
    private String name;
    private Long parentId;
    private Boolean isParent;//产品种类是分级的，比如家电-->电视--->液晶电视。。。
    private Integer sort;//排序权重
}
