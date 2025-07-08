-- 修复购物车表唯一约束以支持逻辑删除
-- 执行此脚本前请确保已连接到fresh_delivery数据库

USE fresh_delivery;

-- 删除原有的唯一约束
ALTER TABLE tb_cart DROP INDEX uk_user_product;

-- 添加新的复合唯一约束，包含deleted字段
-- 这样可以允许相同的user_id和product_id组合，只要deleted状态不同
ALTER TABLE tb_cart ADD UNIQUE KEY uk_user_product_deleted (user_id, product_id, deleted);

-- 验证约束是否添加成功
SHOW INDEX FROM tb_cart WHERE Key_name = 'uk_user_product_deleted';

-- 显示表结构确认
DESCRIBE tb_cart;