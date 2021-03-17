package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.lyException;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.mapper.SpecificationMapper;
import com.leyou.pojo.SpecGroup;
import com.leyou.pojo.SpecParam;
import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpecificationService {

    @Autowired
    private SpecificationMapper specificationMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    /**
     * 根据cid查询规格组
     * @param cid
     * @return
     */
    public List<SpecGroup> queryGroupById(Long cid) {
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        List<SpecGroup> list = specificationMapper.select(specGroup);
        if(CollectionUtils.isEmpty(list)){
            throw  new lyException(ExceptionEnum.SPEC_GROUP_NOT_FIND);
        }
        return list;
    }

    public List<SpecParam> querySpecParams(Long gid,Long cid,Boolean searching) {
        SpecParam param = new SpecParam();
        param.setGroupId(gid);
        param.setCid(cid);
        param.setSearching(searching);
        List<SpecParam> list = specParamMapper.select(param);
        if(CollectionUtils.isEmpty(list)){
            throw new lyException(ExceptionEnum.SPEC_PARAM_NOT_FOUND);
        }
        return list;
    }

    public List<SpecGroup> queryListByCid(Long cid) {
//        根据当前种类查询出规则组（组id，组name）
        List<SpecGroup> groups = queryGroupById(cid);
//      根据组id查询规则组对应的（主体，品牌，参数，....）
        List<SpecParam> params = querySpecParams(null, cid, null);
        Map<Long, List<SpecParam>> map = new HashMap<>();
//        将规格参数变成map，key为gid，值为组下的所有参数
        for (SpecParam param : params) {
            if(!map.containsKey(param.getGroupId())){
                map.put(param.getGroupId(),new ArrayList<>());
            }
            map.get(param.getGroupId()).add(param);
        }
//    将规格参数放入组内
        for (SpecGroup group : groups) {
            group.setParams(map.get(group.getId()));
        }

        return groups;
    }
}


