package com.leyou.API;

import com.leyou.pojo.SpecGroup;
import com.leyou.pojo.SpecParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface SpecificationApi {

    @GetMapping("spec/groups/{cid}")
    List<SpecGroup>querySpecificationByCategoryId(@PathVariable("cid")Long cid);

    @GetMapping("spec/params")
    List<SpecParam> querySpecParams(
            @RequestParam(value = "gid",required = false)Long gid,
            @RequestParam(value = "cid",required = false)Long cid,
            @RequestParam(value = "searching",required = false)Boolean searching);

    @GetMapping("spec/group")
    List<SpecGroup> queryGroupByCid(@RequestParam("cid")Long cid);
}
