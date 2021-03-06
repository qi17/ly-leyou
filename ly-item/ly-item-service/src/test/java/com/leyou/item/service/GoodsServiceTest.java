package com.leyou.item.service;


import com.leyou.dto.CartDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsServiceTest  {
    @Autowired
    private GoodsService service;

    @Test
    public void testDecreaseStock() {
        List<CartDto> list = Arrays.asList(new CartDto(2600242L, 2), new CartDto(2600248L, 2));
        service.decreaseStock(list);
    }
}