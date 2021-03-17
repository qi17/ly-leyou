package com.leyou.web;

import com.leyou.serice.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class pageController {
    @Autowired
    private PageService pageService;

    @GetMapping("item/{id}.html")
    public String toItemPage(@PathVariable("id") Long spuId, Model model){
//        准备model数据
        Map<String, Object> attributes = pageService.loadModel(spuId);

//        放入model中
        model.addAllAttributes(attributes);

       return "item";
    }
}
