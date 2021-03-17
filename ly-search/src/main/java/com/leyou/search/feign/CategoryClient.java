package com.leyou.search.feign;

import com.leyou.API.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "item-service")
public interface CategoryClient  extends CategoryApi {
}
