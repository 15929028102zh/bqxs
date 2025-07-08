package com.biangqiang.freshdelivery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 微信OpenID
     */
    @TableField("open_id")
    private String openId;

    /**
     * 微信UnionID
     */
    @TableField("union_id")
    private String unionId;

    /**
     * 用户昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 用户头像
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 手机号码
     */
    @TableField("phone")
    private String phone;

    /**
     * 性别：0-未知，1-男，2-女
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 用户状态：0-禁用，1-正常
     */
    @TableField("status")
    private Integer status;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;
    
    // 手动添加setter方法以解决编译问题
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setOpenId(String openId) {
        this.openId = openId;
    }
    
    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public void setGender(Integer gender) {
        this.gender = gender;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
    
    // 手动添加getter方法以解决编译问题
    public Long getId() {
        return id;
    }
    
    public String getOpenId() {
        return openId;
    }
    
    public String getUnionId() {
        return unionId;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public Integer getGender() {
        return gender;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public Integer getDeleted() {
        return deleted;
    }
}