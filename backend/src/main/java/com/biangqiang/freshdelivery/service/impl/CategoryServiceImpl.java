package com.biangqiang.freshdelivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.biangqiang.freshdelivery.dto.CategoryQueryDTO;
import com.biangqiang.freshdelivery.entity.Category;
import com.biangqiang.freshdelivery.mapper.CategoryMapper;
import com.biangqiang.freshdelivery.service.CategoryService;
import com.biangqiang.freshdelivery.vo.CategoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品分类服务实现类
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public IPage<CategoryVO> getCategoryList(CategoryQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(queryDTO.getName()), Category::getName, queryDTO.getName())
                .eq(queryDTO.getParentId() != null, Category::getParentId, queryDTO.getParentId())
                .eq(queryDTO.getStatus() != null, Category::getStatus, queryDTO.getStatus())
                .orderByAsc(Category::getParentId)
                .orderByAsc(Category::getSortOrder);

        // 分页查询
        Page<Category> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        IPage<Category> categoryPage = this.page(page, queryWrapper);

        // 转换为VO
        IPage<CategoryVO> voPage = new Page<>();
        BeanUtils.copyProperties(categoryPage, voPage);
        
        List<CategoryVO> voList = categoryPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public CategoryVO getCategoryDetail(Long id) {
        Category category = this.getById(id);
        if (category == null) {
            return null;
        }
        return convertToVO(category);
    }

    @Override
    public boolean createCategory(Category category) {
        // 设置默认值
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        return this.save(category);
    }

    @Override
    public boolean updateCategory(Category category) {
        return this.updateById(category);
    }

    @Override
    public boolean deleteCategory(Long id) {
        // 检查是否有子分类
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getParentId, id);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new RuntimeException("该分类下存在子分类，无法删除");
        }
        
        // TODO: 检查是否有商品使用该分类
        
        return this.removeById(id);
    }

    @Override
    public boolean updateCategoryStatus(Long id, Integer status) {
        Category category = new Category();
        category.setId(id);
        category.setStatus(status);
        return this.updateById(category);
    }

    @Override
    public List<CategoryVO> getCategoryTree() {
        // 获取所有分类
        List<Category> allCategories = baseMapper.selectAllForTree();
        
        // 转换为VO
        List<CategoryVO> allCategoryVOs = allCategories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        // 构建树形结构
        return buildCategoryTree(allCategoryVOs, 0L);
    }

    @Override
    public List<CategoryVO> getCategoriesByParentId(Long parentId) {
        List<Category> categories = baseMapper.selectByParentId(parentId);
        return categories.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 构建分类树形结构
     */
    private List<CategoryVO> buildCategoryTree(List<CategoryVO> allCategories, Long parentId) {
        List<CategoryVO> result = new ArrayList<>();
        
        for (CategoryVO category : allCategories) {
            if (parentId.equals(category.getParentId())) {
                // 递归查找子分类
                List<CategoryVO> children = buildCategoryTree(allCategories, category.getId());
                category.setChildren(children);
                result.add(category);
            }
        }
        
        return result;
    }

    /**
     * 转换为VO对象
     */
    private CategoryVO convertToVO(Category category) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        
        // 设置父分类名称
        if (category.getParentId() != null && category.getParentId() > 0) {
            Category parentCategory = this.getById(category.getParentId());
            if (parentCategory != null) {
                vo.setParentName(parentCategory.getName());
            }
        }
        
        return vo;
    }
}