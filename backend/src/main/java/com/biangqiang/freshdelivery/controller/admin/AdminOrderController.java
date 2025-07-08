package com.biangqiang.freshdelivery.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.biangqiang.freshdelivery.common.Result;
import com.biangqiang.freshdelivery.entity.Order;
import com.biangqiang.freshdelivery.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理后台订单控制器
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Tag(name = "管理后台订单管理")
@RestController
@RequestMapping("/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @Operation(summary = "分页查询订单列表")
    @GetMapping("/list")
    public Result<IPage<Order>> getOrderList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        try {
            Page<Order> pageParam = new Page<>(page, size);
            IPage<Order> orderPage = orderService.getOrderList(pageParam, orderNo, userName, status, startDate, endDate);
            return Result.success(orderPage);
        } catch (Exception e) {
            return Result.error("获取订单列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/{id}")
    public Result<Order> getOrderDetail(@PathVariable Long id) {
        try {
            Order order = orderService.getById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }
            return Result.success(order);
        } catch (Exception e) {
            return Result.error("获取订单详情失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新订单状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        try {
            Integer status = request.get("status");
            boolean success = orderService.updateOrderStatus(id, status);
            if (success) {
                return Result.success();
            } else {
                return Result.error("更新订单状态失败");
            }
        } catch (Exception e) {
            return Result.error("更新订单状态失败: " + e.getMessage());
        }
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            boolean success = orderService.cancelOrder(id, reason);
            if (success) {
                return Result.success();
            } else {
                return Result.error("取消订单失败");
            }
        } catch (Exception e) {
            return Result.error("取消订单失败: " + e.getMessage());
        }
    }

    @Operation(summary = "订单发货")
    @PutMapping("/{id}/ship")
    public Result<Void> shipOrder(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String company = request.get("company");
            String trackingNo = request.get("trackingNo");
            String remark = request.get("remark");
            boolean success = orderService.shipOrder(id, company, trackingNo, remark);
            if (success) {
                return Result.success();
            } else {
                return Result.error("订单发货失败");
            }
        } catch (Exception e) {
            return Result.error("订单发货失败: " + e.getMessage());
        }
    }

    @Operation(summary = "确认收货")
    @PutMapping("/{id}/confirm")
    public Result<Void> confirmOrder(@PathVariable Long id) {
        try {
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

    @Operation(summary = "获取订单统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getOrderStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", orderService.getTotalOrderCount());
            stats.put("pending", orderService.getPendingOrderCount());
            stats.put("completed", orderService.getCompletedOrderCount());
            stats.put("totalAmount", orderService.getTotalOrderAmount());
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取订单统计失败: " + e.getMessage());
        }
    }

    @Operation(summary = "导出订单")
    @GetMapping("/export")
    public Result<Void> exportOrders(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        try {
            // TODO: 实现订单导出功能
            return Result.success();
        } catch (Exception e) {
            return Result.error("导出订单失败: " + e.getMessage());
        }
    }
}