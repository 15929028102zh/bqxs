package com.biangqiang.freshdelivery.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.biangqiang.freshdelivery.dto.CategoryQueryDTO;
import com.biangqiang.freshdelivery.entity.Category;
import com.biangqiang.freshdelivery.vo.CategoryVO;

import java.util.List;

/**
 * 商品分类服务接口
 */
public interface CategoryService extends IService<Category> {

    /**
     * 分页查询分类列表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<CategoryVO> getCategoryList(CategoryQueryDTO queryDTO);

    /**
     * 获取分类详情
     * @param id 分类ID
     * @return 分类详情
     */
    CategoryVO getCategoryDetail(Long id);

    /**
     * 创建分类
     * @param category 分类信息
     * @return 是否成功
     */
    boolean createCategory(Category category);

    /**
     * 更新分类
     * @param category 分类信息
     * @return 是否成功
     */
    boolean updateCategory(Category category);

    /**
     * 删除分类
     * @param id 分类ID
     * @return 是否成功
     */
    boolean deleteCategory(Long id);

    /**
     * 更新分类状态
     * @param id 分类ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateCategoryStatus(Long id, Integer status);

    /**
     * 获取分类树形结构
     * @return 分类树
     */
    List<CategoryVO> getCategoryTree();

    /**
     * 根据父分类ID获取子分类列表
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<CategoryVO> getCategoriesByParentId(Long parentId);
}