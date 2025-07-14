package com.biangqiang.freshdelivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.biangqiang.freshdelivery.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单项Mapper接口
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * 根据订单ID查询订单项列表
     *
     * @param orderId 订单ID
     * @return 订单项列表
     */
    @Select("SELECT * FROM tb_order_item WHERE order_id = #{orderId} AND deleted = 0")
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据订单ID删除订单项
     *
     * @param orderId 订单ID
     * @return 影响行数
     */
    int deleteByOrderId(@Param("orderId") Long orderId);
    
    /**
     * 查询商品的销售统计数据
     * 只统计已付款且未取消的订单（状态：1-待发货，2-待收货，3-已完成）
     *
     * @param productId 商品ID
     * @return 包含销量、销售额、订单数的Map
     */
    @Select("SELECT " +
            "COALESCE(SUM(oi.quantity), 0) as total_quantity, " +
            "COALESCE(SUM(oi.total_price), 0) as total_revenue, " +
            "COALESCE(COUNT(DISTINCT oi.order_id), 0) as order_count " +
            "FROM tb_order_item oi " +
            "JOIN tb_order o ON oi.order_id = o.id " +
            "WHERE oi.product_id = #{productId} " +
            "AND o.status IN (1,2,3) " +
            "AND o.deleted = 0 " +
            "AND oi.deleted = 0")
    java.util.Map<String, Object> getProductSalesStats(@Param("productId") Long productId);
}