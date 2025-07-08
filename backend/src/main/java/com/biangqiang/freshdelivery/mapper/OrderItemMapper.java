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
}