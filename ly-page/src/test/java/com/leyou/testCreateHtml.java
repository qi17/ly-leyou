package com.leyou;

import com.leyou.serice.PageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class testCreateHtml {
    @Autowired
    private PageService pageService;

    @Test
    public void test1(){
        pageService.createHtml(141L);
    }
}
