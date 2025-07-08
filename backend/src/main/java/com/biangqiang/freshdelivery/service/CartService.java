package com.biangqiang.freshdelivery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.biangqiang.freshdelivery.entity.Cart;
import com.biangqiang.freshdelivery.vo.CartVO;

import java.util.List;

/**
 * 购物车服务接口
 *
 * @author biangqiang
 * @since 2024-01-01
 */
public interface CartService extends IService<Cart> {

    /**
     * 添加商品到购物车
     *
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 数量
     * @param specification 规格
     * @return 是否成功
     */
    boolean addToCart(Long userId, Long productId, Integer quantity, String specification);

    /**
     * 获取用户购物车列表
     *
     * @param userId 用户ID
     * @return 购物车列表
     */
    List<CartVO> getCartList(Long userId);

    /**
     * 更新购物车商品数量
     *
     * @param userId 用户ID
     * @param cartId 购物车ID
     * @param quantity 新数量
     * @return 是否成功
     */
    boolean updateQuantity(Long userId, Long cartId, Integer quantity);

    /**
     * 删除购物车商品
     *
     * @param userId 用户ID
     * @param cartId 购物车ID
     * @return 是否成功
     */
    boolean deleteCartItem(Long userId, Long cartId);

    /**
     * 清空用户购物车
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean clearCart(Long userId);

    /**
     * 获取用户购物车商品数量
     *
     * @param userId 用户ID
     * @return 商品数量
     */
    Integer getCartCount(Long userId);

    /**
     * 删除指定商品的购物车记录
     *
     * @param userId 用户ID
     * @param productIds 商品ID列表
     * @return 是否成功
     */
    boolean removeCartItemsByProductIds(Long userId, List<Long> productIds);
}