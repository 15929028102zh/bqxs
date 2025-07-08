# 数据库表名映射错误修复指南

## 问题描述

在运行应用时遇到以下错误：
```
java.sql.SQLSyntaxErrorException: Table 'fresh_delivery.cart' doesn't exist
```

## 问题分析

### 根本原因
1. **数据库表名不一致**：数据库中实际的表名是 `tb_cart`，但实体类映射的表名是 `cart`
2. **实体类映射错误**：`Cart` 实体类使用了 `@TableName("cart")` 注解
3. **SQL查询失败**：MyBatis生成的SQL语句查找不存在的 `cart` 表

### 错误堆栈分析
- 错误发生在 `CartMapper.countByUserId` 方法调用时
- SQL语句：`SELECT COALESCE(SUM(quantity), 0) FROM cart WHERE user_id = ? AND deleted = 0`
- 实际应该查询的表：`tb_cart`

## 解决方案

### 修复步骤

1. **修改Cart实体类的表名映射**
   ```java
   // 修改前
   @TableName("cart")
   public class Cart implements Serializable {
   
   // 修改后
   @TableName("tb_cart")
   public class Cart implements Serializable {
   ```

2. **验证其他实体类的表名映射**
   确认以下实体类都正确映射到对应的数据库表：
   - `User` → `tb_user` ✓
   - `Product` → `tb_product` ✓
   - `Category` → `tb_category` ✓
   - `Order` → `tb_order` ✓
   - `Admin` → `tb_admin` ✓
   - `Cart` → `tb_cart` ✓（已修复）

## 数据库表结构

### tb_cart表结构
```sql
CREATE TABLE `tb_cart` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `quantity` int NOT NULL COMMENT '商品数量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`,`product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';
```

## 验证修复

### 1. 重启后端服务
```bash
# 停止当前服务
# 重新启动服务
java -jar fresh-delivery-backend-1.0.0.jar
```

### 2. 测试购物车功能
- 登录小程序
- 添加商品到购物车
- 查看购物车列表
- 修改购物车商品数量

### 3. 检查日志
确认不再出现 `Table 'fresh_delivery.cart' doesn't exist` 错误

## 预防措施

### 1. 代码规范
- 所有实体类的 `@TableName` 注解必须与数据库实际表名一致
- 建议使用统一的表名前缀（如 `tb_`）

### 2. 开发流程
- 新增实体类时，确认数据库表已存在
- 修改表名时，同步更新实体类映射
- 定期检查实体类与数据库表的一致性

### 3. 测试策略
- 集成测试中包含数据库操作验证
- 启动时检查所有实体类的表映射

## 相关文件

- **实体类**：`f:\code\backend\src\main\java\com\biangqiang\freshdelivery\entity\Cart.java`
- **数据库脚本**：`f:\code\backend\src\main\resources\sql\init.sql`
- **Mapper接口**：`f:\code\backend\src\main\java\com\biangqiang\freshdelivery\mapper\CartMapper.java`

## 总结

通过修正 `Cart` 实体类的 `@TableName` 注解，将表名从 `cart` 更正为 `tb_cart`，成功解决了数据库表不存在的错误。这个修复确保了：

1. **数据库操作正常**：所有购物车相关的CRUD操作都能正确执行
2. **代码一致性**：实体类映射与数据库表名保持一致
3. **功能完整性**：购物车功能可以正常使用

建议在后续开发中严格遵循表名映射规范，避免类似问题再次发生。