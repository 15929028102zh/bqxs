package com.biangqiang.freshdelivery.service.impl;

import com.biangqiang.freshdelivery.dto.AdminLoginDTO;
import com.biangqiang.freshdelivery.entity.Admin;
import com.biangqiang.freshdelivery.mapper.AdminMapper;
import com.biangqiang.freshdelivery.service.AdminService;
import com.biangqiang.freshdelivery.util.JwtUtil;
import com.biangqiang.freshdelivery.util.PasswordUtil;
import com.biangqiang.freshdelivery.vo.AdminVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 管理员服务实现类
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminServiceImpl.class);
    
    private final AdminMapper adminMapper;
    private final JwtUtil jwtUtil;
    private final PasswordUtil passwordUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ADMIN_TOKEN_PREFIX = "admin:token:";

    @Override
    public AdminVO login(AdminLoginDTO adminLoginDTO, String clientIp) {
        log.info("管理员登录: {}", adminLoginDTO.getUsername());
        
        // 1. 根据用户名查询管理员
        Admin admin = adminMapper.selectByUsername(adminLoginDTO.getUsername());
        if (admin == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 2. 验证密码（明文比较）
        if (!adminLoginDTO.getPassword().equals(admin.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 3. 检查账户状态
        if (admin.getStatus() != 1) {
            throw new RuntimeException("账户已被禁用，请联系超级管理员");
        }
        
        // 4. 生成JWT token
        String token = jwtUtil.generateAdminToken(admin.getId(), admin.getUsername());
        
        // 5. 将token存储到Redis（7天过期）
        redisTemplate.opsForValue().set(ADMIN_TOKEN_PREFIX + token, admin.getId(), 7, TimeUnit.DAYS);
        
        // 6. 更新最后登录时间和IP
        adminMapper.updateLastLogin(admin.getId(), LocalDateTime.now(), clientIp);
        
        // 7. 构建返回对象
        AdminVO adminVO = new AdminVO();
        BeanUtils.copyProperties(admin, adminVO);
        adminVO.setToken(token);
        adminVO.setLastLoginTime(LocalDateTime.now());
        
        log.info("管理员登录成功: {}", admin.getUsername());
        return adminVO;
    }

    @Override
    public AdminVO getAdminInfo(Long adminId) {
        log.info("获取管理员信息: {}", adminId);
        
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }
        
        AdminVO adminVO = new AdminVO();
        BeanUtils.copyProperties(admin, adminVO);
        
        return adminVO;
    }

    @Override
    public void logout(String token) {
        if (StringUtils.hasText(token)) {
            // 从Redis中删除token
            redisTemplate.delete(ADMIN_TOKEN_PREFIX + token);
            log.info("管理员退出登录成功");
        }
    }
}