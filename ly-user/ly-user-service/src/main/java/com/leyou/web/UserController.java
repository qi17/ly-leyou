package com.leyou.web;

import com.leyou.pojo.User;
import com.leyou.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class UserController {

    @Autowired
    private UserService service;


    /**
     * 注册用户
     * @param data
     * @param type
     * @return
     */
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkData(@PathVariable("data")String data,@PathVariable("type") Integer type){

        return ResponseEntity.ok(service.checkData(data,type));
    }

    /**
     * 验证码
     */
    @PostMapping("/code")
    public ResponseEntity<Void> checkCode(@RequestParam("phone") String phone){

        service.checkCode(phone);
        return ResponseEntity.noContent().build();
    }

    /**
     * 注册功能
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid User user, @RequestParam("code")String code){
        service.register(user,code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 查询用户名和校验密码
     */
    @GetMapping("/query")
    public ResponseEntity<User> queryUserByUsernameAndPassword(
            @RequestParam("username")String username,
            @RequestParam("password")String password){
        return ResponseEntity.ok(service.queryUserByUsernameAndPassword(username,password));
    }
}
