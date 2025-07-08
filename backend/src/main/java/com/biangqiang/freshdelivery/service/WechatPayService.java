package com.biangqiang.freshdelivery.service;

import com.biangqiang.freshdelivery.entity.Order;

import java.util.Map;

/**
 * 微信支付服务接口
 *
 * @author biangqiang
 * @since 2024-01-01
 */
public interface WechatPayService {

    /**
     * 创建微信支付订单
     *
     * @param order 订单信息
     * @return 支付参数
     */
    Map<String, Object> createPayOrder(Order order);

    /**
     * 处理支付结果通知
     *
     * @param notifyData 通知数据
     * @return 处理结果
     */
    boolean handlePayNotify(String notifyData);

    /**
     * 查询支付状态
     *
     * @param orderNo 订单号
     * @return 支付状态
     */
    String queryPayStatus(String orderNo);
}