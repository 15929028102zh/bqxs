package com.biangqiang.freshdelivery.controller.admin;

import com.biangqiang.freshdelivery.common.Result;
import com.biangqiang.freshdelivery.service.OrderService;
import com.biangqiang.freshdelivery.service.ProductService;
import com.biangqiang.freshdelivery.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 管理后台统计控制器
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Tag(name = "管理后台数据统计")
@RestController
@RequestMapping("/admin/statistics")
@RequiredArgsConstructor
public class AdminStatisticsController {

    private final OrderService orderService;
    private final ProductService productService;
    private final UserService userService;

    @Operation(summary = "获取统计概览")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getStatisticsOverview() {
        try {
            Map<String, Object> overview = new HashMap<>();
            
            // 基础统计数据
            overview.put("totalUsers", userService.getTotalUserCount());
            overview.put("totalProducts", productService.getTotalProductCount());
            overview.put("totalOrders", orderService.getTotalOrderCount());
            overview.put("totalRevenue", orderService.getTotalRevenue());
            
            // 今日数据
            LocalDate today = LocalDate.now();
            overview.put("todayOrders", orderService.getTodayOrderCount(today));
            overview.put("todayRevenue", orderService.getTodayRevenue(today));
            overview.put("todayUsers", userService.getTodayUserCount(today));
            
            // 订单状态分布
            Map<String, Object> orderStatusStats = new HashMap<>();
            orderStatusStats.put("pending", orderService.getOrderCountByStatus(1)); // 待付款
            orderStatusStats.put("paid", orderService.getOrderCountByStatus(2)); // 待发货
            orderStatusStats.put("shipped", orderService.getOrderCountByStatus(3)); // 已发货
            orderStatusStats.put("completed", orderService.getOrderCountByStatus(4)); // 已完成
            orderStatusStats.put("cancelled", orderService.getOrderCountByStatus(5)); // 已取消
            overview.put("orderStatusStats", orderStatusStats);
            
            return Result.success(overview);
        } catch (Exception e) {
            return Result.error("获取统计概览失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取销售趋势数据")
    @GetMapping("/sales-trend")
    public Result<Map<String, Object>> getSalesTrend(
            @RequestParam(defaultValue = "week") String period
    ) {
        try {
            Map<String, Object> trendData = new HashMap<>();
            List<Map<String, Object>> salesData = new ArrayList<>();
            
            if ("week".equals(period)) {
                // 获取近7天的销售数据
                for (int i = 6; i >= 0; i--) {
                    LocalDate date = LocalDate.now().minusDays(i);
                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("date", date.format(DateTimeFormatter.ofPattern("MM-dd")));
                    dayData.put("orders", orderService.getOrderCountByDate(date));
                    dayData.put("amount", orderService.getRevenueByDate(date));
                    salesData.add(dayData);
                }
            } else if ("month".equals(period)) {
                // 获取近30天的销售数据
                for (int i = 29; i >= 0; i--) {
                    LocalDate date = LocalDate.now().minusDays(i);
                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("date", date.format(DateTimeFormatter.ofPattern("MM-dd")));
                    dayData.put("orders", orderService.getOrderCountByDate(date));
                    dayData.put("amount", orderService.getRevenueByDate(date));
                    salesData.add(dayData);
                }
            }
            
            trendData.put("salesData", salesData);
            return Result.success(trendData);
        } catch (Exception e) {
            return Result.error("获取销售趋势失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取最新订单")
    @GetMapping("/recent-orders")
    public Result<List<Map<String, Object>>> getRecentOrders(
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        try {
            List<Map<String, Object>> recentOrders = orderService.getRecentOrders(limit);
            return Result.success(recentOrders);
        } catch (Exception e) {
            return Result.error("获取最新订单失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取热门商品")
    @GetMapping("/hot-products")
    public Result<List<Map<String, Object>>> getHotProducts(
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        try {
            List<Map<String, Object>> hotProducts = productService.getHotProductsStats(limit);
            return Result.success(hotProducts);
        } catch (Exception e) {
            return Result.error("获取热门商品失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取用户增长趋势")
    @GetMapping("/user-growth")
    public Result<Map<String, Object>> getUserGrowth(
            @RequestParam(defaultValue = "week") String period
    ) {
        try {
            Map<String, Object> growthData = new HashMap<>();
            List<Map<String, Object>> userData = new ArrayList<>();
            
            if ("week".equals(period)) {
                // 获取近7天的用户增长数据
                for (int i = 6; i >= 0; i--) {
                    LocalDate date = LocalDate.now().minusDays(i);
                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("date", date.format(DateTimeFormatter.ofPattern("MM-dd")));
                    dayData.put("newUsers", userService.getNewUserCountByDate(date));
                    dayData.put("totalUsers", userService.getTotalUserCountByDate(date));
                    userData.add(dayData);
                }
            } else if ("month".equals(period)) {
                // 获取近30天的用户增长数据
                for (int i = 29; i >= 0; i--) {
                    LocalDate date = LocalDate.now().minusDays(i);
                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("date", date.format(DateTimeFormatter.ofPattern("MM-dd")));
                    dayData.put("newUsers", userService.getNewUserCountByDate(date));
                    dayData.put("totalUsers", userService.getTotalUserCountByDate(date));
                    userData.add(dayData);
                }
            }
            
            growthData.put("userData", userData);
            return Result.success(growthData);
        } catch (Exception e) {
            return Result.error("获取用户增长趋势失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取商品分类统计")
    @GetMapping("/category-stats")
    public Result<List<Map<String, Object>>> getCategoryStats() {
        try {
            List<Map<String, Object>> categoryStats = productService.getCategoryStats();
            return Result.success(categoryStats);
        } catch (Exception e) {
            return Result.error("获取商品分类统计失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取销售报表")
    @GetMapping("/sales-report")
    public Result<Map<String, Object>> getSalesReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "day") String type
    ) {
        try {
            Map<String, Object> reportData = new HashMap<>();
            
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            // 获取指定时间段的销售数据
            List<Map<String, Object>> salesData = orderService.getSalesReport(start, end, type);
            reportData.put("salesData", salesData);
            
            // 统计汇总
            BigDecimal totalAmount = orderService.getTotalAmountByDateRange(start, end);
            Long totalOrders = orderService.getTotalOrdersByDateRange(start, end);
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalAmount", totalAmount);
            summary.put("totalOrders", totalOrders);
            summary.put("avgOrderAmount", totalOrders > 0 ? totalAmount.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO);
            reportData.put("summary", summary);
            
            return Result.success(reportData);
        } catch (Exception e) {
            return Result.error("获取销售报表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取库存预警")
    @GetMapping("/stock-alert")
    public Result<List<Map<String, Object>>> getStockAlert(
            @RequestParam(defaultValue = "10") Integer threshold
    ) {
        try {
            List<Map<String, Object>> lowStockProducts = productService.getLowStockProducts(threshold);
            return Result.success(lowStockProducts);
        } catch (Exception e) {
            return Result.error("获取库存预警失败: " + e.getMessage());
        }
    }
}