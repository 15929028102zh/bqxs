package com.biangqiang.freshdelivery.interceptor;

import com.biangqiang.freshdelivery.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 管理员认证拦截器
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {

    // 手动添加log字段以解决编译问题
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminAuthInterceptor.class);

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ADMIN_TOKEN_PREFIX = "admin:token:";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求路径
        String requestURI = request.getRequestURI();
        
        // 管理员登录接口不需要验证
        if ("/admin/login".equals(requestURI)) {
            return true;
        }
        
        // 获取Authorization头
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.warn("管理员访问接口缺少Authorization头: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未授权访问\"}");
            return false;
        }
        
        // 提取token
        String token = authHeader.substring(7);
        
        // 验证token格式
        if (!jwtUtil.validateToken(token)) {
            log.warn("管理员token无效: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"token无效\"}");
            return false;
        }
        
        // 检查token类型
        String userType = jwtUtil.getUserTypeFromToken(token);
        if (!"ADMIN".equals(userType)) {
            log.warn("非管理员token访问管理员接口: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"权限不足\"}");
            return false;
        }
        
        // 检查Redis中是否存在token
        Object adminId = redisTemplate.opsForValue().get(ADMIN_TOKEN_PREFIX + token);
        if (adminId == null) {
            log.warn("管理员token已过期或不存在: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"token已过期\"}");
            return false;
        }
        
        // 从token中获取管理员ID并设置到request中
        Long adminIdFromToken = jwtUtil.getAdminIdFromToken(token);
        request.setAttribute("adminId", adminIdFromToken);
        
        return true;
    }
}