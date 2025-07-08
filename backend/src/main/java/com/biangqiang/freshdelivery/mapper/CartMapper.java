package com.biangqiang.freshdelivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.biangqiang.freshdelivery.dto.CartWithProductDTO;
import com.biangqiang.freshdelivery.entity.Cart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 购物车Mapper接口
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Mapper
public interface CartMapper extends BaseMapper<Cart> {

    /**
     * 根据用户ID和商品ID查询购物车项
     *
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 购物车项
     */
    @Select("SELECT * FROM tb_cart WHERE user_id = #{userId} AND product_id = #{productId} AND deleted = 0")
    Cart selectByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * 根据用户ID查询购物车列表（包含商品信息）
     *
     * @param userId 用户ID
     * @return 购物车列表
     */
    @Select("SELECT c.*, p.name as productName, p.price, p.images, p.stock " +
            "FROM tb_cart c LEFT JOIN tb_product p ON c.product_id = p.id " +
            "WHERE c.user_id = #{userId} AND c.deleted = 0 AND p.deleted = 0 " +
            "ORDER BY c.create_time DESC")
    List<CartWithProductDTO> selectCartWithProductByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID统计购物车商品数量
     *
     * @param userId 用户ID
     * @return 商品数量
     */
    @Select("SELECT COALESCE(SUM(quantity), 0) FROM tb_cart WHERE user_id = #{userId} AND deleted = 0")
    Integer countByUserId(@Param("userId") Long userId);

    /**
     * 物理删除指定用户的指定商品购物车记录
     *
     * @param userId 用户ID
     * @param productIds 商品ID列表
     * @return 删除的记录数
     */
    @Delete("<script>" +
            "DELETE FROM tb_cart WHERE user_id = #{userId} AND product_id IN " +
            "<foreach collection='productIds' item='productId' open='(' separator=',' close=')'>" +
            "#{productId}" +
            "</foreach>" +
            "</script>")
    int physicalDeleteByUserIdAndProductIds(@Param("userId") Long userId, @Param("productIds") List<Long> productIds);
}