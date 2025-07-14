package com.biangqiang.freshdelivery.controller;

import com.biangqiang.freshdelivery.common.Result;
import com.biangqiang.freshdelivery.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图片资源控制器
 *
 * @author biangqiang
 * @since 2024-01-01
 */

@Tag(name = "图片资源", description = "图片资源相关接口")
@RestController
@RequestMapping("/image")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @Operation(summary = "获取商品图片", description = "根据商品ID获取商品图片列表")
    @GetMapping("/product/{productId}")
    public Result<Map<String, Object>> getProductImages(@PathVariable Long productId) {
        try {
            List<String> images = imageService.getProductImages(productId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("productId", productId);
            result.put("images", images);
            result.put("count", images.size());
            result.put("mainImage", images.isEmpty() ? null : images.get(0));
            
            return Result.success(result);
        } catch (Exception e) {
            // 获取商品图片失败
            return Result.error("获取商品图片失败");
        }
    }

    @Operation(summary = "获取分类图片", description = "根据分类ID获取分类图片")
    @GetMapping("/category/{categoryId}")
    public Result<Map<String, Object>> getCategoryImage(@PathVariable Long categoryId) {
        try {
            String image = imageService.getCategoryImage(categoryId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("categoryId", categoryId);
            result.put("image", image);
            
            return Result.success(result);
        } catch (Exception e) {
            // 获取分类图片失败
            return Result.error("获取分类图片失败");
        }
    }

    @Operation(summary = "获取用户头像", description = "根据用户ID获取用户头像")
    @GetMapping("/avatar/{userId}")
    public Result<Map<String, Object>> getUserAvatar(@PathVariable Long userId) {
        try {
            String avatar = imageService.getUserAvatar(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("avatar", avatar);
            
            return Result.success(result);
        } catch (Exception e) {
            // 获取用户头像失败
            return Result.error("获取用户头像失败");
        }
    }

    @Operation(summary = "获取轮播图", description = "获取首页轮播图列表")
    @GetMapping("/banner")
    public Result<List<Map<String, Object>>> getBannerImages() {
        try {
            List<Map<String, Object>> banners = imageService.getBannerImages();
            return Result.success(banners);
        } catch (Exception e) {
            // 获取轮播图失败
            return Result.error("获取轮播图失败");
        }
    }

    @Operation(summary = "获取默认图片", description = "根据类型获取默认图片")
    @GetMapping("/default/{type}")
    public Result<Map<String, Object>> getDefaultImage(@PathVariable String type) {
        try {
            String image = imageService.getDefaultImage(type);
            
            Map<String, Object> result = new HashMap<>();
            result.put("type", type);
            result.put("image", image);
            
            return Result.success(result);
        } catch (Exception e) {
            // 获取默认图片失败
            return Result.error("获取默认图片失败");
        }
    }

    @Operation(summary = "压缩图片", description = "获取指定尺寸的压缩图片URL")
    @GetMapping("/compress")
    public Result<Map<String, Object>> compressImage(
            @RequestParam String url,
            @RequestParam(defaultValue = "300") int width,
            @RequestParam(defaultValue = "300") int height) {
        try {
            if (!imageService.isValidImageUrl(url)) {
                return Result.error("无效的图片URL");
            }
            
            String compressedUrl = imageService.compressImage(url, width, height);
            
            Map<String, Object> result = new HashMap<>();
            result.put("originalUrl", url);
            result.put("compressedUrl", compressedUrl);
            result.put("width", width);
            result.put("height", height);
            
            return Result.success(result);
        } catch (Exception e) {
            // 压缩图片失败
            return Result.error("压缩图片失败");
        }
    }

    @Operation(summary = "批量获取图片", description = "批量获取多种类型的图片资源")
    @PostMapping("/batch")
    public Result<Map<String, Object>> getBatchImages(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 获取商品图片
            if (request.containsKey("productIds")) {
                List<?> productIdList = (List<?>) request.get("productIds");
                Map<Long, List<String>> productImages = new HashMap<>();
                for (Object productIdObj : productIdList) {
                    Long productId = productIdObj instanceof Integer ? 
                        ((Integer) productIdObj).longValue() : (Long) productIdObj;
                    productImages.put(productId, imageService.getProductImages(productId));
                }
                result.put("productImages", productImages);
            }
            
            // 获取分类图片
            if (request.containsKey("categoryIds")) {
                List<?> categoryIdList = (List<?>) request.get("categoryIds");
                Map<Long, String> categoryImages = new HashMap<>();
                for (Object categoryIdObj : categoryIdList) {
                    Long categoryId = categoryIdObj instanceof Integer ? 
                        ((Integer) categoryIdObj).longValue() : (Long) categoryIdObj;
                    categoryImages.put(categoryId, imageService.getCategoryImage(categoryId));
                }
                result.put("categoryImages", categoryImages);
            }
            
            // 获取用户头像
            if (request.containsKey("userIds")) {
                List<?> userIdList = (List<?>) request.get("userIds");
                Map<Long, String> userAvatars = new HashMap<>();
                for (Object userIdObj : userIdList) {
                    Long userId = userIdObj instanceof Integer ? 
                        ((Integer) userIdObj).longValue() : (Long) userIdObj;
                    userAvatars.put(userId, imageService.getUserAvatar(userId));
                }
                result.put("userAvatars", userAvatars);
            }
            
            // 获取轮播图
            if (request.containsKey("includeBanner") && (Boolean) request.get("includeBanner")) {
                result.put("bannerImages", imageService.getBannerImages());
            }
            
            return Result.success(result);
        } catch (Exception e) {
            // 批量获取图片失败
            return Result.error("批量获取图片失败");
        }
    }
}