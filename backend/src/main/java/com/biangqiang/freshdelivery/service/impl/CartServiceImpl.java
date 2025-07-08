package com.biangqiang.freshdelivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.biangqiang.freshdelivery.dto.CartWithProductDTO;
import com.biangqiang.freshdelivery.entity.Cart;
import com.biangqiang.freshdelivery.entity.Product;
import com.biangqiang.freshdelivery.mapper.CartMapper;
import com.biangqiang.freshdelivery.mapper.ProductMapper;
import com.biangqiang.freshdelivery.service.CartService;
import com.biangqiang.freshdelivery.vo.CartVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车服务实现类
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    // 手动添加log字段以解决编译问题
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public boolean addToCart(Long userId, Long productId, Integer quantity, String specification) {
        log.info("添加商品到购物车: userId={}, productId={}, quantity={}", userId, productId, quantity);
        
        // 检查商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted() == 1 || product.getStatus() != 1) {
            log.warn("商品不存在或已下架: productId={}", productId);
            return false;
        }
        
        // 检查库存
        if (product.getStock() < quantity) {
            log.warn("商品库存不足: productId={}, stock={}, quantity={}", productId, product.getStock(), quantity);
            return false;
        }
        
        // 查询是否已存在相同商品（只查询未删除的记录）
        Cart existingCart = cartMapper.selectByUserIdAndProductId(userId, productId);
        
        if (existingCart != null) {
            // 更新数量
            int newQuantity = existingCart.getQuantity() + quantity;
            if (newQuantity > product.getStock()) {
                log.warn("购物车商品数量超过库存: productId={}, stock={}, newQuantity={}", productId, product.getStock(), newQuantity);
                return false;
            }
            existingCart.setQuantity(newQuantity);
            return updateById(existingCart);
        } else {
            // 检查是否存在已删除的记录，如果存在则恢复
            LambdaQueryWrapper<Cart> deletedQueryWrapper = new LambdaQueryWrapper<>();
            deletedQueryWrapper.eq(Cart::getUserId, userId)
                              .eq(Cart::getProductId, productId)
                              .eq(Cart::getDeleted, 1);
            Cart deletedCart = getOne(deletedQueryWrapper);
            
            if (deletedCart != null) {
                // 恢复已删除的记录
                deletedCart.setQuantity(quantity);
                deletedCart.setDeleted(0);
                return updateById(deletedCart);
            } else {
                // 新增购物车项
                Cart cart = new Cart();
                cart.setUserId(userId);
                cart.setProductId(productId);
                cart.setQuantity(quantity);
                cart.setDeleted(0); // 明确设置deleted字段为0
                // cart.setSpecification(specification); // 暂时移除，数据库表中没有此字段
                return save(cart);
            }
        }
    }

    @Override
    public List<CartVO> getCartList(Long userId) {
        log.info("获取用户购物车列表: userId={}", userId);
        
        List<CartWithProductDTO> cartList = cartMapper.selectCartWithProductByUserId(userId);
        
        return cartList.stream().map(cart -> {
            CartVO cartVO = new CartVO();
            BeanUtils.copyProperties(cart, cartVO);
            
            // 计算小计
            if (cart.getPrice() != null) {
                cartVO.setSubtotal(cart.getPrice().multiply(new BigDecimal(cart.getQuantity())));
            }
            
            return cartVO;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean updateQuantity(Long userId, Long cartId, Integer quantity) {
        log.info("更新购物车商品数量: userId={}, cartId={}, quantity={}", userId, cartId, quantity);
        
        if (quantity <= 0) {
            return false;
        }
        
        // 查询购物车项
        Cart cart = getById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            log.warn("购物车项不存在或不属于当前用户: cartId={}, userId={}", cartId, userId);
            return false;
        }
        
        // 检查库存
        Product product = productMapper.selectById(cart.getProductId());
        if (product == null || product.getStock() < quantity) {
            log.warn("商品库存不足: productId={}, stock={}, quantity={}", cart.getProductId(), 
                    product != null ? product.getStock() : 0, quantity);
            return false;
        }
        
        cart.setQuantity(quantity);
        return updateById(cart);
    }

    @Override
    @Transactional
    public boolean deleteCartItem(Long userId, Long cartId) {
        log.info("删除购物车商品: userId={}, cartId={}", userId, cartId);
        
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getId, cartId)
                   .eq(Cart::getUserId, userId);
        
        return remove(queryWrapper);
    }

    @Override
    @Transactional
    public boolean clearCart(Long userId) {
        log.info("清空用户购物车: userId={}", userId);
        
        try {
            // 直接物理删除所有购物车记录（包括已逻辑删除和未删除的）
            LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Cart::getUserId, userId);
            
            List<Cart> allCarts = list(queryWrapper);
            if (!allCarts.isEmpty()) {
                // 使用物理删除
                for (Cart cart : allCarts) {
                    cartMapper.deleteById(cart.getId());
                }
                log.info("物理删除购物车记录数量: {}", allCarts.size());
                return true;
            }
            
            return true; // 没有记录也算成功
        } catch (Exception e) {
            log.error("清空购物车失败: userId={}, error={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Integer getCartCount(Long userId) {
        log.info("获取用户购物车商品数量: userId={}", userId);
        
        return cartMapper.countByUserId(userId);
    }

    @Override
    @Transactional
    public boolean removeCartItemsByProductIds(Long userId, List<Long> productIds) {
        log.info("删除指定商品的购物车记录: userId={}, productIds={}", userId, productIds);
        
        if (productIds == null || productIds.isEmpty()) {
            return true;
        }
        
        try {
            // 使用物理删除指定商品的购物车记录
            int deletedCount = cartMapper.physicalDeleteByUserIdAndProductIds(userId, productIds);
            log.info("物理删除购物车记录数量: {}", deletedCount);
            
            return true;
        } catch (Exception e) {
            log.error("删除指定商品的购物车记录失败: userId={}, productIds={}, error={}", userId, productIds, e.getMessage(), e);
            throw e;
        }
    }
}