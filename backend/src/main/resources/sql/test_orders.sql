-- 测试订单数据
-- 插入测试用户（如果不存在）
INSERT IGNORE INTO `tb_user` (`id`, `open_id`, `nickname`, `phone`, `status`) VALUES
(1, 'test_openid_001', '测试用户1', '13800138001', 1),
(2, 'test_openid_002', '测试用户2', '13800138002', 1);

-- 插入测试地址
INSERT IGNORE INTO `tb_user_address` (`id`, `user_id`, `receiver_name`, `receiver_phone`, `province`, `city`, `district`, `detail_address`, `is_default`) VALUES
(1, 1, '张三', '13800138001', '北京市', '朝阳区', '望京街道', '望京SOHO T1座', 1),
(2, 2, '李四', '13800138002', '上海市', '浦东新区', '陆家嘴街道', '东方明珠塔', 1);

-- 插入测试订单数据
-- 待付款订单
INSERT INTO `tb_order` (`order_no`, `user_id`, `status`, `product_amount`, `delivery_fee`, `total_amount`, `pay_amount`, `pay_type`, `pay_status`, `delivery_type`, `receiver_name`, `receiver_phone`, `receiver_address`, `remark`, `create_time`, `update_time`) VALUES
('ORDER20250107001', 1, 0, 25.80, 5.00, 30.80, 0.00, 1, 0, 1, 'Zhang San', '13800138001', 'Beijing Chaoyang District', 'Please deliver ASAP', NOW(), NOW()),
('ORDER20250107002', 2, 0, 18.60, 0.00, 18.60, 0.00, 2, 0, 1, 'Li Si', '13800138002', 'Shanghai Pudong District', '', NOW(), NOW());

-- 待发货订单（现金支付已完成）
INSERT INTO `tb_order` (`order_no`, `user_id`, `status`, `product_amount`, `delivery_fee`, `total_amount`, `pay_amount`, `pay_type`, `pay_status`, `pay_time`, `delivery_type`, `receiver_name`, `receiver_phone`, `receiver_address`, `remark`, `create_time`, `update_time`) VALUES
('ORDER20250107003', 1, 1, 32.40, 5.00, 37.40, 37.40, 2, 1, NOW(), 1, 'Zhang San', '13800138001', 'Beijing Chaoyang District', 'Cash payment order', NOW(), NOW()),
('ORDER20250107004', 2, 1, 24.50, 0.00, 24.50, 24.50, 1, 1, NOW(), 1, 'Li Si', '13800138002', 'Shanghai Pudong District', 'WeChat payment order', NOW(), NOW()),
('ORDER20250107005', 1, 1, 15.80, 5.00, 20.80, 20.80, 2, 1, NOW(), 2, 'Zhang San', '13800138001', 'Beijing Chaoyang District', 'Express delivery', NOW(), NOW());

-- 待收货订单（货到付款）
INSERT INTO `tb_order` (`order_no`, `user_id`, `status`, `product_amount`, `delivery_fee`, `total_amount`, `pay_amount`, `pay_type`, `pay_status`, `delivery_type`, `receiver_name`, `receiver_phone`, `receiver_address`, `remark`, `create_time`, `update_time`) VALUES
('ORDER20250107006', 2, 2, 28.90, 5.00, 33.90, 0.00, 3, 0, 1, 'Li Si', '13800138002', 'Shanghai Pudong District', 'Cash on delivery order', NOW(), NOW()),
('ORDER20250107007', 1, 2, 41.20, 0.00, 41.20, 41.20, 1, 1, 1, 'Zhang San', '13800138001', 'Beijing Chaoyang District', 'Shipped order', NOW(), NOW());

-- 已完成订单
INSERT INTO `tb_order` (`order_no`, `user_id`, `status`, `product_amount`, `delivery_fee`, `total_amount`, `pay_amount`, `pay_type`, `pay_status`, `pay_time`, `delivery_type`, `receiver_name`, `receiver_phone`, `receiver_address`, `remark`, `finish_time`, `create_time`, `update_time`) VALUES
('ORDER20250107008', 1, 3, 19.60, 5.00, 24.60, 24.60, 2, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), 1, 'Zhang San', '13800138001', 'Beijing Chaoyang District', 'Completed order', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),
('ORDER20250107009', 2, 3, 35.80, 0.00, 35.80, 35.80, 1, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 1, 'Li Si', '13800138002', 'Shanghai Pudong District', 'Completed order', NOW(), DATE_SUB(NOW(), INTERVAL 2 DAY), NOW());

