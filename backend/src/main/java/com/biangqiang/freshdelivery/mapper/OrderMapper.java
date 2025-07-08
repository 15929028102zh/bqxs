package com.biangqiang.freshdelivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.biangqiang.freshdelivery.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单Mapper接口
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

}