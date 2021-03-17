package com.leyou.mq;

import com.leyou.serice.PageService;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ItemListener {
    @Autowired
    private PageService pageService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.item.delete.queue",durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange",type = "topic"),
            key = {"item.insert","item.update"}
    ))
    public void listenerCreateOrUpdate(Long spuId){
        if(spuId == null){
            return;
        }
//        对接受的消息进行业务处理
        pageService.createHtml(spuId);
    }

    /**
     * 删除静态页
     * @param spuId
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.item.delete.queue",durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange",type = "topic"),
            key = {"item.delete"}
    ))
    public void listenerDelete(Long spuId){
        if(spuId == null){
            return;
        }
//        对接受的消息进行业务处理
        pageService.deletePage(spuId);
    }
}
