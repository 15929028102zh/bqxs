package com.biangqiang.freshdelivery.dto;

import lombok.Data;

/**
 * 分类查询DTO
 */
@Data
public class CategoryQueryDTO {

    /**
     * 分类名称（模糊查询）
     */
    private String name;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;

    // Getter and Setter methods
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

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
}