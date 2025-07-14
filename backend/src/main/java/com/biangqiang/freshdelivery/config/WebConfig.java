package com.biangqiang.freshdelivery.config;

import com.biangqiang.freshdelivery.interceptor.AdminAuthInterceptor;
import com.biangqiang.freshdelivery.interceptor.UserAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AdminAuthInterceptor adminAuthInterceptor;
    private final UserAuthInterceptor userAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册管理员认证拦截器
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/admin/**", "/upload/**")  // 拦截所有/admin开头的请求和上传接口
                .excludePathPatterns("/admin/login"); // 排除登录接口
        
        // 注册用户认证拦截器
        registry.addInterceptor(userAuthInterceptor)
                .addPathPatterns("/**")  // 拦截所有请求
                .excludePathPatterns(
                    "/admin/**",  // 排除管理员接口
                    "/user/login", "/user/phone-login", "/user/send-code", "/auth/**",  // 排除登录相关接口
                    "/product/**", "/category/**",  // 排除所有商品和分类相关接口
                    "/api/product/**", "/api/category/**",  // 排除API路径下的商品和分类接口
                    "/image/**", "/upload/**",  // 排除图片和上传接口
                    "/cart/**", "/api/cart/**",  // 排除购物车相关接口（临时用于测试）
                    "/images/**", "/uploads/**", "/static/**",  // 排除静态资源
                    "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**",  // 排除Swagger文档
                    "/", "/health", "/actuator/**", "/error"  // 排除健康检查
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 配置CORS跨域
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // 允许所有域名
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的HTTP方法
                .allowedHeaders("*") // 允许所有请求头
                .allowCredentials(true) // 允许携带凭证
                .maxAge(3600); // 预检请求缓存时间
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源映射
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/", "file:f:/code/backend/uploads/images/");
        
        // 配置上传文件访问路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:f:/code/backend/uploads/");
    }
}