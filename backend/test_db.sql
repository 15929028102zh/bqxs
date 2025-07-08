-- 测试数据库连接和查询订单数据
USE fresh_delivery;

-- 查看订单表数据
SELECT COUNT(*) as order_count FROM tb_order;

-- 查看订单详情（前5条）
SELECT id, order_no, user_id, status, total_amount, create_time FROM tb_order LIMIT 5;

-- 查看订单商品表数据
SELECT COUNT(*) as order_item_count FROM tb_order_item;

-- 查看订单商品详情（前5条）
SELECT oi.order_id, oi.product_name, oi.product_price, oi.quantity, oi.total_price 
FROM tb_order_item oi LIMIT 5;

-- 查看用户表数据
SELECT COUNT(*) as user_count FROM tb_user;
SELECT id, nickname, phone FROM tb_user LIMIT 3;