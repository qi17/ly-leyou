package com.leyou.item.web;

import com.leyou.common.vo.PageResult;
import com.leyou.item.service.BrandService;
import com.leyou.pojo.Brand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("brand")
public class BrandController {

    @Autowired
    private BrandService brandService;



    @GetMapping("page")
    public ResponseEntity<PageResult<Brand>> queryBrandByPage(
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "rows", defaultValue = "5") Integer rows,
        @RequestParam(value = "sortBy",required = false) String sortBy,
        @RequestParam(value = "desc", defaultValue = "false") Boolean desc,
        @RequestParam(value = "key", required = false) String key
        ){
        PageResult<Brand> result =  brandService.queryBrandByPage(page,rows,sortBy,desc,key);
        return ResponseEntity.ok(result);
    }

    /**
     * 新增品牌
     * @param brand
     * @param cid
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> saveBrand(Brand brand,
                                          @RequestParam("cids") List<Long> cid){
        brandService.saveBrand(brand,cid);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 新增商品
     */
    @GetMapping("cid/{cid}")
    public ResponseEntity<List<Brand>> queryBrandByCid(@PathVariable("cid")Long cid){
        return ResponseEntity.ok(brandService.queryBrandByCid(cid));
    }

    /**
     * 根据id查找品牌
     *
     */
    @GetMapping("{id}")
    public ResponseEntity<Brand> queryBrandById(@PathVariable("id")Long id){
        return  ResponseEntity.ok(brandService.queryById(id));
    }


    @GetMapping("list")
    public ResponseEntity<List<Brand>> queryBrandByIds(@RequestParam("ids") List<Long> ids){
        return ResponseEntity.ok(brandService.queryByIds(ids));
    }
}
