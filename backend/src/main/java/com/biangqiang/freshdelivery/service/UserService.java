package com.biangqiang.freshdelivery.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.biangqiang.freshdelivery.dto.LoginDTO;
import com.biangqiang.freshdelivery.dto.UserUpdateDTO;
import com.biangqiang.freshdelivery.entity.User;
import com.biangqiang.freshdelivery.vo.UserVO;

/**
 * 用户服务接口
 *
 * @author biangqiang
 * @since 2024-01-01
 */
public interface UserService {
    
    /**
     * 微信登录
     *
     * @param loginDTO 登录信息
     * @return 用户信息
     */
    UserVO login(LoginDTO loginDTO);
    
    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserVO getUserInfo(Long userId);
    
    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param userUpdateDTO 更新信息
     * @return 是否成功
     */
    Boolean updateUser(Long userId, UserUpdateDTO userUpdateDTO);
    
    /**
     * 绑定手机号
     *
     * @param userId 用户ID
     * @param phone 手机号
     * @param code 验证码
     * @return 是否成功
     */
    Boolean bindPhone(Long userId, String phone, String code);
    
    /**
     * 发送验证码
     *
     * @param phone 手机号
     * @return 是否成功
     */
    Boolean sendCode(String phone);
    
    /**
     * 手机号登录
     *
     * @param phone 手机号
     * @param smsCode 短信验证码
     * @param code 微信登录code
     * @return 用户信息
     */
    UserVO phoneLogin(String phone, String smsCode, String code);
    
    /**
     * 用户登出
     *
     * @param token JWT Token
     * @return 是否成功
     */
    Boolean logout(String token);
    
    /**
     * 管理员获取用户列表
     *
     * @param page 分页参数
     * @param keyword 搜索关键词
     * @return 用户列表
     */
    IPage<User> getUserListForAdmin(Page<User> page, String keyword);
    
    /**
     * 管理员更新用户状态
     *
     * @param userId 用户ID
     * @param status 状态
     * @return 是否成功
     */
    Boolean updateUserStatusForAdmin(Long userId, Integer status);
    
    /**
     * 管理员删除用户
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean deleteUserForAdmin(Long userId);
    
    // 统计相关方法
    
    /**
     * 获取用户总数
     *
     * @return 用户总数
     */
    Long getTotalUserCount();
    
    /**
     * 获取今日新增用户数
     *
     * @param date 日期
     * @return 今日新增用户数
     */
    Long getTodayUserCount(java.time.LocalDate date);
    
    /**
     * 根据日期获取新增用户数
     *
     * @param date 日期
     * @return 新增用户数
     */
    Long getNewUserCountByDate(java.time.LocalDate date);
    
    /**
     * 根据日期获取累计用户数
     *
     * @param date 日期
     * @return 累计用户数
     */
    Long getTotalUserCountByDate(java.time.LocalDate date);
}