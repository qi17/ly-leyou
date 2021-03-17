package com.leyou.search.feign;

import com.leyou.API.BrandApi;
import com.leyou.pojo.Brand;
import org.springframework.cloud.openfeign.FeignClient;

import java.util.List;

@FeignClient(value = "item-service")
public interface BrandClient extends BrandApi {

}
