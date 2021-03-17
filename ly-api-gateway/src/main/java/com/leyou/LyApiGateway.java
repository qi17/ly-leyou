package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.ComponentScan;

@SpringCloudApplication
@EnableZuulProxy
@ComponentScan(value = "com.leyou.config")
public class LyApiGateway {
    public static void main(String[] args) {
        SpringApplication.run(LyApiGateway.class,args);
    }
}
