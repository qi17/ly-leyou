package com.leyou.search.mq;

import com.leyou.search.service.SearchService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ItemListener {

    @Autowired
    private SearchService searchService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.item.insert.queue",durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange",type = "topic"),
            key = {"item.insert","item.update"}
    ))
    public void listenerUpdateAndInsert(Long spuId){
        if(spuId == null){
            return;
        }
//        对接受的消息进行业务处理
        searchService.createOrUpdateIndex(spuId);
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.item.delete.queue",durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange",type = ExchangeTypes.TOPIC),
            key = {"item.delete"}
    ))
    public void listenerDelete(Long spuId){
        if(spuId == null){
            return;
        }
//        对接受的消息进行业务处理
        searchService.deleteIndex(spuId);
    }
}
