package com.biangqiang.freshdelivery.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Slf4j
@Component
public class JwtUtil {
    
    // 手动添加log字段以解决编译问题
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtUtil.class);
    
    private static final String SECRET = "fresh-delivery-secret-key-2024";
    private static final long EXPIRATION = 7 * 24 * 60 * 60 * 1000L; // 7天
    
    /**
     * 生成JWT token
     *
     * @param userId 用户ID
     * @param openId 微信OpenID
     * @return JWT token
     */
    public String generateToken(Long userId, String openId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("openId", openId);
        claims.put("userType", "USER");
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }
    
    /**
     * 生成管理员JWT token
     *
     * @param adminId 管理员ID
     * @param username 用户名
     * @return JWT token
     */
    public String generateAdminToken(Long adminId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("adminId", adminId);
        claims.put("username", username);
        claims.put("userType", "ADMIN");
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(adminId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }
    
    /**
     * 从token中获取用户ID
     *
     * @param token JWT token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return Long.valueOf(claims.get("userId").toString());
        } catch (Exception e) {
            log.error("从token中获取用户ID失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 从token中获取OpenID
     *
     * @param token JWT token
     * @return OpenID
     */
    public String getOpenIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get("openId").toString();
        } catch (Exception e) {
            log.error("从token中获取OpenID失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 从token中获取管理员ID
     *
     * @param token JWT token
     * @return 管理员ID
     */
    public Long getAdminIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return Long.valueOf(claims.get("adminId").toString());
        } catch (Exception e) {
            log.error("从token中获取管理员ID失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 从token中获取用户类型
     *
     * @param token JWT token
     * @return 用户类型
     */
    public String getUserTypeFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get("userType").toString();
        } catch (Exception e) {
            log.error("从token中获取用户类型失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 验证token是否有效
     *
     * @param token JWT token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("验证token失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 从token中获取Claims
     *
     * @param token JWT token
     * @return Claims
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();
    }
}