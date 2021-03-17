package com.leyou.cart.entities;

import lombok.Data;


@Data
public class Cart {

    private Long skuId;
    private String title;
    private String image;
    private Long price;
    private Integer num;
    private String ownSpec;

}
