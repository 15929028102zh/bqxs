package com.biangqiang.freshdelivery.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.biangqiang.freshdelivery.dto.ProductQueryDTO;
import com.biangqiang.freshdelivery.entity.Product;
import com.biangqiang.freshdelivery.entity.OrderItem;
import com.biangqiang.freshdelivery.mapper.ProductMapper;
import com.biangqiang.freshdelivery.mapper.OrderItemMapper;
import com.biangqiang.freshdelivery.service.ProductService;
import com.biangqiang.freshdelivery.config.HotProductsConfig;
import com.biangqiang.freshdelivery.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    
    private final ProductMapper productMapper;
    private final OrderItemMapper orderItemMapper;
    private final HotProductsConfig hotProductsConfig;
    
    @Override
    public Page<ProductVO> getProductList(ProductQueryDTO queryDTO) {
        
        // 1. 构建查询条件
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        
        // 商品名称模糊查询
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            queryWrapper.like(Product::getName, queryDTO.getKeyword());
        }
        if (StringUtils.hasText(queryDTO.getName())) {
            queryWrapper.like(Product::getName, queryDTO.getName());
        }
        
        // 分类查询
        if (queryDTO.getCategoryId() != null) {
            queryWrapper.eq(Product::getCategoryId, queryDTO.getCategoryId());
        }
        
        // 价格区间查询
        if (queryDTO.getMinPrice() != null) {
            queryWrapper.ge(Product::getPrice, queryDTO.getMinPrice());
        }
        if (queryDTO.getMaxPrice() != null) {
            queryWrapper.le(Product::getPrice, queryDTO.getMaxPrice());
        }
        
        // 状态查询（如果指定了状态）
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(Product::getStatus, queryDTO.getStatus());
        }
        queryWrapper.eq(Product::getDeleted, 0);
        
        // 排序：默认按创建时间倒序
        queryWrapper.orderByDesc(Product::getCreateTime);
        
        // 2. 分页查询
        Page<Product> productPage = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        IPage<Product> result = productMapper.selectPage(productPage, queryWrapper);
        
        // 3. 转换为VO
        List<ProductVO> productVOList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        // 4. 构建返回结果
        Page<ProductVO> voPage = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        voPage.setRecords(productVOList);
        voPage.setTotal(result.getTotal());
        voPage.setPages(result.getPages());
        
        return voPage;
    }
    
    @Override
    public ProductVO getProductDetail(Long id) {
        
        // 1. 查询商品信息
        Product product = productMapper.selectById(id);
        if (product == null) {

            throw new RuntimeException("商品不存在");
        }
        
        // 2. 检查商品状态
        
        if (product.getStatus() != 1) {
            throw new RuntimeException("商品已下架");
        }
        
        if (product.getDeleted() == 1) {
            throw new RuntimeException("商品不存在");
        }
        
        // 3. 转换为VO并返回
        ProductVO productVO = convertToVO(product);
        
        return productVO;
    }
    
    @Override
    public Page<ProductVO> searchProducts(String keyword, Integer page, Integer size) {
        
        if (!StringUtils.hasText(keyword)) {
            return new Page<>(page, size);
        }
        
        // 1. 构建搜索条件
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> 
            wrapper.like(Product::getName, keyword)
                   .or()
                   .like(Product::getDescription, keyword)
        );
        
        // 只查询上架的商品
        queryWrapper.eq(Product::getStatus, 1);
        queryWrapper.eq(Product::getDeleted, 0);
        
        // 按销量和创建时间排序
        queryWrapper.orderByDesc(Product::getSales)
                   .orderByDesc(Product::getCreateTime);
        
        // 2. 分页查询
        Page<Product> productPage = new Page<>(page, size);
        IPage<Product> result = productMapper.selectPage(productPage, queryWrapper);
        
        // 3. 转换为VO
        List<ProductVO> productVOList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        // 4. 构建返回结果
        Page<ProductVO> voPage = new Page<>(page, size);
        voPage.setRecords(productVOList);
        voPage.setTotal(result.getTotal());
        voPage.setPages(result.getPages());
        
        return voPage;
    }
    
    @Override
    public List<ProductVO> getRecommendProducts(Integer limit) {
        
        // 1. 构建查询条件 - 获取推荐商品（这里简化为销量高的商品）
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        
        // 只查询上架的商品
        queryWrapper.eq(Product::getStatus, 1);
        queryWrapper.eq(Product::getDeleted, 0);
        
        // 按销量倒序，取前limit个
        queryWrapper.orderByDesc(Product::getSales)
                   .orderByDesc(Product::getCreateTime);
        queryWrapper.last("LIMIT " + limit);
        
        // 2. 执行查询
        List<Product> products = productMapper.selectList(queryWrapper);
        
        // 3. 转换为VO
        List<ProductVO> result = products.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return result;
    }
    
    /**
     * 根据配置的排序规则比较两个商品
     * @param a 商品A
     * @param b 商品B
     * @return 比较结果
     */
    private int compareProducts(ProductVO a, ProductVO b) {
        HotProductsConfig.SortRule sortRule = hotProductsConfig.getSortRuleEnum();
        
        switch (sortRule) {
            case SALES_DESC:
                // 按销量降序
                int salesCompare = Integer.compare(b.getSales(), a.getSales());
                if (salesCompare != 0) return salesCompare;
                return b.getCreateTime().compareTo(a.getCreateTime());
                
            case REVENUE_DESC:
                // 按销售额降序（需要获取销售额数据）
                Map<String, Object> salesDataA = getProductSalesData(a.getId());
                Map<String, Object> salesDataB = getProductSalesData(b.getId());
                BigDecimal revenueA = (BigDecimal) salesDataA.get("total_revenue");
                BigDecimal revenueB = (BigDecimal) salesDataB.get("total_revenue");
                
                int revenueCompare = revenueB.compareTo(revenueA);
                if (revenueCompare != 0) return revenueCompare;
                return b.getCreateTime().compareTo(a.getCreateTime());
                
            case MIXED:
            default:
                // 综合排序：销量优先，销售额次之，最后按创建时间
                int mixedSalesCompare = Integer.compare(b.getSales(), a.getSales());
                if (mixedSalesCompare != 0) return mixedSalesCompare;
                
                Map<String, Object> mixedSalesDataA = getProductSalesData(a.getId());
                Map<String, Object> mixedSalesDataB = getProductSalesData(b.getId());
                BigDecimal mixedRevenueA = (BigDecimal) mixedSalesDataA.get("total_revenue");
                BigDecimal mixedRevenueB = (BigDecimal) mixedSalesDataB.get("total_revenue");
                
                int mixedRevenueCompare = mixedRevenueB.compareTo(mixedRevenueA);
                if (mixedRevenueCompare != 0) return mixedRevenueCompare;
                
                return b.getCreateTime().compareTo(a.getCreateTime());
        }
    }
    
    @Override
    public List<ProductVO> getHotProducts(Integer limit) {
        // 简化实现，先返回基本的商品列表
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getStatus, 1);
        queryWrapper.eq(Product::getDeleted, 0);
        queryWrapper.orderByDesc(Product::getSales);
        queryWrapper.orderByDesc(Product::getCreateTime);
        queryWrapper.last("LIMIT " + limit);
        
        List<Product> products = productMapper.selectList(queryWrapper);
        
        return products.stream()
                .map(product -> {
                    ProductVO vo = convertToVO(product);
                    // 计算销售额：销量 × 价格
                    BigDecimal revenue = BigDecimal.valueOf(product.getSales() != null ? product.getSales() : 0)
                            .multiply(product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO);
                    vo.setRevenue(revenue);
                    return vo;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 获取商品的实际销售数据
     * @param productId 商品ID
     * @return 包含销量、销售额、订单数的Map
     */
    private Map<String, Object> getProductSalesData(Long productId) {
        // 通过OrderItemMapper查询商品的真实销售数据
        Map<String, Object> salesStats = orderItemMapper.getProductSalesStats(productId);
        
        // 确保返回的数据类型正确
        Map<String, Object> result = new HashMap<>();
        
        // 处理可能的null值和数据类型转换
        Object totalQuantity = salesStats.get("total_quantity");
        Object totalRevenue = salesStats.get("total_revenue");
        Object orderCount = salesStats.get("order_count");
        
        // 转换为正确的数据类型
        result.put("total_quantity", totalQuantity != null ? 
            (totalQuantity instanceof Long ? ((Long) totalQuantity).intValue() : (Integer) totalQuantity) : 0);
        
        result.put("total_revenue", totalRevenue != null ? 
            (totalRevenue instanceof BigDecimal ? (BigDecimal) totalRevenue : new BigDecimal(totalRevenue.toString())) : BigDecimal.ZERO);
        
        result.put("order_count", orderCount != null ? 
            (orderCount instanceof Long ? ((Long) orderCount).intValue() : (Integer) orderCount) : 0);
        
        return result;
    }
    
    @Override
    public Page<ProductVO> getProductsByCategory(Long categoryId, Integer page, Integer size) {
        
        // 1. 构建查询条件
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        
        // 按分类查询
        queryWrapper.eq(Product::getCategoryId, categoryId);
        
        // 只查询上架的商品
        queryWrapper.eq(Product::getStatus, 1);
        queryWrapper.eq(Product::getDeleted, 0);
        
        // 按销量和创建时间排序
        queryWrapper.orderByDesc(Product::getSales)
                   .orderByDesc(Product::getCreateTime);
        
        // 2. 分页查询
        Page<Product> productPage = new Page<>(page, size);
        IPage<Product> result = productMapper.selectPage(productPage, queryWrapper);
        
        // 3. 转换为VO
        List<ProductVO> productVOList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        // 4. 构建返回结果
        Page<ProductVO> voPage = new Page<>(page, size);
        voPage.setRecords(productVOList);
        voPage.setTotal(result.getTotal());
        voPage.setPages(result.getPages());
        
        return voPage;
    }
    

    
    /**
     * 创建模拟商品数据
     */
    private List<ProductVO> createMockProducts() {
        List<ProductVO> products = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            ProductVO product = createMockProduct();
            product.setId((long) i);
            product.setName("商品" + i);
            products.add(product);
        }
        
        return products;
    }
    
    /**
     * 创建单个模拟商品
     */
    private ProductVO createMockProduct() {
        ProductVO product = new ProductVO();
        product.setId(1L);
        product.setName("新鲜白菜");
        product.setDescription("新鲜优质大白菜，口感清脆甜美");
        product.setImages(Arrays.asList("/images/products/baicai1.jpg", "/images/products/baicai2.jpg"));
        product.setPrice(new BigDecimal("3.50"));
        product.setOriginalPrice(new BigDecimal("4.00"));
        product.setSpecification("500g/份");
        product.setOrigin("山东");
        product.setCategoryId(3L);
        // 设置分类ID而不是分类名称
        product.setCategoryId(3L);
        product.setStock(100);
        product.setSales(50);
        product.setIsRecommend(1);
        product.setStatus(1);
        product.setCreateTime(LocalDateTime.now());
        
        return product;
    }
    
    @Override
    public ProductVO createProduct(Product product) {
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        productMapper.insert(product);
        return convertToVO(product);
    }
    
    @Override
    public ProductVO updateProduct(Long id, Product product) {
        product.setId(id);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        return convertToVO(product);
    }
    
    @Override
    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
    }
    
    @Override
    public void updateProductStatus(Long id, Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
    }
    
    @Override
    public void batchDeleteProducts(List<Long> ids) {
        productMapper.deleteBatchIds(ids);
    }
    
    /**
     * 转换为VO对象
     */
    private ProductVO convertToVO(Product product) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);
        // 处理图片字段
        if (StringUtils.hasText(product.getImages())) {
            List<String> imageList = Arrays.asList(product.getImages().split(","));
            vo.setImages(imageList);
            // 设置主图（第一张图片）用于前端兼容
            if (!imageList.isEmpty()) {
                vo.setImage(imageList.get(0));
            }
        }
        return vo;
    }
    
    // 统计相关方法实现
    
    @Override
    public Long getTotalProductCount() {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getDeleted, 0); // 只统计未删除的商品
        return productMapper.selectCount(queryWrapper);
    }
    
    @Override
    public List<Map<String, Object>> getHotProductsStats(Integer limit) {
        
        // 1. 构建查询条件 - 获取热门商品（按销量排序）
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        
        // 只查询上架的商品
        queryWrapper.eq(Product::getStatus, 1);
        queryWrapper.eq(Product::getDeleted, 0);
        
        // 销量大于0的商品才算热门
        queryWrapper.gt(Product::getSales, 0);
        
        // 按销量倒序，取前limit个
        queryWrapper.orderByDesc(Product::getSales)
                   .orderByDesc(Product::getCreateTime);
        queryWrapper.last("LIMIT " + limit);
        
        // 2. 执行查询
        List<Product> products = productMapper.selectList(queryWrapper);
        
        // 3. 转换为统计格式
        return products.stream().map(product -> {
            Map<String, Object> productMap = new HashMap<>();
            productMap.put("id", product.getId());
            productMap.put("name", product.getName());
            productMap.put("price", product.getPrice());
            productMap.put("sales", product.getSales());
            productMap.put("stock", product.getStock());
            
            // 计算销售额（销量 × 价格）
            BigDecimal revenue = BigDecimal.ZERO;
            if (product.getSales() != null && product.getPrice() != null) {
                revenue = product.getPrice().multiply(new BigDecimal(product.getSales()));
            }
            productMap.put("revenue", revenue);
            
            // 根据categoryId设置分类名称
            String categoryName = getCategoryNameById(product.getCategoryId());
            productMap.put("categoryName", categoryName);
            // 处理图片
            if (StringUtils.hasText(product.getImages())) {
                String[] imageArray = product.getImages().split(",");
                productMap.put("image", imageArray.length > 0 ? imageArray[0] : "");
            } else {
                productMap.put("image", "");
            }
            return productMap;
        }).collect(Collectors.toList());
    }
    
    @Override
    public List<Map<String, Object>> getCategoryStats() {
        
        // 这里简化处理，实际应该通过SQL GROUP BY查询
        List<Map<String, Object>> categoryStats = new ArrayList<>();
        
        // 模拟分类统计数据
        Map<String, Object> category1 = new HashMap<>();
        category1.put("categoryId", 1L);
        category1.put("categoryName", "水果类");
        category1.put("productCount", getTotalProductCountByCategory(1L));
        category1.put("totalSales", getTotalSalesByCategory(1L));
        categoryStats.add(category1);
        
        Map<String, Object> category2 = new HashMap<>();
        category2.put("categoryId", 2L);
        category2.put("categoryName", "蔬菜类");
        category2.put("productCount", getTotalProductCountByCategory(2L));
        category2.put("totalSales", getTotalSalesByCategory(2L));
        categoryStats.add(category2);
        
        Map<String, Object> category3 = new HashMap<>();
        category3.put("categoryId", 3L);
        category3.put("categoryName", "叶菜类");
        category3.put("productCount", getTotalProductCountByCategory(3L));
        category3.put("totalSales", getTotalSalesByCategory(3L));
        categoryStats.add(category3);
        
        return categoryStats;
    }
    
    @Override
    public List<Map<String, Object>> getLowStockProducts(Integer threshold) {
        
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getStatus, 1) // 只查询上架商品
                   .eq(Product::getDeleted, 0) // 未删除
                   .le(Product::getStock, threshold) // 库存小于等于阈值
                   .orderByAsc(Product::getStock); // 按库存升序
        
        List<Product> products = productMapper.selectList(queryWrapper);
        
        return products.stream().map(product -> {
            Map<String, Object> productMap = new HashMap<>();
            productMap.put("id", product.getId());
            productMap.put("name", product.getName());
            productMap.put("stock", product.getStock());
            productMap.put("price", product.getPrice());
            // 根据categoryId设置分类名称
            String categoryName = getCategoryNameById(product.getCategoryId());
            productMap.put("categoryName", categoryName);
            productMap.put("status", product.getStock() == 0 ? "缺货" : "库存不足");
            return productMap;
        }).collect(Collectors.toList());
    }
    
    /**
     * 根据分类获取商品数量
     */
    private Long getTotalProductCountByCategory(Long categoryId) {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getCategoryId, categoryId)
                   .eq(Product::getDeleted, 0);
        return productMapper.selectCount(queryWrapper);
    }
    
    /**
     * 根据分类获取总销量
     */
    private Long getTotalSalesByCategory(Long categoryId) {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getCategoryId, categoryId)
                   .eq(Product::getDeleted, 0)
                   .select(Product::getSales);
        
        List<Product> products = productMapper.selectList(queryWrapper);
        return products.stream()
                .mapToLong(product -> product.getSales() != null ? product.getSales() : 0L)
                .sum();
    }
    
    /**
     * 根据分类ID获取分类名称
     */
    private String getCategoryNameById(Long categoryId) {
        if (categoryId == null) {
            return "未分类";
        }
        
        // 简化处理，实际应该查询分类表
        switch (categoryId.intValue()) {
            case 1:
                return "水果类";
            case 2:
                return "蔬菜类";
            case 3:
                return "叶菜类";
            case 4:
                return "肉类";
            case 5:
                return "海鲜类";
            default:
                return "其他类";
        }
    }
    
    @Override
    public boolean updateStock(Long productId, Integer quantity) {
        if (productId == null || quantity == null) {
            return false;
        }
        
        // 查询当前商品信息
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted() == 1) {
            return false;
        }
        
        // 计算新库存
        Integer currentStock = product.getStock() != null ? product.getStock() : 0;
        Integer newStock = currentStock - quantity; // quantity为正数表示扣减
        
        // 检查库存是否足够
        if (newStock < 0) {
            return false;
        }
        
        // 更新库存
        Product updateProduct = new Product();
        updateProduct.setId(productId);
        updateProduct.setStock(newStock);
        updateProduct.setUpdateTime(LocalDateTime.now());
        
        return productMapper.updateById(updateProduct) > 0;
    }
    
    @Override
    public boolean batchUpdateStock(List<Map<String, Object>> stockUpdates) {
        if (stockUpdates == null || stockUpdates.isEmpty()) {
            return true;
        }
        
        try {
            for (Map<String, Object> update : stockUpdates) {
                Long productId = Long.valueOf(update.get("productId").toString());
                Integer quantity = Integer.valueOf(update.get("quantity").toString());
                
                if (!updateStock(productId, quantity)) {
                    return false; // 如果任何一个更新失败，返回false
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}