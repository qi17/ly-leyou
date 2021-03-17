package com.leyou.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class PayConfiguration {
    @Bean
    public RestTemplate template(){
        return  new RestTemplate(new OkHttp3ClientHttpRequestFactory());
    }

    @Bean
    @ConfigurationProperties("ly.pay")
    public PayConfig payConfig(){
        return  new PayConfig();
    }
}
