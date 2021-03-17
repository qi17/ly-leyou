package com.leyou.cart.web;

import com.leyou.cart.entities.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CartController {
    @Autowired
    private CartService service;

    /**
     * 新增购物车
     * @param cart
     * @return
     */
    @PostMapping()
    public ResponseEntity<Void> addCart(@RequestBody Cart cart){
        service.addCart(cart);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 获取购物车列表
     *
     * @return
     */
    @GetMapping
    public ResponseEntity<List<Cart>> getCartList() {
        return ResponseEntity.ok(service.queryCartList());
    }

    /**
     * 修改购物车商品数量
     */
    @PutMapping
    public  ResponseEntity<Void> updateCartNum(@RequestParam("skuId")Long skuId,@RequestParam("num")Integer num){
        service.updateCartNum(skuId,num);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 删除购物车商品
     */
    @DeleteMapping("{skuId}")
    public ResponseEntity<Void> deleteCart(@PathVariable("skuId") Long skuId) {
        service.deleteCart(skuId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
