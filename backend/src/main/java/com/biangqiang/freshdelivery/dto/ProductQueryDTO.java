package com.biangqiang.freshdelivery.dto;

import lombok.Data;

/**
 * 商品查询DTO
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Data
public class ProductQueryDTO {
    
    /**
     * 页码
     */
    private Integer page = 1;
    
    /**
     * 每页数量
     */
    private Integer size = 10;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 搜索关键词
     */
    private String keyword;
    
    /**
     * 商品名称
     */
    private String name;
    
    /**
     * 最低价格
     */
    private java.math.BigDecimal minPrice;
    
    /**
     * 最高价格
     */
    private java.math.BigDecimal maxPrice;
    
    /**
     * 商品状态：0-下架，1-上架
     */
    private Integer status;
    
    /**
     * 排序字段：price/sales/createTime
     */
    private String sortBy;
    
    /**
     * 排序方式：asc/desc
     */
    private String sortOrder = "desc";
    
    /**
     * 数量限制（用于推荐、热销等接口）
     */
    private Integer limit = 10;

    // Getter and Setter methods
    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public java.math.BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(java.math.BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public java.math.BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(java.math.BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}