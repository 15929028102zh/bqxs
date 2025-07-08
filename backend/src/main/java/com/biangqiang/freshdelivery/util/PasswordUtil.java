package com.biangqiang.freshdelivery.util;

import org.springframework.stereotype.Component;

/**
 * 密码工具类
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Component
public class PasswordUtil {

    // 明文存储，不再需要盐值

    /**
     * 加密密码（明文存储）
     *
     * @param password 原始密码
     * @return 明文密码
     */
    public String encryptPassword(String password) {
        // 改为明文存储，直接返回原始密码
        return password;
    }

    /**
     * 验证密码（明文比较）
     *
     * @param password 原始密码
     * @param storedPassword 存储的密码
     * @return 是否匹配
     */
    public boolean verifyPassword(String password, String storedPassword) {
        // 改为明文比较
        return password != null && password.equals(storedPassword);
    }
}