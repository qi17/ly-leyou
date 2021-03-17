package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.lyException;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class CategoryService  {
    @Autowired
    private CategoryMapper categoryMapper;

    public List<Category> queryCategory( Long pid){
        Category category = new Category();
        category.setParentId(pid);
        List<Category> list = this.categoryMapper.select(category);
        if(list.isEmpty()){
            throw new lyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return list;
    }

    public List<Category> queryCategoryByIds(List<Long> ids){

        List<Category> list = categoryMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(list)){
            throw  new lyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return list;
    }
}
