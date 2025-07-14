package com.biangqiang.freshdelivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.biangqiang.freshdelivery.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 收货地址 Mapper 接口
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Mapper
public interface AddressMapper extends BaseMapper<Address> {

    /**
     * 根据用户ID获取地址列表
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    List<Address> selectByUserId(@Param("userId") Long userId);

    /**
     * 获取用户的默认地址
     *
     * @param userId 用户ID
     * @return 默认地址
     */
    Address selectDefaultByUserId(@Param("userId") Long userId);

    /**
     * 取消用户的所有默认地址
     *
     * @param userId 用户ID
     * @return 更新行数
     */
    @Update("UPDATE tb_user_address SET is_default = 0 WHERE user_id = #{userId} AND deleted = 0")
    int cancelDefaultByUserId(@Param("userId") Long userId);

    /**
     * 设置默认地址
     *
     * @param id 地址ID
     * @return 更新行数
     */
    @Update("UPDATE `address` SET is_default = 1 WHERE id = #{id}")
    int setDefault(@Param("id") Long id);
}