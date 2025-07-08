package com.biangqiang.freshdelivery.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_product")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品名称
     */
    @TableField("name")
    private String name;

    /**
     * 商品描述
     */
    @TableField("description")
    private String description;

    /**
     * 商品图片（多张图片用逗号分隔）
     */
    @TableField("images")
    @JsonProperty("image")  // 前端使用 image 字段名
    private String images;

    /**
     * 商品价格
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 商品原价
     */
    @TableField("original_price")
    private BigDecimal originalPrice;

    /**
     * 商品规格
     */
    @TableField("specification")
    private String specification;

    /**
     * 商品产地
     */
    @TableField("origin")
    private String origin;

    /**
     * 商品分类ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 库存数量
     */
    @TableField("stock")
    private Integer stock;

    /**
     * 销量
     */
    @TableField("sales")
    private Integer sales;

    /**
     * 商品状态：0-下架，1-上架
     */
    @TableField("status")
    private Integer status;

    /**
     * 排序权重
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 是否推荐：0-否，1-是
     */
    @TableField("is_recommend")
    private Integer isRecommend;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;

    // 手动添加getter方法以解决编译问题
    public Integer getStatus() {
        return status;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public Integer getSales() {
        return sales;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getImages() {
        return images;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public String getSpecification() {
        return specification;
    }

    public String getOrigin() {
        return origin;
    }

    public Integer getStock() {
        return stock;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public Integer getIsRecommend() {
        return isRecommend;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    // 手动添加setter方法以解决编译问题
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImages(String images) {
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

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setIsRecommend(Integer isRecommend) {
        this.isRecommend = isRecommend;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}