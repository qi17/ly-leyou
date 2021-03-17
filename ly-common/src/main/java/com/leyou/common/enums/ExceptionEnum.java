package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum  ExceptionEnum {

    BRAND_SAVE_ERROR(500,"新增品牌失败"),
    GOODS_SAVE_ERROR(500,"新增商品失败"),
    BRAND_NOT_FIND(404,"品牌不存在"),
    SPEC_GROUP_NOT_FIND(404,"规格组没查到"),
    SPEC_PARAM_NOT_FOUND(404,"规格参数没查到"),
    GOODS_NOT_FOUND(404,"商品没查到"),
    GOODS_DETAIL_NOT_FOUND(404,"商品详情没查到"),
    GOODS_SKU_NOT_FOUND(404,"商品SKU没查到"),
    GOODS_STOCK_NOT_FOUND(404,"商品库存不存在"),
    UPLOAD_FILE_ERROR(404,"文件上传失败"),
    GOODS_UPDATE_ERROR(500,"商品更新失败"),
    GOODS_ID_CANNOT_BE_NULL(400,"商品更新失败"),
    INVALID_USER_TYPE(400,"无效的用户数据类型"),
    INVALID_VERIFY_CODE(400,"无效的验证码"),
    INVALID_USERNAME_PASSWORD(400,"无效的验证码"),
    UNAUTHORIZED(403,"权限不足"),
    CART_NOT_FIND(404,"购物车为空"),
    RECEIVER_ADDRESS_NOT_FOUND(404,"收货地址不存在"),
    STOCK_NOT_ENOUGH(500,"库存不足"),
    WX_PAY_NOTIFY_PARAM_ERROR(405,"微信支付参数有误"),
    WX_PAY_SIGN_INVALID(400,"签名无效"),
    ORDER_NOT_FOUND(404,"订单未找到"),
    ORDER_STATUS_EXCEPTION(405,"订单状态异常"),
    INVALID_ORDER_PARAM(400,"订单参数异常"),
   UPDATE_ORDER_STATUS_ERROR(400,"更新订购单状态失败"),
    CATEGORY_NOT_FOUND(404,"商品分类没查到");

    private int code;
    private String msg;

}
