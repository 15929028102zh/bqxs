package com.biangqiang.freshdelivery.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import javax.annotation.PostConstruct;

/**
 * 微信API工具类
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Slf4j
@Component
public class WeChatUtil {
    
    // 手动添加log字段以解决编译问题
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WeChatUtil.class);
    
    @Value("${wechat.miniapp.app-id:your-app-id}")
    private String appId;
    
    @Value("${wechat.miniapp.app-secret:your-app-secret}")
    private String appSecret;
    
    private static final String CODE_TO_SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 配置验证
     */
    @PostConstruct
    public void validateConfig() {
        if ("your-app-id".equals(appId) || "your-miniapp-appid".equals(appId)) {
            log.warn("微信小程序AppID使用默认值，请在application.yml中配置真实的AppID");
        }
        if ("your-app-secret".equals(appSecret) || "your-miniapp-secret".equals(appSecret)) {
            log.warn("微信小程序AppSecret使用默认值，请在application.yml中配置真实的AppSecret");
        }
        log.info("微信小程序配置: appId={}", appId);
    }
    
    /**
     * 通过code获取微信用户信息
     *
     * @param code 微信登录code
     * @return 微信用户信息
     */
    public WeChatUserInfo getWeChatUserInfo(String code) {
        try {
            String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    CODE_TO_SESSION_URL, appId, appSecret, code);
            
            String response = restTemplate.getForObject(url, String.class);
            log.info("微信登录响应: {}", response);
            
            JsonNode jsonNode = objectMapper.readTree(response);
            
            if (jsonNode.has("errcode")) {
                int errcode = jsonNode.get("errcode").asInt();
                if (errcode != 0) {
                    String errmsg = jsonNode.get("errmsg").asText();
                    log.error("微信登录失败: errcode={}, errmsg={}, appId={}", errcode, errmsg, appId);
                    
                    // 提供更友好的错误信息
                    String friendlyMessage = getFriendlyErrorMessage(errcode, errmsg);
                    log.error("微信登录错误详情: {}", friendlyMessage);
                    return null;
                }
            }
            
            WeChatUserInfo userInfo = new WeChatUserInfo();
            userInfo.setOpenId(jsonNode.get("openid").asText());
            if (jsonNode.has("unionid")) {
                userInfo.setUnionId(jsonNode.get("unionid").asText());
            }
            userInfo.setSessionKey(jsonNode.get("session_key").asText());
            
            return userInfo;
            
        } catch (Exception e) {
            log.error("获取微信用户信息失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 获取友好的错误信息
     *
     * @param errcode 错误代码
     * @param errmsg  错误信息
     * @return 友好的错误信息
     */
    private String getFriendlyErrorMessage(int errcode, String errmsg) {
        switch (errcode) {
            case 40013:
                return "AppID无效，请检查微信小程序AppID配置是否正确";
            case 40125:
                return "AppSecret无效，请检查微信小程序AppSecret配置是否正确";
            case 40029:
                return "code无效，可能已过期或被使用过，请重新获取";
            case 45011:
                return "API调用太频繁，请稍后再试";
            case 40226:
                return "高风险等级用户，小程序登录拦截";
            case 40163:
                return "code已被使用，微信登录code只能使用一次";
            default:
                return String.format("微信API错误: %s (错误代码: %d)", errmsg, errcode);
        }
    }
    
    /**
     * 微信用户信息
     */
    public static class WeChatUserInfo {
        private String openId;
        private String unionId;
        private String sessionKey;
        
        public String getOpenId() {
            return openId;
        }
        
        public void setOpenId(String openId) {
            this.openId = openId;
        }
        
        public String getUnionId() {
            return unionId;
        }
        
        public void setUnionId(String unionId) {
            this.unionId = unionId;
        }
        
        public String getSessionKey() {
            return sessionKey;
        }
        
        public void setSessionKey(String sessionKey) {
            this.sessionKey = sessionKey;
        }
    }
}