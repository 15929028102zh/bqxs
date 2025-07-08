package com.biangqiang.freshdelivery.service;

import com.biangqiang.freshdelivery.dto.AdminLoginDTO;
import com.biangqiang.freshdelivery.vo.AdminVO;

/**
 * 管理员服务接口
 *
 * @author biangqiang
 * @since 2024-01-01
 */
public interface AdminService {

    /**
     * 管理员登录
     *
     * @param adminLoginDTO 登录信息
     * @param clientIp 客户端IP
     * @return 管理员信息
     */
    AdminVO login(AdminLoginDTO adminLoginDTO, String clientIp);

    /**
     * 获取管理员信息
     *
     * @param adminId 管理员ID
     * @return 管理员信息
     */
    AdminVO getAdminInfo(Long adminId);

    /**
     * 管理员退出登录
     *
     * @param token 访问令牌
     */
    void logout(String token);
}