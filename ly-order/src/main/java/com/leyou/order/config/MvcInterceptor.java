package com.leyou.order.config;


import com.leyou.order.interceptor.UserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class MvcInterceptor implements WebMvcConfigurer {
    @Autowired
    private JwtProperties prop;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        向springmvc的拦截器中添加注册自定义的拦截器，并且配置请求路径
//        但是这里有一个问题,我们拦截器需要在spring加载的时候就注入容器中，但是创建对象的时候是晚于注入所以，我们需要改变一下
//        registry.addInterceptor(new UserInterceptor()).addPathPatterns("/**");
        registry.addInterceptor(new UserInterceptor(prop)).addPathPatterns("/**");
    }
}
