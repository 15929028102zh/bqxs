package com.biangqiang.freshdelivery.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.biangqiang.freshdelivery.common.Result;
import com.biangqiang.freshdelivery.dto.ProductQueryDTO;
import com.biangqiang.freshdelivery.entity.Product;
import com.biangqiang.freshdelivery.service.ProductService;
import com.biangqiang.freshdelivery.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商品控制器
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Tag(name = "商品管理", description = "商品相关接口")
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "获取商品列表", description = "分页查询商品列表")
    @GetMapping("/list")
    public Result<Page<ProductVO>> getProductList(ProductQueryDTO queryDTO) {
        return Result.success(productService.getProductList(queryDTO));
    }

    @Operation(summary = "获取商品详情", description = "根据商品ID获取商品详细信息")
    @GetMapping("/{id}")
    public Result<ProductVO> getProductDetail(@PathVariable Long id) {
        return Result.success(productService.getProductDetail(id));
    }

    @Operation(summary = "搜索商品", description = "根据关键词搜索商品")
    @GetMapping("/search")
    public Result<Page<ProductVO>> searchProducts(@RequestParam String keyword,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(productService.searchProducts(keyword, page, size));
    }

    @Operation(summary = "获取推荐商品", description = "获取系统推荐的商品列表")
    @GetMapping("/recommend")
    public Result<List<ProductVO>> getRecommendProducts(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(productService.getRecommendProducts(limit));
    }

    @Operation(summary = "获取热销商品", description = "获取热销商品列表")
    @GetMapping("/hot")
    public Result<List<ProductVO>> getHotProducts(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(productService.getHotProducts(limit));
    }

    @Operation(summary = "按分类获取商品", description = "根据分类ID获取商品列表")
    @GetMapping("/category/{categoryId}")
    public Result<Page<ProductVO>> getProductsByCategory(@PathVariable Long categoryId,
                                                         @RequestParam(defaultValue = "1") Integer page,
                                                         @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(productService.getProductsByCategory(categoryId, page, size));
    }

    @Operation(summary = "创建商品", description = "创建新商品")
    @PostMapping
    public Result<ProductVO> createProduct(@RequestBody Product product) {
        return Result.success(productService.createProduct(product));
    }

    @Operation(summary = "更新商品", description = "更新商品信息")
    @PutMapping("/{id}")
    public Result<ProductVO> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return Result.success(productService.updateProduct(id, product));
    }

    @Operation(summary = "删除商品", description = "删除指定商品")
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success();
    }

    @Operation(summary = "更新商品状态", description = "更新商品上架/下架状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateProductStatus(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        Integer status = request.get("status");
        productService.updateProductStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "批量删除商品", description = "批量删除多个商品")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteProducts(@RequestBody List<Long> ids) {
        productService.batchDeleteProducts(ids);
        return Result.success();
    }
}