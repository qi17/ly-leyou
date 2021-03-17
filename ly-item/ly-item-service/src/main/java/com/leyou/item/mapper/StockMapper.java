package com.leyou.item.mapper;

import com.leyou.common.advice.BaseMapper;
import com.leyou.pojo.Stock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;


//这里做一下说明在通用mapper里，我们可以进行批量操作
//tk.mybatis.mapper.common.special.InsertListMapper这里的批量插入方法限制了插入主键必须为id字段
//所以我们需要选择additional下的mapper，才可以插入
public interface StockMapper  extends BaseMapper<Stock> {

    @Update("update tb_stock set stock = stock - #{num} where sku_id = #{skuId} and stock >= #{num}")
    int decreaseStock(@Param("skuId") Long skuId, @Param("num") Integer num);
}
