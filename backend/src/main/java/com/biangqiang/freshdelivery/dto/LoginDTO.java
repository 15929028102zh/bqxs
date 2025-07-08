package com.biangqiang.freshdelivery.dto;

import lombok.Data;

/**
 * 用户登录DTO
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Data
public class LoginDTO {
    
    /**
     * 微信登录code
     */
    private String code;
    
    /**
     * 用户信息
     */
    private UserInfo userInfo;
    
    // 手动添加getter和setter方法以解决编译问题
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public UserInfo getUserInfo() {
        return userInfo;
    }
    
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
    
    @Data
    public static class UserInfo {
        /**
         * 用户昵称
         */
        private String nickName;
        
        /**
         * 用户头像
         */
        private String avatarUrl;
        
        /**
         * 性别 0-未知 1-男 2-女
         */
        private Integer gender;
        
        // 手动添加getter和setter方法以解决编译问题
        public String getNickName() {
            return nickName;
        }
        
        public void setNickName(String nickName) {
            this.nickName = nickName;
        }
        
        public String getAvatarUrl() {
            return avatarUrl;
        }
        
        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
        
        public Integer getGender() {
            return gender;
        }
        
        public void setGender(Integer gender) {
            this.gender = gender;
        }
    }
}