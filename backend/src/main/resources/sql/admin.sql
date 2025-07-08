-- 管理员表
CREATE TABLE IF NOT EXISTS `tb_admin` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `password` varchar(255) NOT NULL COMMENT '密码（明文）',
    `name` varchar(100) DEFAULT NULL COMMENT '管理员姓名',
    `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
    `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
    `role` varchar(20) NOT NULL DEFAULT 'ADMIN' COMMENT '角色（SUPER_ADMIN: 超级管理员, ADMIN: 普通管理员）',
    `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态（0: 禁用, 1: 启用）',
    `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 插入默认管理员账户（用户名: admin, 密码: 123456）
-- 密码使用明文存储
INSERT INTO `tb_admin` (`username`, `password`, `name`, `role`, `status`) 
VALUES ('admin', '123456', '系统管理员', 'SUPER_ADMIN', 1)
ON DUPLICATE KEY UPDATE `password` = '123456';

-- 注意：默认密码为 123456，建议首次登录后立即修改密码