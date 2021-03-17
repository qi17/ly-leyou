package com.leyou.item.mapper;

import com.leyou.common.advice.BaseMapper;
import com.leyou.pojo.Brand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;


public interface BrandMapper extends BaseMapper<Brand> {

    /**
     * 新增品牌中间表 tb_category_brand
     * @param cid
     * @param bid
     * @return
     */
    @Insert("INSERT INTO tb_category_brand (category_id, brand_id) VALUES (#{cid},#{bid})")
    int  insertCategoryBrand(@Param("cid") Long cid,@Param("bid") Long bid);

    /**
     * 根据cid-->brand_id -->brand_name;
     */
    @Select("select b.* from `tb_brand` b INNER JOIN `tb_category_brand` cb ON b.id = cb.brand_id where cb.category_id=#{cid}")
    List<Brand> queryBrandByCid(@Param("cid") Long cid );
}
