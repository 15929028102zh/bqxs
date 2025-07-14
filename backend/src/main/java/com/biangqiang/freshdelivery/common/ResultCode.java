package com.biangqiang.freshdelivery.common;



/**
 * 响应结果码枚举
 *
 * @author biangqiang
 * @since 2024-01-01
 */
public enum ResultCode {

    // 通用结果码
    SUCCESS(200, "操作成功"),
    ERROR(500, "系统异常"),
    PARAM_ERROR(400, "参数错误"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    
    // 认证相关
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    TOKEN_INVALID(4001, "Token无效"),
    TOKEN_EXPIRED(4002, "Token已过期"),
    LOGIN_REQUIRED(4003, "请先登录"),
    
    // 用户相关
    USER_NOT_FOUND(5001, "用户不存在"),
    USER_DISABLED(5002, "用户已被禁用"),
    PHONE_ALREADY_BOUND(5003, "手机号已绑定"),
    VERIFICATION_CODE_ERROR(5004, "验证码错误"),
    VERIFICATION_CODE_EXPIRED(5005, "验证码已过期"),
    WECHAT_LOGIN_ERROR(5006, "微信登录失败"),
    
    // 商品相关
    PRODUCT_NOT_FOUND(6001, "商品不存在"),
    PRODUCT_OFFLINE(6002, "商品已下架"),
    PRODUCT_STOCK_INSUFFICIENT(6003, "商品库存不足"),
    CATEGORY_NOT_FOUND(6004, "商品分类不存在"),
    
    // 订单相关
    ORDER_NOT_FOUND(7001, "订单不存在"),
    ORDER_STATUS_ERROR(7002, "订单状态错误"),
    ORDER_CANNOT_CANCEL(7003, "订单无法取消"),
    ORDER_ALREADY_PAID(7004, "订单已支付"),
    ORDER_PAYMENT_FAILED(7005, "订单支付失败"),
    
    // 地址相关
    ADDRESS_NOT_FOUND(8001, "收货地址不存在"),
    ADDRESS_LIMIT_EXCEEDED(8002, "收货地址数量超限"),
    
    // 购物车相关
    CART_ITEM_NOT_FOUND(9001, "购物车商品不存在"),
    CART_EMPTY(9002, "购物车为空"),
    
    // 文件上传相关
    FILE_UPLOAD_ERROR(10001, "文件上传失败"),
    FILE_TYPE_NOT_SUPPORTED(10002, "文件类型不支持"),
    FILE_SIZE_EXCEEDED(10003, "文件大小超限");

    /**
     * 响应码
     */
    private final Integer code;

    /**
     * 响应消息
     */
    private final String message;
    
    // 手动添加构造器
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    // 手动添加getter方法
    public Integer getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}