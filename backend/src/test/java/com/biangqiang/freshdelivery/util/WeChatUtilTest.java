package com.biangqiang.freshdelivery.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 微信工具类测试
 * 用于验证微信小程序配置是否正确
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Slf4j
@SpringBootTest
public class WeChatUtilTest {

    @Autowired
    private WeChatUtil weChatUtil;

    /**
     * 测试微信配置验证
     * 这个测试会在启动时自动验证配置
     */
    @Test
    public void testConfigValidation() {
        log.info("微信配置验证测试开始");
        // 配置验证在@PostConstruct中自动执行
        log.info("微信配置验证测试完成");
    }

    /**
     * 测试微信登录（需要真实的code）
     * 注意：这个测试需要从小程序获取真实的code才能成功
     */
    @Test
    public void testWeChatLogin() {
        log.info("微信登录测试开始");
        
        // 这里使用一个无效的测试code来验证错误处理
        String testCode = "invalid_test_code";
        
        WeChatUtil.WeChatUserInfo userInfo = weChatUtil.getWeChatUserInfo(testCode);
        
        if (userInfo == null) {
            log.info("预期结果：使用无效code返回null，错误处理正常");
        } else {
            log.info("获取到用户信息: openId={}", userInfo.getOpenId());
        }
        
        log.info("微信登录测试完成");
    }

    /**
     * 手动测试方法
     * 如果你有真实的微信小程序code，可以在这里测试
     */
    public void manualTestWithRealCode(String realCode) {
        log.info("手动测试开始，使用真实code: {}", realCode);
        
        WeChatUtil.WeChatUserInfo userInfo = weChatUtil.getWeChatUserInfo(realCode);
        
        if (userInfo != null) {
            log.info("登录成功！");
            log.info("OpenID: {}", userInfo.getOpenId());
            log.info("UnionID: {}", userInfo.getUnionId());
            log.info("SessionKey: {}", userInfo.getSessionKey() != null ? "已获取" : "未获取");
        } else {
            log.error("登录失败，请检查:");
            log.error("1. 微信小程序AppID和AppSecret配置是否正确");
            log.error("2. code是否有效（code只能使用一次且有时效性）");
            log.error("3. 网络连接是否正常");
        }
        
        log.info("手动测试完成");
    }
}