package com.biangqiang.freshdelivery.interceptor;

import com.biangqiang.freshdelivery.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户认证拦截器
 * 用于验证用户JWT token并设置用户ID到request属性中
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserAuthInterceptor implements HandlerInterceptor {

    // 手动添加log字段以解决编译问题
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserAuthInterceptor.class);

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求路径
        String requestURI = request.getRequestURI();
        String servletPath = request.getServletPath();
        String contextPath = request.getContextPath();
        
        log.info("UserAuthInterceptor处理请求: requestURI={}, servletPath={}, contextPath={}", requestURI, servletPath, contextPath);
        
        // 不需要认证的接口
        if (isExcludedPath(requestURI)) {
            log.info("请求路径被排除，无需认证: {}", requestURI);
            return true;
        }
        
        log.info("请求路径需要认证: {}", requestURI);
        
        // 获取Authorization头
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.warn("用户访问接口缺少Authorization头: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未授权访问，请先登录\"}");
            return false;
        }
        
        // 提取token（去掉"Bearer "前缀）
        String token = authHeader.substring(7);
        
        // 验证token有效性
        if (!jwtUtil.validateToken(token)) {
            log.warn("用户访问接口token无效: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"token已过期或无效，请重新登录\"}");
            return false;
        }
        
        // 验证用户类型
        String userType = jwtUtil.getUserTypeFromToken(token);
        if (!"USER".equals(userType)) {
            log.warn("用户访问接口token类型错误: {}, userType: {}", requestURI, userType);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"权限不足\"}");
            return false;
        }
        
        // 从token中获取用户ID
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            log.warn("用户访问接口无法获取用户ID: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"无效的用户信息\"}");
            return false;
        }
        
        // 将用户ID设置到request属性中，供Controller使用
        request.setAttribute("userId", userId);
        
        // 同时设置openId（可选）
        String openId = jwtUtil.getOpenIdFromToken(token);
        if (openId != null) {
            request.setAttribute("openId", openId);
        }
        
        log.debug("用户认证成功: userId={}, requestURI={}", userId, requestURI);
        return true;
    }
    
    /**
     * 判断是否为不需要认证的路径
     */
    private boolean isExcludedPath(String requestURI) {
        // 登录相关接口
        if (requestURI.startsWith("/api/user/login") || 
            requestURI.startsWith("/api/user/phone-login") ||
            requestURI.startsWith("/api/user/send-code") ||
            requestURI.startsWith("/api/auth/")) {
            return true;
        }
        
        // 商品和分类查询接口（无需登录）
        if (requestURI.startsWith("/api/product/") ||
            requestURI.startsWith("/api/category/")) {
            return true;
        }
        
        // 静态资源
        if (requestURI.startsWith("/api/images/") ||
            requestURI.startsWith("/api/uploads/") ||
            requestURI.startsWith("/api/static/")) {
            return true;
        }
        
        // Swagger文档
        if (requestURI.startsWith("/api/swagger-ui") ||
            requestURI.startsWith("/api/v3/api-docs") ||
            requestURI.startsWith("/api/swagger-resources") ||
            requestURI.startsWith("/api/webjars/")) {
            return true;
        }
        
        // 健康检查
        if (requestURI.equals("/api/") ||
            requestURI.equals("/api/health") ||
            requestURI.equals("/api/actuator/health")) {
            return true;
        }
        
        return false;
    }
}