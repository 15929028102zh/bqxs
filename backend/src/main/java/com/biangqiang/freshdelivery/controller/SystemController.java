package com.biangqiang.freshdelivery.controller;

import com.biangqiang.freshdelivery.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统配置控制器
 *
 * @author biangqiang
 * @since 2024-01-01
 */

@Tag(name = "系统配置", description = "系统配置相关接口")
@RestController
@RequestMapping("/system")
public class SystemController {

    @Value("${server.port:8080}")
    private String serverPort;
    
    @Value("${file.upload.domain:http://localhost:8081}")
    private String domain;

    // 模拟系统图片配置存储（实际项目中应该存储在数据库中）
    private static final Map<String, Object> systemImages = new HashMap<>();
    
    static {
        // 初始化默认系统图片配置
        systemImages.put("defaultProductImage", "/uploads/system/default-product.jpg");
        systemImages.put("defaultCategoryImage", "/uploads/system/default-category.jpg");
        systemImages.put("defaultAvatarImage", "/uploads/system/default-avatar.jpg");
        systemImages.put("bannerImages", Arrays.asList(
            "/uploads/system/banner1.jpg",
            "/uploads/system/banner2.jpg",
            "/uploads/system/banner3.jpg"
        ));
        systemImages.put("appIcon", "/uploads/system/app-icon.png");
    }

    @Operation(summary = "获取系统图片配置", description = "获取系统默认图片配置")
    @GetMapping("/images")
    public Result<Map<String, Object>> getSystemImages() {
        try {
            // 转换为完整URL
            Map<String, Object> result = new HashMap<>();
            String baseUrl = domain;
            
            for (Map.Entry<String, Object> entry : systemImages.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                if (value instanceof String) {
                    String url = (String) value;
                    result.put(key, url.startsWith("http") ? url : baseUrl + url);
                } else if (value instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> urls = (List<String>) value;
                    List<String> fullUrls = urls.stream()
                        .map(url -> url.startsWith("http") ? url : baseUrl + url)
                        .collect(Collectors.toList());
                    result.put(key, fullUrls);
                } else {
                    result.put(key, value);
                }
            }
            
            return Result.success(result);
        } catch (Exception e) {
            // 获取系统图片配置失败
            return Result.error("获取系统图片配置失败");
        }
    }

    @Operation(summary = "更新系统图片配置", description = "更新系统图片配置")
    @PutMapping("/images")
    public Result<String> updateSystemImage(@RequestBody Map<String, Object> request) {
        try {
            // 更新系统图片配置
            for (Map.Entry<String, Object> entry : request.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                // 验证配置项是否存在
                if (systemImages.containsKey(key)) {
                    // 如果是URL，转换为相对路径存储
                    if (value instanceof String) {
                        String url = (String) value;
                        if (url.startsWith(domain)) {
                            url = url.substring(domain.length());
                        }
                        systemImages.put(key, url);
                    } else {
                        systemImages.put(key, value);
                    }
                    
                    // 更新系统图片配置
                }
            }
            
            return Result.success("更新成功");
        } catch (Exception e) {
            // 更新系统图片配置失败
            return Result.error("更新系统图片配置失败: " + e.getMessage());
        }
    }

    @Operation(summary = "重置系统图片配置", description = "重置系统图片配置为默认值")
    @PostMapping("/images/reset")
    public Result<String> resetSystemImages() {
        try {
            // 重置为默认配置
            systemImages.clear();
            systemImages.put("defaultProductImage", "/uploads/system/default-product.jpg");
            systemImages.put("defaultCategoryImage", "/uploads/system/default-category.jpg");
            systemImages.put("defaultAvatarImage", "/uploads/system/default-avatar.jpg");
            systemImages.put("bannerImages", Arrays.asList(
                "/uploads/system/banner1.jpg",
                "/uploads/system/banner2.jpg",
                "/uploads/system/banner3.jpg"
            ));
            systemImages.put("appIcon", "/uploads/system/app-icon.png");
            
            // 重置系统图片配置成功
            return Result.success("重置成功");
        } catch (Exception e) {
            // 重置系统图片配置失败
            return Result.error("重置系统图片配置失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取系统信息", description = "获取系统基本信息")
    @GetMapping("/info")
    public Result<Map<String, Object>> getSystemInfo() {
        try {
            Map<String, Object> systemInfo = new HashMap<>();
            systemInfo.put("version", "1.0.0");
            systemInfo.put("name", "边墙鲜送管理系统");
            systemInfo.put("description", "新鲜食材配送管理平台");
            systemInfo.put("serverTime", System.currentTimeMillis());
            systemInfo.put("serverPort", serverPort);
            
            // 系统运行状态
            Runtime runtime = Runtime.getRuntime();
            Map<String, Object> runtime_info = new HashMap<>();
            runtime_info.put("totalMemory", runtime.totalMemory());
            runtime_info.put("freeMemory", runtime.freeMemory());
            runtime_info.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
            runtime_info.put("maxMemory", runtime.maxMemory());
            runtime_info.put("availableProcessors", runtime.availableProcessors());
            systemInfo.put("runtime", runtime_info);
            
            return Result.success(systemInfo);
        } catch (Exception e) {
            // 获取系统信息失败
            return Result.error("获取系统信息失败");
        }
    }
}