-- 为tb_order_item表添加deleted字段的SQL脚本
-- 执行此脚本前请确保已连接到fresh_delivery数据库

USE fresh_delivery;

-- 添加deleted字段到tb_order_item表
ALTER TABLE tb_order_item ADD COLUMN deleted tinyint DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除';

-- 验证字段是否添加成功
DESC tb_order_item;

-- 查看表结构
SHOW CREATE TABLE tb_order_item;