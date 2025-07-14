package com.biangqiang.freshdelivery.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品视图对象
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Data
@Schema(description = "商品信息")
public class ProductVO {
    
    /**
     * 商品ID
     */
    private Long id;
    
    /**
     * 商品名称
     */
    private String name;
    
    /**
     * 商品描述
     */
    private String description;
    
    /**
     * 商品图片列表
     */
    private List<String> images;
    
    /**
     * 商品主图（兼容前端单图字段）
     */
    @JsonProperty("image")
    private String image;
    
    /**
     * 商品价格
     */
    private BigDecimal price;
    
    /**
     * 原价
     */
    private BigDecimal originalPrice;
    
    /**
     * 规格
     */
    private String specification;
    
    /**
     * 产地
     */
    private String origin;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 库存数量
     */
    private Integer stock;
    
    /**
     * 销量
     */
    private Integer sales;

    /**
     * 销售额
     */
    @Schema(description = "销售额")
    private BigDecimal revenue;

    /**
     * 是否推荐 0-否 1-是
     */
    private Integer isRecommend;
    
    /**
     * 商品状态 0-下架 1-上架
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 商品评价列表（详情页使用）
     */@Schema(description = "商品评价列表")
    private List<ReviewVO> reviews;

    // Manual setter methods for compilation
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public void setIsRecommend(Integer isRecommend) {
        this.isRecommend = isRecommend;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setReviews(List<ReviewVO> reviews) {
        this.reviews = reviews;
    }
    
    public void setImage(String image) {
        this.image = image;
    }
     
    @Data
    @Schema(description = "商品评价信息")
    public static class ReviewVO {
        @Schema(description = "评价ID")
        private Long id;

        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "用户昵称")
        private String userNickname;

        @Schema(description = "用户头像")
        private String userAvatar;

        @Schema(description = "评价内容")
        private String content;

        @Schema(description = "评分")
        private Integer rating;

        @Schema(description = "评价图片")
        private List<String> images;

        @Schema(description = "评价时间")
        private LocalDateTime createTime;
    }
}