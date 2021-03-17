package com.leyou.API;

import com.leyou.common.vo.PageResult;
import com.leyou.dto.CartDto;
import com.leyou.pojo.Sku;
import com.leyou.pojo.Spu;
import com.leyou.pojo.SpuDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GoodsApi {
        /**
         * 根据spu的id查询spu
         * @param id
         * @return
         */
        @GetMapping("/spu/{id}")
        Spu querySpuById(@PathVariable("id") Long id);

        @GetMapping("spu/page")
        PageResult<Spu>  querySpuByPage(
                @RequestParam(value = "page",defaultValue = "1") Integer page,
                @RequestParam(value = "rows",defaultValue = "5") Integer rows,
                @RequestParam(value = "key",required = false ) String key,
                @RequestParam(value = "saleable",required = false ) Boolean saleable
        );

        @GetMapping("/spu/detail/{id}")
        SpuDetail querySpuDetailById(@PathVariable("id") Long id);

        @GetMapping("sku/list")
        List<Sku> querySkuBySpuId(@RequestParam("id") Long id);

        /**
         * 根据sku ids查询sku
         * @param ids
         * @return
         */
        @GetMapping("sku/list/ids")
        List<Sku> querySkusByIds(@RequestParam("ids") List<Long> ids);

        /**
         * 减库存
         * @param cartDTOS
         */
        @PostMapping("stock/decrease")
        void decreaseStock(@RequestBody List<CartDto> cartDTOS);

}
