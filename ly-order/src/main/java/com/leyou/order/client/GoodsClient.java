package com.leyou.order.client;

import com.leyou.API.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("item-service")
public interface GoodsClient  extends GoodsApi {
}
