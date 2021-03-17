package com.leyou.order.service;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.auth.etities.UserInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.lyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.dto.CartDto;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.dto.AddressDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayStateEnum;
import com.leyou.order.interceptor.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.utils.PayHelper;
import com.leyou.pojo.Sku;
import com.sun.media.jfxmedia.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private OrderStatusMapper orderStatusMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private PayHelper payHelper;


    public Long createOrder(OrderDTO orderDto) {
//        1.新增订单
        Order order = new Order();
//        1.1生成订单id，使用算法生成
        Long orderId = idWorker.nextId();
        order.setCreateTime(new Date());
        order.setOrderId(orderId);
        order.setPaymentType(orderDto.getPaymentType());
        order.setPostFee(0L);  //// TODO 调用物流信息，根据地址计算邮费
//          1.2获取用户信息并设置
        UserInfo user = UserInterceptor.get();
        order.setUserId(user.getId());
        order.setBuyerNick(user.getName());
        order.setBuyerRate(false);  //卖家为留言

        //收货人地址信息，应该从数据库中物流信息中获取，这里使用的是假的数据
        AddressDTO addressDTO = AddressClient.findById(2L);
        if (addressDTO == null) {
            // 商品不存在，抛出异常
            throw new lyException(ExceptionEnum.RECEIVER_ADDRESS_NOT_FOUND);
        }
        order.setReceiver(addressDTO.getName());
        order.setReceiverAddress(addressDTO.getAddress());
        order.setReceiverCity(addressDTO.getCity());
        order.setReceiverDistrict(addressDTO.getDistrict());
        order.setReceiverMobile(addressDTO.getPhone());
        order.setReceiverZip(addressDTO.getZipCode());
        order.setReceiverState(addressDTO.getState());

        //付款金额相关，首先把orderDto转化成map，其中key为skuId,值为购物车中该sku的购买数量
        Map<Long, Integer> skuNumMap = orderDto.getOrderDetails().stream()
                                     .collect(Collectors.toMap(CartDto::getSkuId, CartDto::getNum));

        //查询商品信息，根据skuIds批量查询sku详情
        List<Sku> skus = goodsClient.querySkusByIds(new ArrayList<>(skuNumMap.keySet()));

        if (CollectionUtils.isEmpty(skus)) {
            throw new lyException(ExceptionEnum.GOODS_NOT_FOUND);
        }

        Double totalPay = 0.0;
        //填充orderDetail
        ArrayList<OrderDetail> orderDetails = new ArrayList<>();

        //遍历skus + 填充orderDetail
        for (Sku sku : skus) {
            Integer num = skuNumMap.get(sku.getId()); //商品数量
            totalPay += num * sku.getPrice();//计算价格累加

            OrderDetail orderDetail = new OrderDetail(); //遍历的同时对每个商品详情进行添加
            orderDetail.setOrderId(orderId);
            orderDetail.setOwnSpec(sku.getOwnSpec());
            orderDetail.setSkuId(sku.getId());
            orderDetail.setTitle(sku.getTitle());
            orderDetail.setNum(num);
            orderDetail.setPrice(sku.getPrice().longValue());
            orderDetail.setImage(StringUtils.substringBefore(sku.getImages(), ","));

            orderDetails.add(orderDetail);
        }

        order.setActualPay((totalPay.longValue() + order.getPostFee()));  //todo 还要减去优惠金额
        order.setTotalPay(totalPay.longValue());

        //保存order
        orderMapper.insertSelective(order);

        //保存detail
        orderDetailMapper.insertList(orderDetails);


        //填充orderStatus
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(OrderStatusEnum.INIT.value());
        orderStatus.setCreateTime(new Date());

        //保存orderStatus
        orderStatusMapper.insertSelective(orderStatus);

        //减库存
        goodsClient.decreaseStock(orderDto.getOrderDetails());


//todo 删除购物车中已经下单的商品数据, 采用异步mq的方式通知购物车系统删除已购买的商品，传送商品ID和用户ID
//        HashMap<String, Object> map = new HashMap<>();
//        try {
//            map.put("skuIds", skuNumMap.keySet());
//            map.put("userId", user.getId());
//            amqpTemplate.convertAndSend("ly.cart.exchange", "cart.delete", JsonUtils.toString(map));
//        } catch (Exception e) {
//            log.error("删除购物车消息发送异常，商品ID：{}", skuNumMap.keySet(), e);
//        }
//
        log.info("生成订单，订单编号：{}，用户id：{}", orderId, user.getId());
        return orderId;

    }

    public Order queryById(Long orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new lyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        List<OrderDetail> orderDetails = orderDetailMapper.select(orderDetail);
        order.setOrderDetails(orderDetails);
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
        order.setOrderStatus(orderStatus);
        return order;
    }

    public String createOrderUrl(Long orderId) {
        Order order = queryById(orderId);
//        判断订单状态
        if (!order.getOrderStatus().getStatus().equals(OrderStatusEnum.INIT.value())) {
            throw new lyException(ExceptionEnum.ORDER_STATUS_EXCEPTION);
        }
        OrderDetail detail = order.getOrderDetails().get(0);
        String desc = detail.getTitle();
        Long totalPay =/* order.getTotalPay()*/ 1L;

        return payHelper.createPayUrl(orderId, desc, totalPay);
    }

    public void handleNotify(Map<String, String> result) {
//        数据校验
        payHelper.isSuccess(result);
//        签名校验
        payHelper.isSignatureValid(result);

//        校验金额
        String total_fee = result.get("total_fee");
        String tradeNo = result.get("out_trade_no");
        if(StringUtils.isEmpty(total_fee)  || StringUtils.isEmpty(tradeNo)){
            throw new lyException(ExceptionEnum.INVALID_ORDER_PARAM);
        }
        Long totalFee = Long.valueOf(total_fee);
        Long orderId = Long.valueOf(tradeNo);
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if(totalFee != /*order.getTotalPay()*/ 1L){
            throw new lyException(ExceptionEnum.INVALID_ORDER_PARAM);
        }

//        修改订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setStatus(OrderStatusEnum.PAY_UP.value());
        orderStatus.setOrderId(orderId);
        orderStatus.setPaymentTime(new Date());
        int count = orderStatusMapper.updateByPrimaryKeySelective(orderStatus);//根据传入的对象

        if(count != 1){
            throw new lyException(ExceptionEnum.UPDATE_ORDER_STATUS_ERROR);
        }
        log.info("订单支付成功，订单编号：{}",orderId);
    }



    public PayStateEnum queryOrderStatus(Long id) {
//    1.查询订单状态
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(id);
        Integer status = orderStatus.getStatus();
//        判断是否已经支付
        if(status != OrderStatusEnum.INIT.value()){
            return PayStateEnum.SUCCESS;
        }
        return payHelper.queryPayState(id);
    }
}
