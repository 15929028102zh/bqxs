package com.biangqiang.freshdelivery.controller;

import com.biangqiang.freshdelivery.common.Result;
import com.biangqiang.freshdelivery.service.CategoryService;
import com.biangqiang.freshdelivery.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器（前端接口）
 */
@Tag(name = "分类接口", description = "商品分类相关接口")
@RestController("frontendCategoryController")
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "获取分类列表", description = "获取所有启用的分类列表")
    @GetMapping("/list")
    public Result<List<CategoryVO>> getCategoryList() {
        List<CategoryVO> categories = categoryService.getCategoriesByParentId(null);
        return Result.success(categories);
    }

    @Operation(summary = "获取分类树形结构", description = "获取完整的分类树形结构")
    @GetMapping("/tree")
    public Result<List<CategoryVO>> getCategoryTree() {
        List<CategoryVO> tree = categoryService.getCategoryTree();
        return Result.success(tree);
    }

    @Operation(summary = "根据父分类ID获取子分类列表", description = "获取指定父分类下的子分类列表")
    @GetMapping("/children/{parentId}")
    public Result<List<CategoryVO>> getCategoriesByParentId(@PathVariable Long parentId) {
        List<CategoryVO> categories = categoryService.getCategoriesByParentId(parentId);
        return Result.success(categories);
    }

    @Operation(summary = "获取分类详情", description = "根据分类ID获取分类详细信息")
    @GetMapping("/{id}")
    public Result<CategoryVO> getCategoryDetail(@PathVariable Long id) {
        CategoryVO categoryVO = categoryService.getCategoryDetail(id);
        if (categoryVO == null) {
            return Result.error("分类不存在");
        }
        return Result.success(categoryVO);
    }
}