package com.biangqiang.freshdelivery.service;

import java.util.List;
import java.util.Map;

/**
 * 图片服务接口
 *
 * @author biangqiang
 * @since 2024-01-01
 */
public interface ImageService {

    /**
     * 获取商品图片列表
     *
     * @param productId 商品ID
     * @return 图片URL列表
     */
    List<String> getProductImages(Long productId);

    /**
     * 获取分类图片
     *
     * @param categoryId 分类ID
     * @return 图片URL
     */
    String getCategoryImage(Long categoryId);

    /**
     * 获取用户头像
     *
     * @param userId 用户ID
     * @return 头像URL
     */
    String getUserAvatar(Long userId);

    /**
     * 获取轮播图列表
     *
     * @return 轮播图URL列表
     */
    List<Map<String, Object>> getBannerImages();

    /**
     * 获取默认图片
     *
     * @param type 图片类型（product, category, avatar, banner）
     * @return 默认图片URL
     */
    String getDefaultImage(String type);

    /**
     * 保存商品图片关联
     *
     * @param productId 商品ID
     * @param imageUrls 图片URL列表
     */
    void saveProductImages(Long productId, List<String> imageUrls);

    /**
     * 删除商品图片
     *
     * @param productId 商品ID
     */
    void deleteProductImages(Long productId);

    /**
     * 更新用户头像
     *
     * @param userId 用户ID
     * @param avatarUrl 头像URL
     */
    void updateUserAvatar(Long userId, String avatarUrl);

    /**
     * 验证图片URL是否有效
     *
     * @param imageUrl 图片URL
     * @return 是否有效
     */
    boolean isValidImageUrl(String imageUrl);

    /**
     * 压缩图片
     *
     * @param originalUrl 原图URL
     * @param width 目标宽度
     * @param height 目标高度
     * @return 压缩后的图片URL
     */
    String compressImage(String originalUrl, int width, int height);
}