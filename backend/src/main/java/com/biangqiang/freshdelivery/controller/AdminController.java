package com.biangqiang.freshdelivery.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.biangqiang.freshdelivery.common.Result;
import com.biangqiang.freshdelivery.dto.AdminLoginDTO;
import com.biangqiang.freshdelivery.entity.User;
import com.biangqiang.freshdelivery.service.AdminService;
import com.biangqiang.freshdelivery.service.UserService;
import com.biangqiang.freshdelivery.vo.AdminVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 管理员控制器
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Tag(name = "管理员管理", description = "管理员相关接口")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    @Operation(summary = "管理员登录", description = "管理员通过用户名密码登录")
    @PostMapping("/login")
    public Result<AdminVO> login(@RequestBody @Validated AdminLoginDTO adminLoginDTO,
                                HttpServletRequest request) {
        String clientIp = getClientIp(request);
        return Result.success(adminService.login(adminLoginDTO, clientIp));
    }

    @Operation(summary = "获取管理员信息", description = "获取当前登录管理员的详细信息")
    @GetMapping("/info")
    public Result<AdminVO> getAdminInfo(HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("adminId");
        return Result.success(adminService.getAdminInfo(adminId));
    }

    @Operation(summary = "管理员退出登录", description = "管理员退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        adminService.logout(token);
        return Result.success();
    }

    @Operation(summary = "获取用户列表", description = "分页获取用户列表")
    @GetMapping("/user/list")
    public Result<IPage<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<User> page = new Page<>(current, size);
        IPage<User> result = userService.getUserListForAdmin(page, keyword);
        return Result.success(result);
    }

    @Operation(summary = "更新用户状态", description = "启用或禁用用户")
    @PutMapping("/user/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody Integer status) {
        userService.updateUserStatusForAdmin(id, status);
        return Result.success();
    }

    @Operation(summary = "删除用户", description = "删除指定用户")
    @DeleteMapping("/user/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUserForAdmin(id);
        return Result.success();
    }

    /**
     * 获取客户端IP地址
     *
     * @param request HTTP请求
     * @return 客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}