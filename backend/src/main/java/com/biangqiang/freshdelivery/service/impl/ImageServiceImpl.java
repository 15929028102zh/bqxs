package com.biangqiang.freshdelivery.service.impl;

import com.biangqiang.freshdelivery.service.ImageService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 图片服务实现类
 *
 * @author biangqiang
 * @since 2024-01-01
 */

@Service
public class ImageServiceImpl implements ImageService {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${upload.path:./uploads}")
    private String uploadPath;
    
    @Value("${file.upload.domain:http://localhost:8081}")
    private String domain;

    // 模拟数据库存储的图片关联关系
    private final Map<Long, List<String>> productImages = new HashMap<>();
    private final Map<Long, String> categoryImages = new HashMap<>();
    private final Map<Long, String> userAvatars = new HashMap<>();
    private final List<Map<String, Object>> bannerImages = new ArrayList<>();

    public ImageServiceImpl() {
        // 初始化一些示例数据
        initSampleData();
    }

    @Override
    public List<String> getProductImages(Long productId) {
        List<String> images = productImages.get(productId);
        if (images == null || images.isEmpty()) {
            // 返回默认商品图片
            return Arrays.asList(getDefaultImage("product"));
        }
        return new ArrayList<>(images);
    }

    @Override
    public String getCategoryImage(Long categoryId) {
        String image = categoryImages.get(categoryId);
        if (image == null || image.isEmpty()) {
            return getDefaultImage("category");
        }
        return image;
    }

    @Override
    public String getUserAvatar(Long userId) {
        String avatar = userAvatars.get(userId);
        if (avatar == null || avatar.isEmpty()) {
            return getDefaultImage("avatar");
        }
        return avatar;
    }

    @Override
    public List<Map<String, Object>> getBannerImages() {
        if (bannerImages.isEmpty()) {
            // 返回默认轮播图
            Map<String, Object> defaultBanner = new HashMap<>();
            defaultBanner.put("id", 1L);
            defaultBanner.put("title", "欢迎使用边墙鲜送");
            defaultBanner.put("imageUrl", getDefaultImage("banner"));
            defaultBanner.put("linkUrl", "/pages/index/index");
            defaultBanner.put("sort", 1);
            return Arrays.asList(defaultBanner);
        }
        return new ArrayList<>(bannerImages);
    }

    @Override
    public String getDefaultImage(String type) {
        String baseUrl = domain + "/api";
        switch (type.toLowerCase()) {
            case "product":
                return baseUrl + "/images/default-product.svg";
            case "category":
                return baseUrl + "/images/tab/category.png";
            case "avatar":
                return baseUrl + "/images/default-avatar.svg";
            case "banner":
                return baseUrl + "/images/logo.svg";
            default:
                return baseUrl + "/images/default-product.svg";
        }
    }

    @Override
    public void saveProductImages(Long productId, List<String> imageUrls) {
        if (productId != null && imageUrls != null) {
            productImages.put(productId, new ArrayList<>(imageUrls));
            // 保存商品图片
        }
    }

    @Override
    public void deleteProductImages(Long productId) {
        if (productId != null) {
            productImages.remove(productId);
            // 删除商品图片
        }
    }

    @Override
    public void updateUserAvatar(Long userId, String avatarUrl) {
        if (userId != null && avatarUrl != null) {
            userAvatars.put(userId, avatarUrl);
            // 更新用户头像
        }
    }

    @Override
    public boolean isValidImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return false;
        }
        
        // 检查是否为有效的图片URL格式
        String lowerUrl = imageUrl.toLowerCase();
        return lowerUrl.startsWith("http://") || lowerUrl.startsWith("https://") ||
               lowerUrl.startsWith("/uploads/") || lowerUrl.startsWith("/images/");
    }

    @Override
    public String compressImage(String originalUrl, int width, int height) {
        // 简单的图片压缩URL生成（实际项目中可以集成图片处理服务）
        if (originalUrl == null || originalUrl.isEmpty()) {
            return originalUrl;
        }
        
        // 如果是外部URL，直接返回
        if (originalUrl.startsWith("http://") || originalUrl.startsWith("https://")) {
            return originalUrl;
        }
        
        // 为本地图片添加压缩参数
        String separator = originalUrl.contains("?") ? "&" : "?";
        return originalUrl + separator + "w=" + width + "&h=" + height + "&compress=true";
    }

    /**
     * 初始化示例数据
     */
    private void initSampleData() {
        String baseUrl = domain + "/api";
        
        // 初始化商品图片
        productImages.put(1L, Arrays.asList(
            baseUrl + "/images/default-product.svg",
            baseUrl + "/images/tab/food.png"
        ));
        productImages.put(2L, Arrays.asList(
            baseUrl + "/images/default-product.svg"
        ));
        
        // 初始化分类图片
        categoryImages.put(1L, baseUrl + "/images/tab/food.png");
        categoryImages.put(2L, baseUrl + "/images/tab/shopping.png");
        categoryImages.put(3L, baseUrl + "/images/tab/house.png");
        
        // 初始化轮播图
        Map<String, Object> banner1 = new HashMap<>();
        banner1.put("id", 1L);
        banner1.put("title", "新鲜蔬果，每日直送");
        banner1.put("imageUrl", baseUrl + "/images/logo.svg");
        banner1.put("linkUrl", "/pages/product/list?categoryId=1");
        banner1.put("sort", 1);
        bannerImages.add(banner1);
        
        Map<String, Object> banner2 = new HashMap<>();
        banner2.put("id", 2L);
        banner2.put("title", "优质服务，贴心配送");
        banner2.put("imageUrl", baseUrl + "/images/tab/transport.png");
        banner2.put("linkUrl", "/pages/order/list");
        banner2.put("sort", 2);
        bannerImages.add(banner2);
        
        // 图片服务初始化完成
    }
}