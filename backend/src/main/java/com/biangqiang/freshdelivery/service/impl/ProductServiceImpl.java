package com.biangqiang.freshdelivery.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.biangqiang.freshdelivery.dto.ProductQueryDTO;
import com.biangqiang.freshdelivery.entity.Product;
import com.biangqiang.freshdelivery.mapper.ProductMapper;
import com.biangqiang.freshdelivery.service.ProductService;
import com.biangqiang.freshdelivery.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    
    // 手动添加log字段以解决编译问题
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProductServiceImpl.class);
    
    private final ProductMapper productMapper;
    
    @Override
    public Page<ProductVO> getProductList(ProductQueryDTO queryDTO) {
        log.info("查询商品列表: {}", queryDTO);
        
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
        
        log.info("查询商品列表完成: 共{}条记录", result.getTotal());
        return voPage;
    }
    
    @Override
    public ProductVO getProductDetail(Long id) {
        log.info("获取商品详情: {}", id);
        
        // 1. 查询商品信息
        Product product = productMapper.selectById(id);
        if (product == null) {

            log.error("商品不存在: id={}", id);
            throw new RuntimeException("商品不存在");
        }
        
        // 2. 记录商品状态信息
        log.info("商品状态信息: id={}, status={}, deleted={}, name={}", 
                product.getId(), product.getStatus(), product.getDeleted(), product.getName());
        
        // 检查商品状态
        if (product.getStatus() != 1) {
            log.error("商品已下架: id={}, status={}", id, product.getStatus());
            throw new RuntimeException("商品已下架");
        }
        
        if (product.getDeleted() == 1) {
            log.error("商品已删除: id={}, deleted={}", id, product.getDeleted());
            throw new RuntimeException("商品不存在");
        }
        
        // 3. 转换为VO并返回
        ProductVO productVO = convertToVO(product);
        
        log.info("获取商品详情成功: productId={}, name={}", product.getId(), product.getName());
        return productVO;
    }
    
    @Override
    public Page<ProductVO> searchProducts(String keyword, Integer page, Integer size) {
        log.info("搜索商品: keyword={}, page={}, size={}", keyword, page, size);
        
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
        
        log.info("搜索商品完成: keyword={}, 找到{}个结果", keyword, result.getTotal());
        return voPage;
    }
    
    @Override
    public List<ProductVO> getRecommendProducts(Integer limit) {
        log.info("获取推荐商品: limit={}", limit);
        
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
        
        log.info("获取推荐商品完成: 共{}个商品", result.size());
        return result;
    }
    
    @Override
    public List<ProductVO> getHotProducts(Integer limit) {
        log.info("获取热销商品: limit={}", limit);
        
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
        
        // 3. 转换为VO
        List<ProductVO> result = products.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        log.info("获取热门商品完成: 共{}个商品", result.size());
        return result;
    }
    
    @Override
    public Page<ProductVO> getProductsByCategory(Long categoryId, Integer page, Integer size) {
        log.info("根据分类获取商品: categoryId={}, page={}, size={}", categoryId, page, size);
        
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
        
        log.info("根据分类获取商品完成: categoryId={}, 共{}个商品", categoryId, result.getTotal());
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
        product.setCategoryName("叶菜类");
        product.setStock(100);
        product.setSales(50);
        product.setIsRecommend(1);
        product.setStatus(1);
        product.setCreateTime(LocalDateTime.now());
        
        return product;
    }
    
    @Override
    public ProductVO createProduct(Product product) {
        log.info("创建商品: {}", product);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        productMapper.insert(product);
        return convertToVO(product);
    }
    
    @Override
    public ProductVO updateProduct(Long id, Product product) {
        log.info("更新商品: id={}, product={}", id, product);
        product.setId(id);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        return convertToVO(product);
    }
    
    @Override
    public void deleteProduct(Long id) {
        log.info("删除商品: id={}", id);
        productMapper.deleteById(id);
    }
    
    @Override
    public void updateProductStatus(Long id, Integer status) {
        log.info("更新商品状态: id={}, status={}", id, status);
        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
    }
    
    @Override
    public void batchDeleteProducts(List<Long> ids) {
        log.info("批量删除商品: ids={}", ids);
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
}