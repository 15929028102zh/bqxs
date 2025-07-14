package com.biangqiang.freshdelivery.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 热门商品规则配置类
 * Hot Products Rules Configuration
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "hot.products")
@PropertySource("classpath:hot-products-rules.properties")
public class HotProductsConfig {

    /**
     * 销量阈值：商品总销量必须达到此数量才能被认定为热门商品
     */
    private Integer salesThreshold = 2;

    /**
     * 销售额阈值：商品总销售额必须达到此金额才能被认定为热门商品
     */
    private BigDecimal revenueThreshold = new BigDecimal("10.00");

    /**
     * 订单数量阈值：商品必须至少出现在此数量的订单中才能被认定为热门商品
     */
    private Integer orderCountThreshold = 1;

    /**
     * 统计时间范围（天数）：只统计最近N天的订单数据，0表示统计所有历史数据
     */
    private Integer timeRangeDays = 0;

    /**
     * 商品状态过滤：只统计指定状态的订单
     */
    private String orderStatusFilter = "1,2,3";

    /**
     * 排序规则：热门商品的排序方式
     */
    private String sortRule = "MIXED";

    /**
     * 缓存时间（分钟）：热门商品数据的缓存时间，0表示不缓存
     */
    private Integer cacheMinutes = 30;

    /**
     * 获取订单状态过滤列表
     * @return 订单状态列表
     */
    public List<Integer> getOrderStatusList() {
        return Arrays.stream(orderStatusFilter.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    /**
     * 排序规则枚举
     */
    public enum SortRule {
        SALES_DESC,     // 按销量降序
        REVENUE_DESC,   // 按销售额降序
        MIXED          // 综合排序（销量优先，销售额次之）
    }

    /**
     * 获取排序规则枚举
     * @return 排序规则
     */
    public SortRule getSortRuleEnum() {
        try {
            return SortRule.valueOf(sortRule.toUpperCase());
        } catch (IllegalArgumentException e) {
            return SortRule.MIXED; // 默认值
        }
    }

    /**
     * 验证热门商品规则
     * @param actualSales 实际销量
     * @param totalRevenue 总销售额
     * @param orderCount 订单数量
     * @return 是否符合热门商品规则
     */
    public boolean isHotProduct(Integer actualSales, BigDecimal totalRevenue, Integer orderCount) {
        return (actualSales != null && actualSales >= salesThreshold) &&
               (totalRevenue != null && totalRevenue.compareTo(revenueThreshold) >= 0) &&
               (orderCount != null && orderCount >= orderCountThreshold);
    }
}