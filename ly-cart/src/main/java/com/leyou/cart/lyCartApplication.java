package com.leyou.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class lyCartApplication {
    public static void main(String[] args) {
        SpringApplication.run(lyCartApplication.class,args);
    }
}
