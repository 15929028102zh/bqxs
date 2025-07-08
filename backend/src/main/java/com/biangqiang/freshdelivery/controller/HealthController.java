package com.biangqiang.freshdelivery.controller;

import com.biangqiang.freshdelivery.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Tag(name = "健康检查", description = "系统健康检查相关接口")
public class HealthController {

    // 手动添加log字段以解决编译问题
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HealthController.class);

    @Value("${wechat.miniapp.app-id:your-app-id}")
    private String appId;

    @Value("${server.port:8081}")
    private String serverPort;

    /**
     * 基础健康检查
     */
    @Operation(summary = "基础健康检查", description = "检查服务是否正常运行")
    @GetMapping
    public Result<Map<String, Object>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("service", "fresh-delivery-backend");
        healthInfo.put("port", serverPort);
        
        log.info("健康检查请求");
        return Result.success(healthInfo);
    }

    /**
     * 详细健康检查
     */
    @Operation(summary = "详细健康检查", description = "检查服务详细状态，包括配置信息")
    @GetMapping("/detail")
    public Result<Map<String, Object>> healthDetail() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("service", "fresh-delivery-backend");
        healthInfo.put("port", serverPort);
        
        // 微信配置状态
        Map<String, Object> wechatConfig = new HashMap<>();
        boolean wechatConfigured = !"your-app-id".equals(appId) && !"your-miniapp-appid".equals(appId);
        wechatConfig.put("configured", wechatConfigured);
        wechatConfig.put("appId", wechatConfigured ? appId : "未配置");
        wechatConfig.put("status", wechatConfigured ? "已配置" : "需要配置真实的微信小程序AppID和AppSecret");
        healthInfo.put("wechat", wechatConfig);
        
        // 系统信息
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("osName", System.getProperty("os.name"));
        systemInfo.put("osVersion", System.getProperty("os.version"));
        healthInfo.put("system", systemInfo);
        
        log.info("详细健康检查请求，微信配置状态: {}", wechatConfigured ? "已配置" : "未配置");
        return Result.success(healthInfo);
    }

    /**
     * 微信配置检查
     */
    @Operation(summary = "微信配置检查", description = "专门检查微信小程序配置状态")
    @GetMapping("/wechat")
    public Result<Map<String, Object>> wechatHealth() {
        Map<String, Object> wechatInfo = new HashMap<>();
        
        boolean appIdConfigured = !"your-app-id".equals(appId) && !"your-miniapp-appid".equals(appId);
        
        wechatInfo.put("appIdConfigured", appIdConfigured);
        wechatInfo.put("appId", appIdConfigured ? appId : "未配置");
        
        if (appIdConfigured) {
            wechatInfo.put("status", "配置正常");
            wechatInfo.put("message", "微信小程序AppID已配置");
        } else {
            wechatInfo.put("status", "配置缺失");
            wechatInfo.put("message", "请在application.yml中配置真实的微信小程序AppID和AppSecret");
            wechatInfo.put("configPath", "wechat.miniapp.app-id 和 wechat.miniapp.app-secret");
        }
        
        wechatInfo.put("timestamp", LocalDateTime.now());
        
        log.info("微信配置检查请求，配置状态: {}", appIdConfigured ? "已配置" : "未配置");
        return Result.success(wechatInfo);
    }
}