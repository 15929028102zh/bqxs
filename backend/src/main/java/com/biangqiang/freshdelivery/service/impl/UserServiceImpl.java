package com.biangqiang.freshdelivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.biangqiang.freshdelivery.dto.LoginDTO;
import com.biangqiang.freshdelivery.dto.UserUpdateDTO;
import com.biangqiang.freshdelivery.entity.User;
import com.biangqiang.freshdelivery.mapper.UserMapper;
import com.biangqiang.freshdelivery.service.UserService;
import com.biangqiang.freshdelivery.util.JwtUtil;
import com.biangqiang.freshdelivery.util.WeChatUtil;
import com.biangqiang.freshdelivery.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 用户服务实现类
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    // 手动添加log字段以解决编译问题
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserServiceImpl.class);
    
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final WeChatUtil weChatUtil;
    
    @Override
    public UserVO login(LoginDTO loginDTO) {
        log.info("用户登录: {}", loginDTO.getCode());
        
        // 1. 通过微信code获取用户信息
        WeChatUtil.WeChatUserInfo weChatUserInfo = weChatUtil.getWeChatUserInfo(loginDTO.getCode());
        if (weChatUserInfo == null) {
            log.error("微信登录失败: 无法获取微信用户信息，请检查微信小程序配置或网络连接");
            throw new RuntimeException("微信登录失败，请检查网络连接或稍后重试");
        }
        if (!StringUtils.hasText(weChatUserInfo.getOpenId())) {
            log.error("微信登录失败: 获取到的openId为空");
            throw new RuntimeException("微信登录失败，无法获取用户标识");
        }
        
        // 2. 查询用户是否已存在
        User user = userMapper.selectByOpenId(weChatUserInfo.getOpenId());
        
        // 3. 如果用户不存在，创建新用户
        if (user == null) {
            user = new User();
            user.setOpenId(weChatUserInfo.getOpenId());
            user.setUnionId(weChatUserInfo.getUnionId());
            
            // 使用前端传递的用户信息，如果没有则使用默认值
            if (loginDTO.getUserInfo() != null) {
                user.setNickname(StringUtils.hasText(loginDTO.getUserInfo().getNickName()) 
                    ? loginDTO.getUserInfo().getNickName() 
                    : "用户" + System.currentTimeMillis());
                user.setAvatar(StringUtils.hasText(loginDTO.getUserInfo().getAvatarUrl()) 
                    ? loginDTO.getUserInfo().getAvatarUrl() 
                    : "https://example.com/default-avatar.jpg");
                user.setGender(loginDTO.getUserInfo().getGender());
            } else {
                user.setNickname("用户" + System.currentTimeMillis());
                user.setAvatar("https://example.com/default-avatar.jpg");
            }
            
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            userMapper.insert(user);
            
            log.info("创建新用户成功: userId={}, nickname={}, avatar={}", 
                user.getId(), user.getNickname(), user.getAvatar());
        } else {
            // 4. 更新用户信息（如果前端传递了新的用户信息）
            boolean needUpdate = false;
            if (loginDTO.getUserInfo() != null) {
                if (StringUtils.hasText(loginDTO.getUserInfo().getNickName()) 
                    && !loginDTO.getUserInfo().getNickName().equals(user.getNickname())) {
                    user.setNickname(loginDTO.getUserInfo().getNickName());
                    needUpdate = true;
                }
                if (StringUtils.hasText(loginDTO.getUserInfo().getAvatarUrl()) 
                    && !loginDTO.getUserInfo().getAvatarUrl().equals(user.getAvatar())) {
                    user.setAvatar(loginDTO.getUserInfo().getAvatarUrl());
                    needUpdate = true;
                }
                if (loginDTO.getUserInfo().getGender() != null 
                    && !loginDTO.getUserInfo().getGender().equals(user.getGender())) {
                    user.setGender(loginDTO.getUserInfo().getGender());
                    needUpdate = true;
                }
            }
            
            // 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            needUpdate = true;
            
            if (needUpdate) {
                user.setUpdateTime(LocalDateTime.now());
                userMapper.updateById(user);
                log.info("更新用户信息成功: userId={}, nickname={}, avatar={}", 
                    user.getId(), user.getNickname(), user.getAvatar());
            }
        }
        
        // 5. 生成JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getOpenId());
        
        // 6. 构建返回结果
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setToken(token);
        
        log.info("用户登录成功: userId={}, openId={}", user.getId(), user.getOpenId());
        return userVO;
    }
    
    @Override
    public UserVO getUserInfo(Long userId) {
        log.info("获取用户信息: {}", userId);
        
        // 1. 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 2. 构建返回结果
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        
        log.info("获取用户信息成功: userId={}, nickname={}", user.getId(), user.getNickname());
        return userVO;
    }
    
    @Override
    public Boolean updateUser(Long userId, UserUpdateDTO userUpdateDTO) {
        log.info("更新用户信息: userId={}, updateInfo={}", userId, userUpdateDTO);
        
        // 1. 查询用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 2. 更新用户信息
        if (StringUtils.hasText(userUpdateDTO.getNickname())) {
            user.setNickname(userUpdateDTO.getNickname());
        }
        if (StringUtils.hasText(userUpdateDTO.getAvatar())) {
            user.setAvatar(userUpdateDTO.getAvatar());
        }
        if (StringUtils.hasText(userUpdateDTO.getPhone())) {
            user.setPhone(userUpdateDTO.getPhone());
        }
        if (userUpdateDTO.getGender() != null) {
            user.setGender(userUpdateDTO.getGender());
        }
        user.setUpdateTime(LocalDateTime.now());
        
        // 3. 保存到数据库
        int result = userMapper.updateById(user);
        
        log.info("更新用户信息完成: userId={}, result={}", userId, result > 0);
        return result > 0;
    }
    
    @Override
    public Boolean bindPhone(Long userId, String phone, String code) {
        log.info("绑定手机号: userId={}, phone={}, code={}", userId, phone, code);
        
        // 1. 验证验证码（这里简化处理，实际应该从Redis中验证）
        if (!"123456".equals(code)) {
            throw new RuntimeException("验证码错误");
        }
        
        // 2. 检查手机号是否已被其他用户绑定
        User existUser = userMapper.selectByPhone(phone);
        if (existUser != null && !existUser.getId().equals(userId)) {
            throw new RuntimeException("该手机号已被其他用户绑定");
        }
        
        // 3. 查询当前用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 4. 绑定手机号
        user.setPhone(phone);
        user.setUpdateTime(LocalDateTime.now());
        int result = userMapper.updateById(user);
        
        log.info("绑定手机号完成: userId={}, phone={}, result={}", userId, phone, result > 0);
        return result > 0;
    }
    
    @Override
    public Boolean sendCode(String phone) {
        log.info("发送验证码: phone={}", phone);
        
        // 1. 生成6位随机验证码
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(100000, 999999));
        
        // 2. 这里应该调用短信服务发送验证码，并将验证码存储到Redis中
        // 为了演示，这里只是打印验证码
        log.info("验证码已生成: phone={}, code={}", phone, code);
        
        // 3. 实际项目中应该:
        // - 调用阿里云、腾讯云等短信服务发送验证码
        // - 将验证码存储到Redis中，设置5分钟过期时间
        // - 限制同一手机号发送频率（如1分钟内只能发送一次）
        
        return true;
    }
    
    @Override
    public UserVO phoneLogin(String phone, String smsCode, String code) {
        log.info("手机号登录: phone={}, smsCode={}, code={}", phone, smsCode, code);
        
        // 1. 验证短信验证码（这里简化处理，实际应该从Redis中验证）
        if (!"123456".equals(smsCode)) {
            throw new RuntimeException("验证码错误");
        }
        
        // 2. 通过微信code获取用户信息
        WeChatUtil.WeChatUserInfo weChatUserInfo = weChatUtil.getWeChatUserInfo(code);
        if (weChatUserInfo == null) {
            log.error("手机号登录-微信验证失败: 无法获取微信用户信息，请检查微信小程序配置或网络连接");
            throw new RuntimeException("微信验证失败，请检查网络连接或稍后重试");
        }
        if (!StringUtils.hasText(weChatUserInfo.getOpenId())) {
            log.error("手机号登录-微信验证失败: 获取到的openId为空");
            throw new RuntimeException("微信验证失败，无法获取用户标识");
        }
        
        // 3. 查询用户是否存在（先按手机号查询，再按openId查询）
        User user = userMapper.selectByPhone(phone);
        if (user == null) {
            user = userMapper.selectByOpenId(weChatUserInfo.getOpenId());
        }
        
        // 4. 如果用户不存在，创建新用户
        if (user == null) {
            user = new User();
            user.setOpenId(weChatUserInfo.getOpenId());
            user.setUnionId(weChatUserInfo.getUnionId());
            user.setPhone(phone);
            user.setNickname("用户" + System.currentTimeMillis());
            user.setAvatar("https://example.com/default-avatar.jpg");
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            userMapper.insert(user);
        } else {
            // 5. 更新用户信息（绑定手机号和openId）
            boolean needUpdate = false;
            if (!StringUtils.hasText(user.getPhone())) {
                user.setPhone(phone);
                needUpdate = true;
            }
            if (!StringUtils.hasText(user.getOpenId())) {
                user.setOpenId(weChatUserInfo.getOpenId());
                needUpdate = true;
            }
            if (needUpdate) {
                user.setUpdateTime(LocalDateTime.now());
                userMapper.updateById(user);
            }
            
            // 6. 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateById(user);
        }
        
        // 7. 生成JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getOpenId());
        
        // 8. 构建返回结果
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setToken(token);
        
        log.info("手机号登录成功: userId={}, phone={}, openId={}", user.getId(), phone, user.getOpenId());
        return userVO;
    }
    
    @Override
    public Boolean logout(String token) {
        log.info("用户登出: token={}", token);
        
        // 1. 验证token格式
        if (!StringUtils.hasText(token)) {
            throw new RuntimeException("token不能为空");
        }
        
        // 2. 从token中获取用户信息
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new RuntimeException("无效的token");
        }
        
        // 3. 实际项目中应该将token加入Redis黑名单
        // 这里简化处理，只是记录日志
        log.info("用户登出成功: userId={}", userId);
        
        // 4. 可以考虑记录用户登出时间等信息
        
        return true;
    }
    
    @Override
    public IPage<User> getUserListForAdmin(Page<User> page, String keyword) {
        log.info("管理员获取用户列表: page={}, keyword={}", page.getCurrent(), keyword);
        
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 如果有搜索关键词，按昵称或手机号搜索
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(User::getNickname, keyword)
                       .or()
                       .like(User::getPhone, keyword);
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(User::getCreateTime);
        
        return userMapper.selectPage(page, queryWrapper);
    }
    
    @Override
    public Boolean updateUserStatusForAdmin(Long userId, Integer status) {
        log.info("管理员更新用户状态: userId={}, status={}", userId, status);
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        
        int result = userMapper.updateById(user);
        return result > 0;
    }
    
    @Override
    public Boolean deleteUserForAdmin(Long userId) {
        log.info("管理员删除用户: userId={}", userId);
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        int result = userMapper.deleteById(userId);
        return result > 0;
    }
    
    // 统计相关方法实现
    
    @Override
    public Long getTotalUserCount() {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getStatus, 1); // 只统计正常状态的用户
        return userMapper.selectCount(queryWrapper);
    }
    
    @Override
    public Long getTodayUserCount(LocalDate date) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(User::getCreateTime, date.atStartOfDay())
                   .lt(User::getCreateTime, date.plusDays(1).atStartOfDay())
                   .eq(User::getStatus, 1); // 只统计正常状态的用户
        return userMapper.selectCount(queryWrapper);
    }
    
    @Override
    public Long getNewUserCountByDate(LocalDate date) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(User::getCreateTime, date.atStartOfDay())
                   .lt(User::getCreateTime, date.plusDays(1).atStartOfDay())
                   .eq(User::getStatus, 1); // 只统计正常状态的用户
        return userMapper.selectCount(queryWrapper);
    }
    
    @Override
    public Long getTotalUserCountByDate(LocalDate date) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(User::getCreateTime, date.plusDays(1).atStartOfDay())
                   .eq(User::getStatus, 1); // 只统计正常状态的用户
        return userMapper.selectCount(queryWrapper);
    }
}