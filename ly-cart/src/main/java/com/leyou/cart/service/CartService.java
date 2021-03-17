package com.leyou.cart.service;

import com.leyou.auth.etities.UserInfo;
import com.leyou.cart.entities.Cart;
import com.leyou.cart.interceptor.UserInterceptor;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.lyException;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private  static final String KEY_PREFIX = "cart:id";

    public void addCart(Cart cart) {
        UserInfo userInfo = UserInterceptor.get();
        String key = KEY_PREFIX + userInfo.getId();
        String hashKey = cart.getSkuId().toString();
        Integer num = cart.getNum();
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        if (operations.hasKey(hashKey)) {
            String json = operations.get(hashKey).toString();
            cart = JsonUtils.toBean(json, Cart.class);
            cart.setNum(cart.getNum() + num);

        }
        operations.put(hashKey, JsonUtils.toString(cart));
    }

    public List<Cart> queryCartList() {
        UserInfo userInfo = UserInterceptor.get();
        String key = KEY_PREFIX + userInfo.getId();
        if (!redisTemplate.hasKey(key)) {
            throw new lyException(ExceptionEnum.CART_NOT_FIND);
        }

        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        List<Cart> cartList = operations.values().stream().map(o -> JsonUtils.toBean(o.toString(), Cart.class)).collect(Collectors.toList());
        return cartList;
    }

    public void updateCartNum(Long skuId, Integer num) {
        UserInfo userInfo = UserInterceptor.get();
        String key = KEY_PREFIX + userInfo.getId();
        String hashKey = skuId.toString();
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        if (!operations.hasKey(hashKey)) {
            throw new lyException(ExceptionEnum.CART_NOT_FIND);
        }
        Cart cart = JsonUtils.toBean(operations.get(hashKey).toString(), Cart.class);
        cart.setNum(num);
        operations.put(hashKey, JsonUtils.toString(cart));
    }

    public void deleteCart(Long skuId) {
        UserInfo userInfo = UserInterceptor.get();
        String key = KEY_PREFIX + userInfo.getId();
        redisTemplate.opsForHash().delete(key, skuId.toString());
    }
}



