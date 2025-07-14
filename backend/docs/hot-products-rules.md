# 热门商品规则实现文档

## 概述

本文档描述了生鲜配送系统中热门商品的判定规则和实现方式。热门商品功能基于真实的订单数据进行统计分析，支持多维度的判定标准和灵活的配置管理。

## 功能特性

### 1. 多维度判定标准

热门商品的判定基于以下三个核心维度：

- **销量阈值**：商品的总销售数量
- **销售额阈值**：商品的总销售金额
- **订单数量阈值**：包含该商品的订单数量

### 2. 数据来源

- 基于真实订单数据统计（`tb_order` 和 `tb_order_item` 表）
- 只统计已付款且未取消的订单（状态：1-待发货，2-待收货，3-已完成）
- 排除已删除的订单和订单项

### 3. 灵活配置

通过配置文件 `hot-products-rules.properties` 支持以下配置项：

```properties
# 销量阈值
hot.products.sales.threshold=2

# 销售额阈值（元）
hot.products.revenue.threshold=10.00

# 订单数量阈值
hot.products.order.count.threshold=1

# 排序规则：SALES_DESC, REVENUE_DESC, MIXED
hot.products.sort.rule=MIXED
```

## 实现架构

### 1. 核心类结构

```
com.biangqiang.freshdelivery
├── config/
│   └── HotProductsConfig.java          # 配置类
├── mapper/
│   └── OrderItemMapper.java            # 数据访问层
├── service/impl/
│   └── ProductServiceImpl.java         # 业务逻辑层
└── resources/
    └── hot-products-rules.properties   # 配置文件
```

### 2. 关键方法

#### ProductServiceImpl.getHotProducts()

主要的热门商品获取方法，实现流程：

1. 查询所有上架商品
2. 为每个商品获取真实销售数据
3. 应用热门商品规则过滤
4. 根据配置的排序规则排序
5. 返回指定数量的热门商品

#### OrderItemMapper.getProductSalesStats()

查询商品销售统计数据的SQL方法：

```sql
SELECT 
    COALESCE(SUM(oi.quantity), 0) as total_quantity,
    COALESCE(SUM(oi.total_price), 0) as total_revenue,
    COALESCE(COUNT(DISTINCT oi.order_id), 0) as order_count
FROM tb_order_item oi
JOIN tb_order o ON oi.order_id = o.id
WHERE oi.product_id = #{productId}
    AND o.status IN (1,2,3)
    AND o.deleted = 0
    AND oi.deleted = 0
```

## 排序规则

系统支持三种排序规则：

### 1. SALES_DESC（按销量降序）
- 主要排序：销量从高到低
- 次要排序：创建时间从新到旧

### 2. REVENUE_DESC（按销售额降序）
- 主要排序：销售额从高到低
- 次要排序：创建时间从新到旧

### 3. MIXED（综合排序）
- 主要排序：销量从高到低
- 次要排序：销售额从高到低
- 第三排序：创建时间从新到旧

## 配置说明

### 基本配置

| 配置项 | 说明 | 默认值 | 示例 |
|--------|------|--------|------|
| `hot.products.sales.threshold` | 销量阈值 | 2 | 商品总销量 >= 2 件 |
| `hot.products.revenue.threshold` | 销售额阈值 | 10.00 | 商品总销售额 >= 10.00 元 |
| `hot.products.order.count.threshold` | 订单数阈值 | 1 | 至少出现在 1 个订单中 |
| `hot.products.sort.rule` | 排序规则 | MIXED | 综合排序 |

### 高级配置

| 配置项 | 说明 | 默认值 | 备注 |
|--------|------|--------|------|
| `hot.products.time.range.days` | 统计时间范围（天） | 0 | 0表示统计所有历史数据 |
| `hot.products.order.status.filter` | 订单状态过滤 | "1,2,3" | 逗号分隔的状态码 |
| `hot.products.cache.minutes` | 缓存时间（分钟） | 30 | 0表示不缓存 |

## 使用示例

### 1. 获取热门商品

```java
@Autowired
private ProductService productService;

// 获取前10个热门商品
List<ProductVO> hotProducts = productService.getHotProducts(10);
```

### 2. 修改配置

修改 `hot-products-rules.properties` 文件：

```properties
# 提高销量阈值到5件
hot.products.sales.threshold=5

# 提高销售额阈值到50元
hot.products.revenue.threshold=50.00

# 改为按销售额排序
hot.products.sort.rule=REVENUE_DESC
```

### 3. 自定义规则验证

```java
@Autowired
private HotProductsConfig hotProductsConfig;

// 验证商品是否符合热门商品规则
boolean isHot = hotProductsConfig.isHotProduct(
    actualSales,    // 实际销量
    totalRevenue,   // 总销售额
    orderCount      // 订单数量
);
```

## 性能优化建议

1. **数据库索引**：确保 `tb_order_item.product_id` 和 `tb_order.status` 字段有适当的索引
2. **缓存策略**：对于访问频繁的热门商品数据，可以考虑添加Redis缓存
3. **分页查询**：对于大量商品的场景，建议在数据库层面进行分页处理
4. **定时更新**：可以考虑通过定时任务预计算热门商品数据

## 扩展功能

### 1. 时间范围过滤

可以扩展支持只统计最近N天的订单数据：

```sql
AND o.create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY)
```

### 2. 分类热门商品

可以扩展支持按商品分类获取热门商品：

```java
List<ProductVO> getHotProductsByCategory(Long categoryId, Integer limit);
```

### 3. 用户个性化

可以扩展支持基于用户购买历史的个性化热门商品推荐。

## 注意事项

1. **数据一致性**：热门商品数据基于订单数据计算，确保订单状态更新及时
2. **配置变更**：修改配置文件后需要重启应用才能生效
3. **性能监控**：在高并发场景下，注意监控热门商品查询的性能表现
4. **业务规则**：根据实际业务需求调整阈值参数，避免过于严格或宽松的规则

## 版本历史

- **v1.0.0**：初始版本，支持基本的热门商品规则和配置
- **v1.1.0**：添加多种排序规则支持
- **v1.2.0**：优化数据查询性能，添加配置验证