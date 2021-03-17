package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.lyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.pojo.Brand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;

    public PageResult<Brand> queryBrandByPage(Integer page,Integer rows, String sortBy,Boolean desc,String key){

//       分页
        PageHelper.startPage(page,rows);
//        过滤
        Example example = new Example(Brand.class);
        if(!StringUtils.isEmpty(key)){
//            如果传入的条件key是一个name那么就按name的like查找，如果是首字母，按首字母的条件来过滤
            example.createCriteria().orLike("name","%"+key+"%").orEqualTo("letter",key.toUpperCase());
        }
//        排序
        if(!StringUtils.isEmpty(sortBy)){
          String  orderByClause =sortBy + (desc?" DESC":" ASC");
          example.setOrderByClause(orderByClause);
        }
//        查询
        List<Brand> brands = brandMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(brands)){
            throw  new lyException(ExceptionEnum.BRAND_NOT_FIND);
        }
        PageInfo<Brand> info = new PageInfo<>(brands);
        return new PageResult<Brand>(info.getTotal(),brands);

    }

    @Transactional
    public void saveBrand(Brand brand, List<Long> cid) {
//        新增品牌表
        brand.setId(null);
        int count = brandMapper.insert(brand);
        if(count != 1 ){
            throw new lyException(ExceptionEnum.BRAND_NOT_FIND);
        }
//        新增中间表
        for (Long o : cid) {
            count = brandMapper.insertCategoryBrand(o,brand.getId());
            if(count != 1){
                throw new lyException(ExceptionEnum.BRAND_NOT_FIND);
            }
        }
    }


    public List<Brand> queryBrandByCid(Long cid) {
//        因为我们需要根据传过来的cid进行查询，返回一个品牌对象。所以我们需要通过cid来查对应的brand_id
//        在查出与brand_id对应的brand_name，这就涉及到了多表的联合查询，所以我们需要自定义sql
        List<Brand> list = brandMapper.queryBrandByCid(cid);
        if(list == null){
            throw  new lyException(ExceptionEnum.BRAND_NOT_FIND);
        }
        return list;
    }


    public Brand queryById(Long id) {
        Brand brand = brandMapper.selectByPrimaryKey(id);
        if(brand == null){
            throw  new lyException(ExceptionEnum.BRAND_NOT_FIND);
        }
        return brand;
    }

    public List<Brand> queryByIds(List<Long> ids) {
        List<Brand> brands = brandMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(brands)){
            throw  new lyException(ExceptionEnum.BRAND_NOT_FIND);
        }
        return brands;
    }
}
