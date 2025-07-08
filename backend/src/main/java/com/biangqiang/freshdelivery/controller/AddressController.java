package com.biangqiang.freshdelivery.controller;

import com.biangqiang.freshdelivery.common.Result;
import com.biangqiang.freshdelivery.entity.Address;
import com.biangqiang.freshdelivery.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 收货地址控制器
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Slf4j
@Tag(name = "收货地址管理", description = "用户收货地址相关接口")
@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "获取地址列表", description = "获取当前用户的所有收货地址")
    @GetMapping("/list")
    public Result<List<Address>> getAddressList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        List<Address> addressList = addressService.getAddressList(userId);
        return Result.success(addressList);
    }

    @Operation(summary = "获取默认地址", description = "获取当前用户的默认收货地址")
    @GetMapping("/default")
    public Result<Address> getDefaultAddress(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        Address defaultAddress = addressService.getDefaultAddress(userId);
        if (defaultAddress == null) {
            return Result.error("暂无默认地址");
        }
        
        return Result.success(defaultAddress);
    }

    @Operation(summary = "添加地址", description = "添加新的收货地址")
    @PostMapping("/add")
    public Result<String> addAddress(@RequestBody @Validated Address address, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        // 设置用户ID
        address.setUserId(userId);
        
        // 参数验证
        if (address.getReceiverName() == null || address.getReceiverName().trim().isEmpty()) {
            return Result.error("收货人姓名不能为空");
        }
        if (address.getReceiverPhone() == null || address.getReceiverPhone().trim().isEmpty()) {
            return Result.error("收货人电话不能为空");
        }
        if (address.getProvince() == null || address.getProvince().trim().isEmpty()) {
            return Result.error("省份不能为空");
        }
        if (address.getCity() == null || address.getCity().trim().isEmpty()) {
            return Result.error("城市不能为空");
        }
        if (address.getDistrict() == null || address.getDistrict().trim().isEmpty()) {
            return Result.error("区县不能为空");
        }
        if (address.getDetailAddress() == null || address.getDetailAddress().trim().isEmpty()) {
            return Result.error("详细地址不能为空");
        }
        
        // 如果未设置是否默认，默认为非默认地址
        if (address.getIsDefault() == null) {
            address.setIsDefault(0);
        }
        
        boolean success = addressService.addAddress(address);
        if (success) {
            return Result.success("添加地址成功");
        } else {
            return Result.error("添加地址失败");
        }
    }

    @Operation(summary = "更新地址", description = "更新收货地址信息")
    @PutMapping("/{id}")
    public Result<String> updateAddress(@PathVariable Long id, @RequestBody @Validated Address address, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        // 设置ID和用户ID
        address.setId(id);
        address.setUserId(userId);
        
        boolean success = addressService.updateAddress(address);
        if (success) {
            return Result.success("更新地址成功");
        } else {
            return Result.error("更新地址失败");
        }
    }

    @Operation(summary = "删除地址", description = "删除收货地址")
    @DeleteMapping("/{id}")
    public Result<String> deleteAddress(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        boolean success = addressService.deleteAddress(id, userId);
        if (success) {
            return Result.success("删除地址成功");
        } else {
            return Result.error("删除地址失败");
        }
    }

    @Operation(summary = "设置默认地址", description = "设置指定地址为默认地址")
    @PutMapping("/{id}/default")
    public Result<String> setDefaultAddress(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        boolean success = addressService.setDefaultAddress(id, userId);
        if (success) {
            return Result.success("设置默认地址成功");
        } else {
            return Result.error("设置默认地址失败");
        }
    }
}