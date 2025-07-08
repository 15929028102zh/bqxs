package com.biangqiang.freshdelivery.controller;

import com.biangqiang.freshdelivery.common.Result;
import com.biangqiang.freshdelivery.service.CartService;
import com.biangqiang.freshdelivery.vo.CartVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 购物车控制器
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Tag(name = "购物车管理", description = "购物车相关接口")
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "添加商品到购物车", description = "将商品添加到用户购物车")
    @PostMapping("/add")
    public Result<Void> addToCart(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        // 从请求中获取用户ID（这里简化处理，实际应该从JWT token中获取）
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        Long productId = Long.valueOf(request.get("productId").toString());
        Integer quantity = Integer.valueOf(request.get("quantity").toString());
        String specification = (String) request.get("specification");
        
        boolean success = cartService.addToCart(userId, productId, quantity, specification);
        if (success) {
            return Result.success();
        } else {
            return Result.error("添加失败，请检查商品库存");
        }
    }

    @Operation(summary = "获取购物车列表", description = "获取用户的购物车商品列表")
    @GetMapping("/list")
    public Result<List<CartVO>> getCartList(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        List<CartVO> cartList = cartService.getCartList(userId);
        return Result.success(cartList);
    }

    @Operation(summary = "更新购物车商品数量", description = "更新购物车中商品的数量")
    @PutMapping("/update")
    public Result<Void> updateQuantity(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        Long cartId = Long.valueOf(request.get("cartId").toString());
        Integer quantity = Integer.valueOf(request.get("quantity").toString());
        
        boolean success = cartService.updateQuantity(userId, cartId, quantity);
        if (success) {
            return Result.success();
        } else {
            return Result.error("更新失败，请检查商品库存");
        }
    }

    @Operation(summary = "删除购物车商品", description = "从购物车中删除指定商品")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteCartItem(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        boolean success = cartService.deleteCartItem(userId, id);
        if (success) {
            return Result.success();
        } else {
            return Result.error("删除失败");
        }
    }

    @Operation(summary = "清空购物车", description = "清空用户的购物车")
    @DeleteMapping("/clear")
    public Result<Void> clearCart(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        boolean success = cartService.clearCart(userId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("清空失败");
        }
    }

    @Operation(summary = "获取购物车商品数量", description = "获取用户购物车中的商品总数量")
    @GetMapping("/count")
    public Result<Integer> getCartCount(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.success(0);
        }
        
        Integer count = cartService.getCartCount(userId);
        return Result.success(count);
    }

    /**
     * 从请求中获取用户ID（简化实现，实际应该从JWT token中解析）
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        // 这里简化处理，实际应该从JWT token中获取用户ID
        // 暂时返回固定用户ID用于测试
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            // 这里应该解析JWT token获取用户ID
            // 暂时返回固定值用于测试
            return 1L;
        }
        // 临时处理：如果没有Authorization头，返回默认用户ID用于测试
        return 1L;
    }
}