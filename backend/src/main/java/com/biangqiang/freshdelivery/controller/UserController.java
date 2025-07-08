package com.biangqiang.freshdelivery.controller;

import com.biangqiang.freshdelivery.common.Result;
import com.biangqiang.freshdelivery.dto.LoginDTO;
import com.biangqiang.freshdelivery.dto.UserUpdateDTO;
import com.biangqiang.freshdelivery.entity.User;
import com.biangqiang.freshdelivery.service.UserService;
import com.biangqiang.freshdelivery.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户控制器
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "微信登录", description = "用户通过微信小程序登录")
    @PostMapping("/login")
    public Result<UserVO> login(@RequestBody @Validated LoginDTO loginDTO) {
        return Result.success(userService.login(loginDTO));
    }

    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/info")
    public Result<UserVO> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.getUserInfo(userId));
    }

    @Operation(summary = "更新用户信息", description = "更新用户的个人资料")
    @PutMapping("/update")
    public Result<Void> updateUser(@RequestBody @Validated UserUpdateDTO userUpdateDTO, 
                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userService.updateUser(userId, userUpdateDTO);
        return Result.success();
    }

    @Operation(summary = "绑定手机号", description = "用户绑定手机号码")
    @PostMapping("/bind-phone")
    public Result<Void> bindPhone(@RequestParam String phone, 
                                  @RequestParam String code,
                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userService.bindPhone(userId, phone, code);
        return Result.success();
    }

    @Operation(summary = "发送验证码", description = "向指定手机号发送验证码")
    @PostMapping("/send-code")
    public Result<Void> sendCode(@RequestParam String phone) {
        userService.sendCode(phone);
        return Result.success();
    }

    @Operation(summary = "手机号登录", description = "用户通过手机号和验证码登录")
    @PostMapping("/phone-login")
    public Result<UserVO> phoneLogin(@RequestParam String phone,
                                     @RequestParam String smsCode,
                                     @RequestParam String code) {
        return Result.success(userService.phoneLogin(phone, smsCode, code));
    }

    @Operation(summary = "退出登录", description = "用户退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        userService.logout(token);
        return Result.success();
    }
}