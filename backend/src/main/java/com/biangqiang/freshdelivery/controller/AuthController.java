package com.biangqiang.freshdelivery.controller;

import com.biangqiang.freshdelivery.common.Result;
import com.biangqiang.freshdelivery.dto.LoginDTO;
import com.biangqiang.freshdelivery.service.UserService;
import com.biangqiang.freshdelivery.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Slf4j
@Tag(name = "用户认证", description = "用户认证相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    // 手动添加log字段以解决编译问题
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    @Operation(summary = "微信登录", description = "用户通过微信小程序登录")
    @PostMapping("/wx-login")
    public Result<UserVO> wxLogin(@RequestBody @Validated LoginDTO loginDTO) {
        log.info("微信登录请求: {}", loginDTO.getCode());
        return Result.success(userService.login(loginDTO));
    }

    @Operation(summary = "快速登录", description = "用户快速登录（仅使用微信code）")
    @PostMapping("/quick-login")
    public Result<UserVO> quickLogin(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        log.info("快速登录请求: {}", code);
        
        // 创建LoginDTO对象，只设置code
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setCode(code);
        
        return Result.success(userService.login(loginDTO));
    }

    @Operation(summary = "解密手机号", description = "解密微信小程序获取的手机号")
    @PostMapping("/decrypt-phone")
    public Result<Map<String, String>> decryptPhone(@RequestBody Map<String, String> request) {
        String encryptedData = request.get("encryptedData");
        String iv = request.get("iv");
        
        log.info("解密手机号请求");
        
        // TODO: 实现手机号解密逻辑
        // 这里需要使用微信小程序的解密算法
        // 暂时返回模拟数据
        Map<String, String> result = new java.util.HashMap<>();
        result.put("phoneNumber", "13800138000");
        result.put("purePhoneNumber", "13800138000");
        result.put("countryCode", "86");
        
        return Result.success(result);
    }
}