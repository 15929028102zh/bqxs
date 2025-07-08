package com.biangqiang.freshdelivery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.biangqiang.freshdelivery.entity.Address;

import java.util.List;

/**
 * 收货地址服务接口
 *
 * @author biangqiang
 * @since 2024-01-01
 */
public interface AddressService extends IService<Address> {

    /**
     * 获取用户地址列表
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    List<Address> getAddressList(Long userId);

    /**
     * 获取用户默认地址
     *
     * @param userId 用户ID
     * @return 默认地址
     */
    Address getDefaultAddress(Long userId);

    /**
     * 添加地址
     *
     * @param address 地址信息
     * @return 是否成功
     */
    boolean addAddress(Address address);

    /**
     * 更新地址
     *
     * @param address 地址信息
     * @return 是否成功
     */
    boolean updateAddress(Address address);

    /**
     * 删除地址
     *
     * @param id 地址ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteAddress(Long id, Long userId);

    /**
     * 设置默认地址
     *
     * @param id 地址ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean setDefaultAddress(Long id, Long userId);
}