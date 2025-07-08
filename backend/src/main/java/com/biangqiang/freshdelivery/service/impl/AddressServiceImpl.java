package com.biangqiang.freshdelivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.biangqiang.freshdelivery.entity.Address;
import com.biangqiang.freshdelivery.mapper.AddressMapper;
import com.biangqiang.freshdelivery.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收货地址服务实现类
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    // 手动添加log字段以解决编译问题
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AddressServiceImpl.class);

    private final AddressMapper addressMapper;

    @Override
    public List<Address> getAddressList(Long userId) {
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Address::getUserId, userId)
                   .orderByDesc(Address::getIsDefault)
                   .orderByDesc(Address::getCreateTime);
        return list(queryWrapper);
    }

    @Override
    public Address getDefaultAddress(Long userId) {
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Address::getUserId, userId)
                   .eq(Address::getIsDefault, 1)
                   .last("LIMIT 1");
        return getOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addAddress(Address address) {
        try {
            // 如果设置为默认地址，先取消其他默认地址
            if (address.getIsDefault() != null && address.getIsDefault() == 1) {
                addressMapper.cancelDefaultByUserId(address.getUserId());
            }
            
            // 如果是用户的第一个地址，自动设为默认
            long count = count(new LambdaQueryWrapper<Address>()
                    .eq(Address::getUserId, address.getUserId()));
            if (count == 0) {
                address.setIsDefault(1);
            }
            
            return save(address);
        } catch (Exception e) {
            log.error("添加地址失败: {}", e.getMessage(), e);
            throw new RuntimeException("添加地址失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAddress(Address address) {
        try {
            // 验证地址是否属于当前用户
            Address existAddress = getById(address.getId());
            if (existAddress == null || !existAddress.getUserId().equals(address.getUserId())) {
                throw new RuntimeException("地址不存在或无权限修改");
            }
            
            // 如果设置为默认地址，先取消其他默认地址
            if (address.getIsDefault() != null && address.getIsDefault() == 1) {
                addressMapper.cancelDefaultByUserId(address.getUserId());
            }
            
            return updateById(address);
        } catch (Exception e) {
            log.error("更新地址失败: {}", e.getMessage(), e);
            throw new RuntimeException("更新地址失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAddress(Long id, Long userId) {
        try {
            // 验证地址是否属于当前用户
            Address address = getById(id);
            if (address == null || !address.getUserId().equals(userId)) {
                throw new RuntimeException("地址不存在或无权限删除");
            }
            
            // 如果删除的是默认地址，需要设置其他地址为默认
            if (address.getIsDefault() == 1) {
                // 查找其他地址设为默认
                LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Address::getUserId, userId)
                           .ne(Address::getId, id)
                           .orderByDesc(Address::getCreateTime)
                           .last("LIMIT 1");
                Address otherAddress = getOne(queryWrapper);
                if (otherAddress != null) {
                    otherAddress.setIsDefault(1);
                    updateById(otherAddress);
                }
            }
            
            return removeById(id);
        } catch (Exception e) {
            log.error("删除地址失败: {}", e.getMessage(), e);
            throw new RuntimeException("删除地址失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultAddress(Long id, Long userId) {
        try {
            // 验证地址是否属于当前用户
            Address address = getById(id);
            if (address == null || !address.getUserId().equals(userId)) {
                throw new RuntimeException("地址不存在或无权限修改");
            }
            
            // 取消其他默认地址
            addressMapper.cancelDefaultByUserId(userId);
            
            // 设置当前地址为默认
            return addressMapper.setDefault(id) > 0;
        } catch (Exception e) {
            log.error("设置默认地址失败: {}", e.getMessage(), e);
            throw new RuntimeException("设置默认地址失败");
        }
    }
}