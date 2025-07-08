package com.biangqiang.freshdelivery.service;

import com.biangqiang.freshdelivery.entity.Order;

/**
 * 现金支付服务接口
 */
public interface CashPaymentService {
    
    /**
     * 处理现金支付记账
     * @param order 订单信息
     * @return 记账结果
     */
    boolean processCashPayment(Order order);
    
    /**
     * 处理货到付款预记账
     * @param order 订单信息
     * @return 记账结果
     */
    boolean processCashOnDelivery(Order order);
    
    /**
     * 确认货到付款收款
     * @param orderNo 订单号
     * @param actualAmount 实际收款金额
     * @return 确认结果
     */
    boolean confirmCashOnDeliveryPayment(String orderNo, Double actualAmount);
    
    /**
     * 查询现金支付记录
     * @param orderNo 订单号
     * @return 支付记录
     */
    Object getCashPaymentRecord(String orderNo);
    
    /**
     * 撤销现金支付记账
     * @param orderNo 订单号
     * @return 撤销结果
     */
    boolean reverseCashPayment(String orderNo);
}