package com.biangqiang.freshdelivery.dto;

import lombok.Data;

/**
 * 用户更新DTO
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Data
public class UserUpdateDTO {
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 用户头像
     */
    private String avatar;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 性别 0-未知 1-男 2-女
     */
    private Integer gender;
    
    // 手动添加getter和setter方法以解决编译问题
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public Integer getGender() {
        return gender;
    }
    
    public void setGender(Integer gender) {
        this.gender = gender;
    }
}