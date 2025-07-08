package com.biangqiang.freshdelivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.biangqiang.freshdelivery.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 管理员数据访问层
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

    /**
     * 根据用户名查询管理员
     *
     * @param username 用户名
     * @return 管理员信息
     */
    @Select("SELECT * FROM tb_admin WHERE username = #{username} AND status = 1")
    Admin selectByUsername(@Param("username") String username);

    /**
     * 根据ID查询管理员
     *
     * @param id 管理员ID
     * @return 管理员信息
     */
    @Select("SELECT * FROM tb_admin WHERE id = #{id} AND status = 1")
    Admin selectById(@Param("id") Long id);

    /**
     * 更新最后登录时间和IP
     *
     * @param id 管理员ID
     * @param lastLoginTime 最后登录时间
     * @param lastLoginIp 最后登录IP
     */
    @Update("UPDATE tb_admin SET last_login_time = #{lastLoginTime}, last_login_ip = #{lastLoginIp} WHERE id = #{id}")
    void updateLastLogin(@Param("id") Long id, 
                        @Param("lastLoginTime") LocalDateTime lastLoginTime, 
                        @Param("lastLoginIp") String lastLoginIp);
}