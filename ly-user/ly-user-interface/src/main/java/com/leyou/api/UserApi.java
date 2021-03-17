package com.leyou.api;

import com.leyou.pojo.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;


public interface UserApi {

    @GetMapping("/check/{data}/{type}")
    Boolean checkData(@PathVariable("data") String data, @PathVariable("type") Integer type);

    /**
     * 发送验证码
     *
     * @param phone
     * @return
     */
    @PostMapping("send")
    void sendVerifyCode(@RequestParam("phone") String phone);

    /**
     * 用户注册
     *
     * @param user
     * @param code
     * @return
     */
    @PostMapping("register")
    void register(@Valid User user, @RequestParam("code") String code);


    @GetMapping("/query")
    User queryUserByUsernameAndPassword( @RequestParam("username")String username,  @RequestParam("password")String password);
}
