package com.leyou.auth.service;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.etities.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.lyException;
import com.leyou.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {
    @Autowired
    private JwtProperties prop;
    @Autowired
    private UserClient userClient;

    public String login(String username, String password) {
        try {
            //        1.校验用户名---远程调用user服务
            User user = userClient.queryUserByUsernameAndPassword(username, password);
            //        判断用户是否存在
            if (user == null) {
                throw new lyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
            }
//            生成token
            String token = JwtUtils.generateToken(new UserInfo(user.getId(), user.getUsername()),prop.getPrivateKey(), prop.getExpire());
            return  token;
        } catch (lyException e) {
            log.info("【授权中心】 生成token失败，用户名称:{}:",username,e.getMessage());
            throw new lyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
    }
}
