package com.leyou.search.feign;

import com.leyou.API.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "item-service")
public interface GoodClient  extends GoodsApi {
}
