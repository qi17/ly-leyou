package com.leyou.order.interceptor;

import com.leyou.auth.etities.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.order.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Slf4j
public class UserInterceptor implements HandlerInterceptor {

    private JwtProperties prop;

    private  static  final ThreadLocal<UserInfo> tl =new ThreadLocal<>();

    public UserInterceptor(JwtProperties prop) {
        this.prop =prop;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        获取token
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());
        try {
//        校验token
            UserInfo userInfo = JwtUtils.getUserInfo(prop.getPublicKey(), token);
//          将获取到的对象放在当前线程中
            tl.set(userInfo);
            return true;
        }catch (Exception e){
            log.error("[购物车服务异常] 解析用户身份失败："+e);
            return  false;
        }
    }

    @Override//该方法是在视图解析完之后执行
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        最后用完数据一定要清空
        tl.remove();
    }

    public static UserInfo get(){
         return tl.get();
    }

}
