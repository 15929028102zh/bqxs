package com.biangqiang.freshdelivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.biangqiang.freshdelivery.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品Mapper接口
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    
    /**
     * 根据关键词搜索商品
     *
     * @param page 分页参数
     * @param keyword 搜索关键词
     * @return 商品列表
     */
    Page<Product> searchProducts(Page<Product> page, @Param("keyword") String keyword);
    
    /**
     * 获取推荐商品
     *
     * @param limit 限制数量
     * @return 推荐商品列表
     */
    List<Product> selectRecommendProducts(@Param("limit") Integer limit);
    
    /**
     * 获取热销商品
     *
     * @param limit 限制数量
     * @return 热销商品列表
     */
    List<Product> selectHotProducts(@Param("limit") Integer limit);
    
    /**
     * 根据分类ID获取商品
     *
     * @param page 分页参数
     * @param categoryId 分类ID
     * @return 商品列表
     */
    Page<Product> selectByCategory(Page<Product> page, @Param("categoryId") Long categoryId);
    
    /**
     * 更新商品销量
     *
     * @param productId 商品ID
     * @param quantity 销售数量
     */
    void updateSales(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}