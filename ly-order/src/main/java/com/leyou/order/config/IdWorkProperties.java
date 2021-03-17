package com.leyou.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("ly.worker")
@Data
public class IdWorkProperties{

    private long workerId; //机器id
    private long dataCenterId; //序列号
}
