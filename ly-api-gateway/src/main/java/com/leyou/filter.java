package com.leyou;

import com.leyou.auth.etities.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.config.FilterProperties;
import com.leyou.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class filter extends ZuulFilter {

    @Autowired
    private JwtProperties prop;

    @Autowired
    private FilterProperties filterProperties;


    @Override
    public String filterType() { //过滤器类型：前置过滤器
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() { //过滤器执行的顺序
        return FilterConstants.PRE_DECORATION_FILTER_ORDER -1;
    }

    @Override
    public boolean shouldFilter() {  //是否过滤
        //        获取上下文
        RequestContext context = RequestContext.getCurrentContext();
//        获取request
        HttpServletRequest request = context.getRequest();
//        获取uri
        String uriPath = request.getRequestURI();
        boolean allowPath = isAllowPath(uriPath);
        //匹配则不拦截 过滤应为false
        return !allowPath ;

    }

    private boolean isAllowPath(String path) {
        List<String> allowPaths = filterProperties.getAllowPaths();
        for (String allowPath : allowPaths) {
            //如果请求中的uri和白名单的uri匹配
            if(path.startsWith(allowPath)){
                return true;
            }
        }
        return false;
    }

    @Override
    public Object run() throws ZuulException { //过滤的业务逻辑
//        获取上下文
        RequestContext context = RequestContext.getCurrentContext();
//        获取request
        HttpServletRequest request = context.getRequest();
//        获取cookie的taken
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());


//        解析token校验
        try {
            UserInfo userInfo = JwtUtils.getUserInfo(prop.getPublicKey(), token);
        //TODO 权限校验 --根据获取到的username进行权限的过滤
        }catch (Exception e){
//           解析token失败
            context.setSendZuulResponse(false);
            context.setResponseStatusCode(403);
        }
        return null;
    }
}
