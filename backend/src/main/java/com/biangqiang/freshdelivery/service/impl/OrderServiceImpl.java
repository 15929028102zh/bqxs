package com.biangqiang.freshdelivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.biangqiang.freshdelivery.entity.Address;
import com.biangqiang.freshdelivery.entity.Order;
import com.biangqiang.freshdelivery.entity.OrderItem;
import com.biangqiang.freshdelivery.mapper.AddressMapper;
import com.biangqiang.freshdelivery.mapper.OrderItemMapper;
import com.biangqiang.freshdelivery.mapper.OrderMapper;
import com.biangqiang.freshdelivery.service.CartService;
import com.biangqiang.freshdelivery.service.OrderService;
import com.biangqiang.freshdelivery.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OrderServiceImpl.class);
    
    private final AddressMapper addressMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartService cartService;
    private final ProductService productService;

    @Override
    @Transactional
    public Map<String, Object> createOrder(Long userId, Long addressId, List<Map<String, Object>> productsList, String deliveryType, String paymentMethod, String remark) {
        try {
            // 1. 验证收货地址
            Address address = addressMapper.selectById(addressId);
            if (address == null || !address.getUserId().equals(userId)) {
                throw new RuntimeException("收货地址不存在或不属于当前用户");
            }

            // 2. 计算订单总金额
            BigDecimal productAmount = BigDecimal.ZERO;
            BigDecimal deliveryFee = "快速配送".equals(deliveryType) ? new BigDecimal("10.00") : BigDecimal.ZERO;
            
            for (Map<String, Object> product : productsList) {
                // 安全处理价格和数量计算，避免空指针异常
                BigDecimal price = new BigDecimal(Optional.ofNullable(product.get("price")).orElse("0").toString());
                Integer quantity = Integer.valueOf(Optional.ofNullable(product.get("quantity")).orElse(0).toString());
                productAmount = productAmount.add(price.multiply(new BigDecimal(quantity)));
            }
            BigDecimal totalAmount = productAmount.add(deliveryFee);

            // 3. 创建订单
            Order order = new Order();
            order.setUserId(userId);
            order.setOrderNo(generateOrderNo());
            order.setProductAmount(productAmount);
            order.setTotalAmount(totalAmount);
            order.setDeliveryFee(deliveryFee);
            order.setStatus(0); // 待支付
            
            // 转换支付方式：微信支付-1，现金支付-2，货到付款-3
            Integer paymentMethodCode = convertPaymentMethod(paymentMethod);
            order.setPayType(paymentMethodCode); // 设置到数据库字段
            order.setPaymentMethod(paymentMethodCode); // 设置前端显示字段
            
            // 转换配送方式：标准配送-1，快速配送-2
            Integer deliveryTypeCode = convertDeliveryType(deliveryType);
            order.setDeliveryType(deliveryTypeCode);
            order.setRemark(remark);
            // 安全处理收货人信息，避免空指针异常
            order.setReceiverName(Optional.ofNullable(address.getReceiverName()).orElse(""));
            order.setReceiverPhone(Optional.ofNullable(address.getReceiverPhone()).orElse(""));
            // 安全处理地址拼接，避免空指针异常
            String fullAddress = Optional.ofNullable(address.getProvince()).orElse("") + 
                               Optional.ofNullable(address.getCity()).orElse("") + 
                               Optional.ofNullable(address.getDistrict()).orElse("") + 
                               Optional.ofNullable(address.getDetailAddress()).orElse("");
            order.setReceiverAddress(fullAddress);
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());

            // 保存订单
            this.save(order);

            // 4. 创建订单项
            List<OrderItem> orderItems = new ArrayList<>();
            for (Map<String, Object> product : productsList) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(order.getId());
                // 安全处理商品信息，避免空指针异常
                orderItem.setProductId(Long.valueOf(Optional.ofNullable(product.get("id")).orElse(0).toString()));
                orderItem.setProductName(Optional.ofNullable(product.get("name")).orElse("").toString());
                orderItem.setProductImage(Optional.ofNullable(product.get("images")).orElse("").toString());
                
                BigDecimal itemPrice = new BigDecimal(Optional.ofNullable(product.get("price")).orElse("0").toString());
                Integer itemQuantity = Integer.valueOf(Optional.ofNullable(product.get("quantity")).orElse(0).toString());
                BigDecimal itemSubtotal = itemPrice.multiply(new BigDecimal(itemQuantity));
                
                orderItem.setPrice(itemPrice);
                orderItem.setQuantity(itemQuantity);
                orderItem.setSubtotal(itemSubtotal);
                orderItem.setCreateTime(LocalDateTime.now());
                orderItems.add(orderItem);
            }

            // 批量保存订单项
            for (OrderItem item : orderItems) {
                orderItemMapper.insert(item);
            }

            // 5. 扣减商品库存
            try {
                List<Map<String, Object>> stockUpdates = new ArrayList<>();
                for (Map<String, Object> product : productsList) {
                    Long productId = Long.valueOf(Optional.ofNullable(product.get("id")).orElse(0).toString());
                    Integer quantity = Integer.valueOf(Optional.ofNullable(product.get("quantity")).orElse(0).toString());
                    
                    if (productId > 0 && quantity > 0) {
                        Map<String, Object> stockUpdate = new HashMap<>();
                        stockUpdate.put("productId", productId);
                        stockUpdate.put("quantity", quantity);
                        stockUpdates.add(stockUpdate);
                    }
                }
                
                if (!stockUpdates.isEmpty()) {
                    boolean stockUpdateSuccess = productService.batchUpdateStock(stockUpdates);
                    if (!stockUpdateSuccess) {
                        throw new RuntimeException("库存扣减失败，订单创建失败");
                    }
                    log.info("订单创建库存扣减成功，订单号：{}, 扣减商品数：{}", order.getOrderNo(), stockUpdates.size());
                }
            } catch (Exception e) {
                log.error("订单创建库存扣减失败，订单号：{}, 错误信息：{}", order.getOrderNo(), e.getMessage());
                throw new RuntimeException("库存扣减失败: " + e.getMessage());
            }

            // 6. 清理购物车中已下单的商品
            try {
                // 提取已下单商品的ID列表
                List<Long> orderedProductIds = new ArrayList<>();
                for (Map<String, Object> product : productsList) {
                    Long productId = Long.valueOf(Optional.ofNullable(product.get("id")).orElse(0).toString());
                    if (productId > 0) {
                        orderedProductIds.add(productId);
                    }
                }
                
                // 只删除已下单的商品，保留其他商品
                if (!orderedProductIds.isEmpty()) {
                    cartService.removeCartItemsByProductIds(userId, orderedProductIds);
                    log.info("订单创建后购物车中已下单商品清理成功，用户ID：{}, 商品ID：{}", userId, orderedProductIds);
                }
            } catch (Exception e) {
                log.warn("订单创建后购物车清理失败，用户ID：{}, 错误信息：{}", userId, e.getMessage());
                // 购物车清理失败不影响订单创建，只记录警告日志
            }

            // 7. 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("orderId", order.getId());
            result.put("orderNo", order.getOrderNo());
            result.put("totalAmount", totalAmount);
            
            log.info("订单创建成功，订单号：{}, 用户ID：{}, 总金额：{}", order.getOrderNo(), userId, totalAmount);
            
            return result;
        } catch (Exception e) {
            log.error("创建订单失败，用户ID：" + userId + ", 错误信息：" + e.getMessage(), e);
            throw new RuntimeException("创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "ORDER" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + System.currentTimeMillis() % 1000;
    }
    
    /**
     * 转换支付方式字符串为数字代码
     */
    private Integer convertPaymentMethod(String paymentMethod) {
        if ("微信支付".equals(paymentMethod)) {
            return 1;
        } else if ("现金支付".equals(paymentMethod)) {
            return 2;
        } else if ("货到付款".equals(paymentMethod)) {
            return 3;
        }
        return 1; // 默认微信支付
    }
    
    /**
     * 转换配送方式字符串为数字代码
     */
    private Integer convertDeliveryType(String deliveryType) {
        if ("标准配送".equals(deliveryType)) {
            return 1;
        } else if ("快速配送".equals(deliveryType)) {
            return 2;
        }
        return 1; // 默认标准配送
    }

    @Override
    public IPage<Order> getOrderList(Page<Order> page, String orderNo, String userName, Integer status, String startDate, String endDate) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        
        // 订单号模糊查询
        if (StringUtils.hasText(orderNo)) {
            queryWrapper.like(Order::getOrderNo, orderNo);
        }
        
        // 用户名查询需要通过自定义SQL实现，暂时跳过
        // if (StringUtils.hasText(userName)) {
        //     // 需要关联用户表查询
        // }
        
        // 订单状态
        if (status != null) {
            queryWrapper.eq(Order::getStatus, status);
        }
        
        // 日期范围查询
        if (StringUtils.hasText(startDate)) {
            queryWrapper.ge(Order::getCreateTime, startDate + " 00:00:00");
        }
        if (StringUtils.hasText(endDate)) {
            queryWrapper.le(Order::getCreateTime, endDate + " 23:59:59");
        }
        
        // 按创建时间倒序
        queryWrapper.orderByDesc(Order::getCreateTime);
        
        IPage<Order> result = this.page(page, queryWrapper);
        
        // 设置paymentMethod字段
        result.getRecords().forEach(this::setPaymentMethodField);
        
        return result;
    }

    @Override
    public boolean updateOrderStatus(Long id, Integer status) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        order.setUpdateTime(LocalDateTime.now());
        return this.updateById(order);
    }

    @Override
    public boolean cancelOrder(Long id, String reason) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(5); // 5表示已取消
        order.setCancelReason(reason);
        order.setUpdateTime(LocalDateTime.now());
        return this.updateById(order);
    }

    @Override
    public boolean shipOrder(Long id, String company, String trackingNo, String remark) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(3); // 3表示已发货
        order.setShippingCompany(company);
        order.setTrackingNumber(trackingNo);
        order.setShippingTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        return this.updateById(order);
    }

    @Override
    public boolean confirmOrder(Long id) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(4); // 4表示已完成
        order.setConfirmTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        return this.updateById(order);
    }

    @Override
    public long getTotalOrderCount() {
        return this.count();
    }

    @Override
    public long getPendingOrderCount() {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Order::getStatus, 1, 2); // 1待付款，2待发货
        return this.count(queryWrapper);
    }

    @Override
    public long getCompletedOrderCount() {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getStatus, 4); // 4已完成
        return this.count(queryWrapper);
    }

    @Override
    public BigDecimal getTotalOrderAmount() {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getStatus, 4); // 只统计已完成的订单
        queryWrapper.select(Order::getTotalAmount);
        
        return this.list(queryWrapper).stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public IPage<Order> getUserOrderList(Long userId, Integer status, Integer page, Integer size) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        
        // 查询指定用户的订单
        queryWrapper.eq(Order::getUserId, userId);
        
        // 如果指定了状态，则按状态过滤
        if (status != null) {
            queryWrapper.eq(Order::getStatus, status);
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(Order::getCreateTime);
        
        IPage<Order> result = this.page(pageParam, queryWrapper);
        
        // 为每个订单设置paymentMethod字段并查询订单项
        result.getRecords().forEach(order -> {
            setPaymentMethodField(order);
            loadOrderItems(order);
        });
        
        return result;
    }
    
    @Override
    public Order getById(Serializable id) {
        Order order = super.getById(id);
        if (order != null) {
            setPaymentMethodField(order);
            loadOrderItems(order);
        }
        return order;
    }
    
    /**
     * 设置paymentMethod字段（从payType复制）
     */
    private void setPaymentMethodField(Order order) {
        if (order != null && order.getPayType() != null) {
            order.setPaymentMethod(order.getPayType());
        }
    }
    
    /**
     * 加载订单项数据
     */
    private void loadOrderItems(Order order) {
        if (order != null && order.getId() != null) {
            LambdaQueryWrapper<OrderItem> itemQueryWrapper = new LambdaQueryWrapper<>();
            itemQueryWrapper.eq(OrderItem::getOrderId, order.getId());
            itemQueryWrapper.orderByAsc(OrderItem::getId);
            List<OrderItem> items = orderItemMapper.selectList(itemQueryWrapper);
            order.setItems(items);
        }
    }

    // 统计相关方法实现
    
    @Override
    public BigDecimal getTotalRevenue() {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getStatus, 4); // 只统计已完成的订单
        queryWrapper.select(Order::getTotalAmount);
        
        return this.list(queryWrapper).stream()
                .map(Order::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Long getTodayOrderCount(LocalDate date) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(Order::getCreateTime, date.atStartOfDay())
                   .lt(Order::getCreateTime, date.plusDays(1).atStartOfDay());
        return this.count(queryWrapper);
    }

    @Override
    public BigDecimal getTodayRevenue(LocalDate date) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(Order::getCreateTime, date.atStartOfDay())
                   .lt(Order::getCreateTime, date.plusDays(1).atStartOfDay())
                   .eq(Order::getStatus, 4); // 只统计已完成的订单
        queryWrapper.select(Order::getTotalAmount);
        
        return this.list(queryWrapper).stream()
                .map(Order::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Long getOrderCountByStatus(Integer status) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getStatus, status);
        return this.count(queryWrapper);
    }

    @Override
    public Long getOrderCountByDate(LocalDate date) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(Order::getCreateTime, date.atStartOfDay())
                   .lt(Order::getCreateTime, date.plusDays(1).atStartOfDay());
        return this.count(queryWrapper);
    }

    @Override
    public BigDecimal getRevenueByDate(LocalDate date) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(Order::getCreateTime, date.atStartOfDay())
                   .lt(Order::getCreateTime, date.plusDays(1).atStartOfDay())
                   .eq(Order::getStatus, 4); // 只统计已完成的订单
        queryWrapper.select(Order::getTotalAmount);
        
        return this.list(queryWrapper).stream()
                .map(Order::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<Map<String, Object>> getRecentOrders(Integer limit) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Order::getCreateTime)
                   .last("LIMIT " + limit);
        
        List<Order> orders = this.list(queryWrapper);
        return orders.stream().map(order -> {
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("id", order.getId());
            orderMap.put("orderNo", order.getOrderNo());
            orderMap.put("userId", order.getUserId());
            orderMap.put("totalAmount", order.getTotalAmount());
            orderMap.put("status", order.getStatus());
            orderMap.put("statusText", getStatusText(order.getStatus()));
            orderMap.put("createTime", order.getCreateTime());
            orderMap.put("paymentMethod", order.getPayType());
            return orderMap;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getSalesReport(LocalDate startDate, LocalDate endDate, String type) {
        List<Map<String, Object>> reportData = new ArrayList<>();
        
        if ("day".equals(type)) {
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", current.toString());
                dayData.put("orders", getOrderCountByDate(current));
                dayData.put("amount", getRevenueByDate(current));
                reportData.add(dayData);
                current = current.plusDays(1);
            }
        }
        
        return reportData;
    }

    @Override
    public BigDecimal getTotalAmountByDateRange(LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(Order::getCreateTime, startDate.atStartOfDay())
                   .lt(Order::getCreateTime, endDate.plusDays(1).atStartOfDay())
                   .eq(Order::getStatus, 4); // 只统计已完成的订单
        queryWrapper.select(Order::getTotalAmount);
        
        return this.list(queryWrapper).stream()
                .map(Order::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Long getTotalOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(Order::getCreateTime, startDate.atStartOfDay())
                   .lt(Order::getCreateTime, endDate.plusDays(1).atStartOfDay());
        return this.count(queryWrapper);
    }
    
    /**
     * 获取订单状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 1: return "待付款";
            case 2: return "待发货";
            case 3: return "已发货";
            case 4: return "已完成";
            case 5: return "已取消";
            default: return "未知";
        }
    }
}