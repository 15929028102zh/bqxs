package com.biangqiang.freshdelivery.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.biangqiang.freshdelivery.common.Result;
import com.biangqiang.freshdelivery.dto.CategoryQueryDTO;
import com.biangqiang.freshdelivery.entity.Category;
import com.biangqiang.freshdelivery.service.CategoryService;
import com.biangqiang.freshdelivery.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理控制器
 */
@Tag(name = "分类管理", description = "商品分类管理相关接口")
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "分页查询分类列表")
    @GetMapping
    public Result<IPage<CategoryVO>> getCategoryList(CategoryQueryDTO queryDTO) {
        IPage<CategoryVO> page = categoryService.getCategoryList(queryDTO);
        return Result.success(page);
    }

    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    public Result<CategoryVO> getCategoryDetail(@PathVariable Long id) {
        CategoryVO categoryVO = categoryService.getCategoryDetail(id);
        if (categoryVO == null) {
            return Result.error("分类不存在");
        }
        return Result.success(categoryVO);
    }

    @Operation(summary = "创建分类")
    @PostMapping
    public Result<Void> createCategory(@RequestBody Category category) {
        boolean success = categoryService.createCategory(category);
        if (success) {
            return Result.success();
        }
        return Result.error("创建分类失败");
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        boolean success = categoryService.updateCategory(category);
        if (success) {
            return Result.success();
        }
        return Result.error("更新分类失败");
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        try {
            boolean success = categoryService.deleteCategory(id);
            if (success) {
                return Result.success();
            }
            return Result.error("删除分类失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "更新分类状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateCategoryStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean success = categoryService.updateCategoryStatus(id, status);
        if (success) {
            return Result.success();
        }
        return Result.error("更新分类状态失败");
    }

    @Operation(summary = "获取分类树形结构")
    @GetMapping("/tree")
    public Result<List<CategoryVO>> getCategoryTree() {
        List<CategoryVO> tree = categoryService.getCategoryTree();
        return Result.success(tree);
    }

    @Operation(summary = "根据父分类ID获取子分类列表")
    @GetMapping("/children/{parentId}")
    public Result<List<CategoryVO>> getCategoriesByParentId(@PathVariable Long parentId) {
        List<CategoryVO> categories = categoryService.getCategoriesByParentId(parentId);
        return Result.success(categories);
    }
}