-- 已取消订单
INSERT INTO `tb_order` (`order_no`, `user_id`, `status`, `product_amount`, `delivery_fee`, `total_amount`, `pay_amount`, `pay_type`, `pay_status`, `delivery_type`, `receiver_name`, `receiver_phone`, `receiver_address`, `remark`, `cancel_time`, `cancel_reason`, `create_time`, `update_time`) VALUES
('ORDER20250107010', 1, 4, 22.40, 5.00, 27.40, 0.00, 1, 0, 1, 'Zhang San', '13800138001', 'Beijing Chaoyang District', 'Cancelled order', NOW(), 'User cancelled', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW());

-- 插入对应的订单商品数据
INSERT INTO `tb_order_item` (`order_id`, `product_id`, `product_name`, `product_image`, `product_price`, `quantity`, `total_price`) VALUES
-- 订单1的商品
(1, 1, 'Fresh Cabbage', '/images/products/baicai.jpg', 3.50, 2, 7.00),
(1, 2, 'Organic Spinach', '/images/products/bocai.jpg', 5.80, 1, 5.80),
(1, 4, 'Red Apple', '/images/products/pingguo.jpg', 8.80, 1, 8.80),
(1, 5, 'Banana', '/images/products/xiangjiao.jpg', 6.50, 1, 6.50),
-- 订单2的商品
(2, 1, 'Fresh Cabbage', '/images/products/baicai.jpg', 3.50, 3, 10.50),
(2, 3, 'Fresh Radish', '/images/products/luobo.jpg', 2.80, 1, 2.80),
(2, 2, 'Organic Spinach', '/images/products/bocai.jpg', 5.80, 1, 5.80),
-- 订单3的商品（待发货）
(3, 4, 'Red Apple', '/images/products/pingguo.jpg', 8.80, 2, 17.60),
(3, 5, 'Banana', '/images/products/xiangjiao.jpg', 6.50, 1, 6.50),
(3, 1, 'Fresh Cabbage', '/images/products/baicai.jpg', 3.50, 2, 7.00),
-- 订单4的商品（待发货）
(4, 2, 'Organic Spinach', '/images/products/bocai.jpg', 5.80, 2, 11.60),
(4, 3, 'Fresh Radish', '/images/products/luobo.jpg', 2.80, 3, 8.40),
(4, 1, 'Fresh Cabbage', '/images/products/baicai.jpg', 3.50, 1, 3.50),
-- 订单5的商品（待发货）
(5, 5, 'Banana', '/images/products/xiangjiao.jpg', 6.50, 1, 6.50),
(5, 4, 'Red Apple', '/images/products/pingguo.jpg', 8.80, 1, 8.80),
-- 订单6的商品（待收货）
(6, 1, 'Fresh Cabbage', '/images/products/baicai.jpg', 3.50, 4, 14.00),
(6, 2, 'Organic Spinach', '/images/products/bocai.jpg', 5.80, 1, 5.80),
(6, 3, 'Fresh Radish', '/images/products/luobo.jpg', 2.80, 3, 8.40),
-- 订单7的商品（待收货）
(7, 4, 'Red Apple', '/images/products/pingguo.jpg', 8.80, 3, 26.40),
(7, 5, 'Banana', '/images/products/xiangjiao.jpg', 6.50, 2, 13.00),
-- 订单8的商品（已完成）
(8, 1, 'Fresh Cabbage', '/images/products/baicai.jpg', 3.50, 2, 7.00),
(8, 2, 'Organic Spinach', '/images/products/bocai.jpg', 5.80, 1, 5.80),
(8, 5, 'Banana', '/images/products/xiangjiao.jpg', 6.50, 1, 6.50),
-- 订单9的商品（已完成）
(9, 4, 'Red Apple', '/images/products/pingguo.jpg', 8.80, 2, 17.60),
(9, 3, 'Fresh Radish', '/images/products/luobo.jpg', 2.80, 4, 11.20),
(9, 5, 'Banana', '/images/products/xiangjiao.jpg', 6.50, 1, 6.50),
-- 订单10的商品（已取消）
(10, 1, 'Fresh Cabbage', '/images/products/baicai.jpg', 3.50, 3, 10.50),
(10, 4, 'Red Apple', '/images/products/pingguo.jpg', 8.80, 1, 8.80),
(10, 2, 'Organic Spinach', '/images/products/bocai.jpg', 5.80, 1, 5.80);