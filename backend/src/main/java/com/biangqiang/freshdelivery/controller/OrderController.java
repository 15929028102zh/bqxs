package com.biangqiang.freshdelivery.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.biangqiang.freshdelivery.common.Result;
import com.biangqiang.freshdelivery.entity.Order;
import com.biangqiang.freshdelivery.service.CashPaymentService;
import com.biangqiang.freshdelivery.service.OrderService;
import com.biangqiang.freshdelivery.service.WechatPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户端订单控制器
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Slf4j
@Tag(name = "订单管理", description = "用户端订单相关接口")
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final WechatPayService wechatPayService;
    private final CashPaymentService cashPaymentService;

    @Operation(summary = "创建订单", description = "用户创建新订单")
    @PostMapping("/create")
    public Result<Map<String, Object>> createOrder(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        // 从请求中获取用户ID（这里简化处理，实际应该从JWT token中获取）
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        try {
            // 获取请求参数
            Long addressId = Long.valueOf(request.get("addressId").toString());
            String deliveryType = request.get("deliveryType").toString();
            String paymentMethod = request.get("paymentMethod").toString();
            String remark = request.get("remark") != null ? request.get("remark").toString() : "";
            
            // 获取商品列表
            List<Map<String, Object>> productsList = (List<Map<String, Object>>) request.get("products");
            
            // 创建订单
            Map<String, Object> orderResult = orderService.createOrder(userId, addressId, productsList, deliveryType, paymentMethod, remark);
            
            return Result.success(orderResult);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("创建订单失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取订单列表", description = "获取用户的订单列表")
    @GetMapping("/list")
    public Result<Map<String, Object>> getOrderList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        try {
            IPage<Order> orderPage = orderService.getUserOrderList(userId, status, page, size);
            
            // 转换为前端期望的数据格式
            Map<String, Object> result = new HashMap<>();
            result.put("list", orderPage.getRecords());
            result.put("total", orderPage.getTotal());
            result.put("page", orderPage.getCurrent());
            result.put("pageSize", orderPage.getSize());
            result.put("hasMore", orderPage.getCurrent() < orderPage.getPages());
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取订单列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取订单详情", description = "获取指定订单的详细信息")
    @GetMapping("/{id}")
    public Result<Order> getOrderDetail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        try {
            Order order = orderService.getById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }
            // 验证订单是否属于当前用户
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权访问此订单");
            }
            return Result.success(order);
        } catch (Exception e) {
            return Result.error("获取订单详情失败: " + e.getMessage());
        }
    }

    @Operation(summary = "取消订单", description = "用户取消订单")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        try {
            Order order = orderService.getById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }
            // 验证订单是否属于当前用户
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权操作此订单");
            }
            
            boolean success = orderService.cancelOrder(id, "用户取消");
            if (success) {
                return Result.success();
            } else {
                return Result.error("取消订单失败");
            }
        } catch (Exception e) {
            return Result.error("取消订单失败: " + e.getMessage());
        }
    }

    @Operation(summary = "确认收货", description = "用户确认收货")
    @PutMapping("/{id}/confirm")
    public Result<Void> confirmOrder(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        try {
            Order order = orderService.getById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }
            // 验证订单是否属于当前用户
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权操作此订单");
            }
            
            boolean success = orderService.confirmOrder(id);
            if (success) {
                return Result.success();
            } else {
                return Result.error("确认收货失败");
            }
        } catch (Exception e) {
            return Result.error("确认收货失败: " + e.getMessage());
        }
    }

    @Operation(summary = "订单支付", description = "发起订单支付")
    @PostMapping("/{id}/pay")
    public Result<Map<String, Object>> payOrder(@PathVariable Long id, @RequestBody Map<String, Object> payRequest, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        try {
            Order order = orderService.getById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }
            // 验证订单是否属于当前用户
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权操作此订单");
            }
            
            // 检查订单状态（0-待支付）
            if (!Integer.valueOf(0).equals(order.getStatus())) {
                return Result.error("订单状态不允许支付，当前状态：" + order.getStatus());
            }
            
            // 根据支付方式处理
            Integer paymentMethod = order.getPayType();
            Map<String, Object> payInfo;
            
            if (paymentMethod == 1) {
                // 微信支付
                payInfo = wechatPayService.createPayOrder(order);
            } else if (paymentMethod == 2) {
                // 现金支付 - 系统记账
                boolean success = cashPaymentService.processCashPayment(order);
                if (success) {
                    // 更新订单状态为待发货，同时更新支付状态和支付时间
                    Order updateOrder = new Order();
                    updateOrder.setId(id);
                    updateOrder.setStatus(1); // 1表示待发货
                    updateOrder.setPayStatus(1); // 1表示已支付
                    updateOrder.setPayTime(java.time.LocalDateTime.now());
                    updateOrder.setUpdateTime(java.time.LocalDateTime.now());
                    orderService.updateById(updateOrder);
                    
                    payInfo = new HashMap<>();
                    payInfo.put("paymentMethod", "现金支付");
                    payInfo.put("status", "success");
                    payInfo.put("message", "现金支付记账成功，请准备现金等待配送员收取");
                    payInfo.put("orderNo", order.getOrderNo());
                    payInfo.put("paymentTime", java.time.LocalDateTime.now());
                } else {
                    return Result.error("现金支付记账失败");
                }
            } else if (paymentMethod == 3) {
                // 货到付款 - 预记账，配送时收款
                boolean success = cashPaymentService.processCashOnDelivery(order);
                if (success) {
                    // 更新订单状态为待收货（货到付款订单直接进入待收货状态），支付状态保持未支付
                    Order updateOrder = new Order();
                    updateOrder.setId(id);
                    updateOrder.setStatus(2); // 2表示待收货
                    updateOrder.setPayStatus(0); // 0表示未支付（货到付款时收取）
                    updateOrder.setUpdateTime(java.time.LocalDateTime.now());
                    orderService.updateById(updateOrder);
                    
                    payInfo = new HashMap<>();
                    payInfo.put("paymentMethod", "货到付款");
                    payInfo.put("status", "success");
                    payInfo.put("message", "货到付款订单已确认，配送员将在送达时收取现金");
                    payInfo.put("orderNo", order.getOrderNo());
                    payInfo.put("paymentTime", java.time.LocalDateTime.now());
                    payInfo.put("deliveryPayment", true); // 标识为配送时付款
                    payInfo.put("expectedAmount", order.getTotalAmount()); // 预期收款金额
                } else {
                    return Result.error("货到付款记账失败");
                }
            } else {
                return Result.error("不支持的支付方式");
            }
            
            return Result.success(payInfo);
        } catch (Exception e) {
            return Result.error("支付失败：" + e.getMessage());
        }
    }

    @Operation(summary = "确认货到付款收款", description = "配送员确认货到付款收款")
    @PostMapping("/{id}/confirm-cash-payment")
    public Result<Map<String, Object>> confirmCashOnDeliveryPayment(
            @PathVariable Long id, 
            @RequestBody Map<String, Object> paymentRequest, 
            HttpServletRequest request) {
        try {
            Order order = orderService.getById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            // 检查订单是否为货到付款
            if (order.getPayType() != 3) {
                return Result.error("该订单不是货到付款订单");
            }
            
            // 获取实际收款金额
            Double actualAmount = Double.parseDouble(paymentRequest.get("actualAmount").toString());
            String deliveryPersonId = paymentRequest.get("deliveryPersonId").toString();
            
            // 确认收款
            boolean success = cashPaymentService.confirmCashOnDeliveryPayment(order.getOrderNo(), actualAmount);
            if (success) {
                // 更新订单状态为已完成，同时更新支付状态和支付时间
                Order updateOrder = new Order();
                updateOrder.setId(id);
                updateOrder.setStatus(3); // 3表示已完成
                updateOrder.setPayStatus(1); // 1表示已支付
                updateOrder.setPayTime(java.time.LocalDateTime.now());
                updateOrder.setUpdateTime(java.time.LocalDateTime.now());
                orderService.updateById(updateOrder);
                
                Map<String, Object> result = new HashMap<>();
                result.put("orderNo", order.getOrderNo());
                result.put("expectedAmount", order.getTotalAmount());
                result.put("actualAmount", actualAmount);
                result.put("deliveryPersonId", deliveryPersonId);
                result.put("confirmTime", java.time.LocalDateTime.now());
                result.put("status", "confirmed");
                
                return Result.success(result);
            } else {
                return Result.error("确认收款失败");
            }
        } catch (Exception e) {
            return Result.error("确认收款失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取物流信息", description = "获取订单的物流信息")
    @GetMapping("/{id}/logistics")
    public Result<Map<String, Object>> getOrderLogistics(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        try {
            Order order = orderService.getById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }
            // 验证订单是否属于当前用户
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权访问此订单");
            }
            
            // 检查订单状态，只有已发货的订单才有物流信息
            if (order.getStatus() < 2) {
                return Result.error("订单尚未发货，暂无物流信息");
            }
            
            // 构造物流信息（这里使用模拟数据，实际应该调用物流公司API）
            Map<String, Object> logistics = new HashMap<>();
            logistics.put("company", "顺丰速运");
            logistics.put("trackingNo", "SF" + order.getOrderNo().substring(5));
            logistics.put("status", order.getStatus() == 2 ? "运输中" : "已签收");
            
            // 模拟物流轨迹
            List<Map<String, Object>> trackingInfo = new java.util.ArrayList<>();
            
            if (order.getStatus() >= 3) {
                Map<String, Object> delivered = new HashMap<>();
                delivered.put("description", "您的快件已签收，感谢使用顺丰速运");
                delivered.put("time", "2025-07-06 14:30:00");
                delivered.put("location", "收件人本人签收");
                trackingInfo.add(delivered);
            }
            
            Map<String, Object> delivering = new HashMap<>();
            delivering.put("description", "快件正在派送中，请保持电话畅通");
            delivering.put("time", "2025-07-06 09:15:00");
            delivering.put("location", "北京市朝阳区");
            trackingInfo.add(delivering);
            
            Map<String, Object> transit = new HashMap<>();
            transit.put("description", "快件已到达北京朝阳分拣中心");
            transit.put("time", "2025-07-06 06:20:00");
            transit.put("location", "北京朝阳分拣中心");
            trackingInfo.add(transit);
            
            Map<String, Object> shipped = new HashMap<>();
            shipped.put("description", "快件已从上海分拣中心发出");
            shipped.put("time", "2025-07-05 20:30:00");
            shipped.put("location", "上海分拣中心");
            trackingInfo.add(shipped);
            
            Map<String, Object> collected = new HashMap<>();
            collected.put("description", "快件已被收取");
            collected.put("time", "2025-07-05 16:45:00");
            collected.put("location", "上海市浦东新区");
            trackingInfo.add(collected);
            
            logistics.put("trackingInfo", trackingInfo);
            
            return Result.success(logistics);
        } catch (Exception e) {
            return Result.error("获取物流信息失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取订单统计", description = "获取用户的订单统计信息")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getOrderStats(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        try {
            // 这里应该实现获取用户订单统计的逻辑
            // 暂时返回模拟数据
            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("waitingPayment", 0);
            stats.put("waitingShipment", 0);
            stats.put("waitingReceive", 0);
            stats.put("completed", 0);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取订单统计失败: " + e.getMessage());
        }
    }

    /**
     * 从请求中获取用户ID（简化实现，实际应该从JWT token中解析）
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        // 这里简化处理，实际应该从JWT token中获取用户ID
        // 暂时返回固定用户ID用于测试
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            // 这里应该解析JWT token获取用户ID
            // 暂时返回固定值用于测试
            return 1L;
        }
        return null;
    }
}