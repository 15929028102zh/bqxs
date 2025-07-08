package com.biangqiang.freshdelivery.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.biangqiang.freshdelivery.entity.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 *
 * @author biangqiang
 * @since 2024-01-01
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单
     *
     * @param userId 用户ID
     * @param addressId 收货地址ID
     * @param productsList 商品列表
     * @param deliveryType 配送方式
     * @param paymentMethod 支付方式
     * @param remark 备注
     * @return 订单创建结果
     */
    Map<String, Object> createOrder(Long userId, Long addressId, List<Map<String, Object>> productsList, String deliveryType, String paymentMethod, String remark);

    /**
     * 分页查询订单列表
     *
     * @param page 分页参数
     * @param orderNo 订单号
     * @param userName 用户名
     * @param status 订单状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 订单分页数据
     */
    IPage<Order> getOrderList(Page<Order> page, String orderNo, String userName, Integer status, String startDate, String endDate);

    /**
     * 更新订单状态
     *
     * @param id 订单ID
     * @param status 新状态
     * @return 是否成功
     */
    boolean updateOrderStatus(Long id, Integer status);

    /**
     * 取消订单
     *
     * @param id 订单ID
     * @param reason 取消原因
     * @return 是否成功
     */
    boolean cancelOrder(Long id, String reason);

    /**
     * 订单发货
     *
     * @param id 订单ID
     * @param company 物流公司
     * @param trackingNo 物流单号
     * @param remark 备注
     * @return 是否成功
     */
    boolean shipOrder(Long id, String company, String trackingNo, String remark);

    /**
     * 确认收货
     *
     * @param id 订单ID
     * @return 是否成功
     */
    boolean confirmOrder(Long id);

    /**
     * 获取订单总数
     *
     * @return 订单总数
     */
    long getTotalOrderCount();

    /**
     * 获取待处理订单数
     *
     * @return 待处理订单数
     */
    long getPendingOrderCount();

    /**
     * 获取已完成订单数
     *
     * @return 已完成订单数
     */
    long getCompletedOrderCount();

    /**
     * 获取订单总金额
     *
     * @return 订单总金额
     */
    BigDecimal getTotalOrderAmount();

    /**
     * 获取用户订单列表
     *
     * @param userId 用户ID
     * @param status 订单状态
     * @param page 页码
     * @param size 每页数量
     * @return 用户订单列表
     */
    IPage<Order> getUserOrderList(Long userId, Integer status, Integer page, Integer size);
}