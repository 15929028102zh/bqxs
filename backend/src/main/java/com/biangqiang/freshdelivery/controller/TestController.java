package com.biangqiang.freshdelivery.controller;

import com.biangqiang.freshdelivery.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/token")
    public String generateToken() {
        // 生成用户ID为1的测试token
        String token = jwtUtil.generateToken(1L, "test_openid");
        return "Bearer " + token;
    }
}