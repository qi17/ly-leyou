package com.leyou.item.web;

import com.leyou.common.vo.PageResult;
import com.leyou.dto.CartDto;
import com.leyou.item.service.GoodsService;
import com.leyou.pojo.Sku;
import com.leyou.pojo.Spu;
import com.leyou.pojo.SpuDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping("spu/page")
    public ResponseEntity<PageResult<Spu>>  querySpuByPage(
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows,
            @RequestParam(value = "key",required = false ) String key,
            @RequestParam(value = "saleable",required = false ) Boolean saleable
            ){
        return ResponseEntity.ok(goodsService.querySpuByPage(page,rows,key,saleable));
    }

    /**
     * 通过分析我们前端传过来的数据，了可以发现，我需要使用一个对象来接受这个数据，因此我们选择了spu的对象来接受，既然从前端穿过来的是一个json字符串
     * 所以我们需要从请求中获取，因此使用@RequestBody接受
     * 最后通过在service中由spu对象分别获取对应的sku，sou—detail，stock这些数据，实现新增
     * @param spu
     * @return
     */
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody Spu spu){
        goodsService.saveGoods(spu);
      return   ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @GetMapping("spu/detail/{id}")
    public ResponseEntity<SpuDetail> querySpuDetailById(@PathVariable("id")Long spuId){

        return ResponseEntity.ok(goodsService.querySpuDetailById(spuId));
    }

    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkuListBySid(@RequestParam("id")Long spuId){


        return ResponseEntity.ok(goodsService.querySkuListBySid(spuId));
    }

    /**
     * 根据sku ids查询sku
     * @param ids
     * @return
     */
    @GetMapping("sku/list/ids")
    public ResponseEntity<List<Sku>> querySkusByIds(@RequestParam("ids") List<Long> ids) {
        return ResponseEntity.ok(goodsService.querySkusByIds(ids));
    }

    /*
     *商品修改
     */
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody Spu spu){
        goodsService.updateGoods(spu);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 根据id查询spu
     */
    @GetMapping("/spu/{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id") Long id){
        return ResponseEntity.ok(goodsService.querySpuById(id));
    }

    /**
     * 减库存
     * @param cartDTOS
     */
    @PostMapping("stock/decrease")
    public ResponseEntity<Void> decreaseStock(@RequestBody List<CartDto> cartDTOS){
        goodsService.decreaseStock(cartDTOS);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
