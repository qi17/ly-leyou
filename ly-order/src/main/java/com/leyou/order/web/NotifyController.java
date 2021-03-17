package com.leyou.order.web;

import com.leyou.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.ResultMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("notify")
public class NotifyController {
    @Autowired
    private OrderService orderService;

//    需要返回xml格式
    @PostMapping(value = "pay",produces = "application/xml")
    public Map<String, String> hello(@RequestBody Map<String,String> result){
        orderService.handleNotify(result);
        Map<String,String> msg = new HashMap<>();
        log.info("[支付回调] 接受微信支付，结果{}",result );

//        支付后需要返回 支付成功的相关信息
        msg.put("return_code","SUCCESS");
        msg.put("return_msg","OK");
        return  msg;
    }
}
