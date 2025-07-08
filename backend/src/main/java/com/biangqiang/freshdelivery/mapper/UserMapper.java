package com.biangqiang.freshdelivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.biangqiang.freshdelivery.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 用户Mapper接口
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据OpenID查询用户
     *
     * @param openId 微信OpenID
     * @return 用户信息
     */
    @Select("SELECT * FROM tb_user WHERE open_id = #{openId} AND deleted = 0")
    User selectByOpenId(@Param("openId") String openId);
    
    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    @Select("SELECT * FROM tb_user WHERE phone = #{phone} AND deleted = 0")
    User selectByPhone(@Param("phone") String phone);
    
    /**
     * 更新用户最后登录时间
     *
     * @param userId 用户ID
     * @param lastLoginTime 最后登录时间
     */
    @Update("UPDATE tb_user SET last_login_time = #{lastLoginTime} WHERE id = #{userId}")
    void updateLastLoginTime(@Param("userId") Long userId, @Param("lastLoginTime") LocalDateTime lastLoginTime);
}