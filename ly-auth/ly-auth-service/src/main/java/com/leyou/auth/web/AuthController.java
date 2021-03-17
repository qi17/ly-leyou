package com.leyou.auth.web;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.etities.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.lyException;
import com.leyou.common.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtProperties prop;

    /**
     * 登陆时生成token
     * @param username
     * @param password
     * @param response
     * @param request
     * @return
     */
    @PostMapping("login")
    public ResponseEntity<String> login(
            @RequestParam("username")String username,
            @RequestParam("password")String password,
            HttpServletResponse response,
            HttpServletRequest request){

//        获取token
        String token = authService.login(username, password);
//        将信息保存到cookies
        CookieUtils.newBuilder(response).httpOnly().request(request).build(prop.getCookieName(),token);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 校验用户登录状态
     * @return
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(
            @CookieValue("LY_TOKEN")String token,
            HttpServletResponse response,
            HttpServletRequest request) {
        try {
//           2.解析token
            UserInfo userInfo = JwtUtils.getUserInfo(prop.getPublicKey(), token);
//          3.刷新cookies
            String newToken = JwtUtils.generateToken(userInfo, prop.getPrivateKey(), prop.getExpire());
//        4.写入cookie
            CookieUtils.newBuilder(response).httpOnly().request(request).build(prop.getCookieName(), newToken);
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
          throw new lyException(ExceptionEnum.UNAUTHORIZED);
        }
    }

}
