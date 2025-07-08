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
}