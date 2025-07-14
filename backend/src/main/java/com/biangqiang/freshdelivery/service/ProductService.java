package com.biangqiang.freshdelivery.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.biangqiang.freshdelivery.dto.ProductQueryDTO;
import com.biangqiang.freshdelivery.entity.Product;
import com.biangqiang.freshdelivery.vo.ProductVO;
import java.util.List;

/**
 * 商品服务接口
 *
 * @author biangqiang
 * @since 2024-01-01
 */
public interface ProductService {
    
    /**
     * 获取商品列表
     *
     * @param queryDTO 查询条件
     * @return 商品分页列表
     */
    Page<ProductVO> getProductList(ProductQueryDTO queryDTO);
    
    /**
     * 获取商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     */
    ProductVO getProductDetail(Long id);
    
    /**
     * 搜索商品
     *
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页数量
     * @return 商品分页列表
     */
    Page<ProductVO> searchProducts(String keyword, Integer page, Integer size);
    
    /**
     * 获取推荐商品
     *
     * @param limit 数量限制
     * @return 推荐商品列表
     */
    List<ProductVO> getRecommendProducts(Integer limit);
    
    /**
     * 获取热销商品
     *
     * @param limit 数量限制
     * @return 热销商品列表
     */
    List<ProductVO> getHotProducts(Integer limit);
    
    /**
     * 根据分类获取商品
     *
     * @param categoryId 分类ID
     * @param page 页码
     * @param size 每页数量
     * @return 商品分页列表
     */
    Page<ProductVO> getProductsByCategory(Long categoryId, Integer page, Integer size);
    
    /**
     * 创建商品
     *
     * @param product 商品信息
     * @return 创建的商品
     */
    ProductVO createProduct(Product product);
    
    /**
     * 更新商品
     *
     * @param id 商品ID
     * @param product 商品信息
     * @return 更新后的商品
     */
    ProductVO updateProduct(Long id, Product product);
    
    /**
     * 删除商品
     *
     * @param id 商品ID
     */
    void deleteProduct(Long id);
    
    /**
     * 更新商品状态
     *
     * @param id 商品ID
     * @param status 状态
     */
    void updateProductStatus(Long id, Integer status);
    
    /**
     * 批量删除商品
     *
     * @param ids 商品ID列表
     */
    void batchDeleteProducts(List<Long> ids);
    
    // 统计相关方法
    
    /**
     * 获取商品总数
     *
     * @return 商品总数
     */
    Long getTotalProductCount();
    
    /**
     * 获取热门商品统计信息
     *
     * @param limit 数量限制
     * @return 热门商品统计列表
     */
    List<java.util.Map<String, Object>> getHotProductsStats(Integer limit);
    
    /**
     * 获取商品分类统计
     *
     * @return 分类统计列表
     */
    List<java.util.Map<String, Object>> getCategoryStats();
    
    /**
     * 获取库存预警商品
     *
     * @param threshold 库存阈值
     * @return 低库存商品列表
     */
    List<java.util.Map<String, Object>> getLowStockProducts(Integer threshold);
    
    /**
     * 更新商品库存
     *
     * @param productId 商品ID
     * @param quantity 扣减数量（正数为扣减，负数为增加）
     * @return 是否更新成功
     */
    boolean updateStock(Long productId, Integer quantity);
    
    /**
     * 批量更新商品库存
     *
     * @param stockUpdates 库存更新列表，Map包含productId和quantity
     * @return 是否全部更新成功
     */
    boolean batchUpdateStock(List<java.util.Map<String, Object>> stockUpdates);
}