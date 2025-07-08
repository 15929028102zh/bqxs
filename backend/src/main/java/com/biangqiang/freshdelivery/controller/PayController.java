package com.biangqiang.freshdelivery.controller;

import com.biangqiang.freshdelivery.common.Result;
import com.biangqiang.freshdelivery.service.WechatPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

/**
 * 支付控制器
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Slf4j
@Tag(name = "支付管理", description = "支付相关接口")
@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PayController {

    // 手动添加log字段以解决编译问题
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PayController.class);

    private final WechatPayService wechatPayService;

    /**
     * 微信支付结果通知
     */
    @PostMapping("/notify")
    @Operation(summary = "微信支付通知", description = "接收微信支付结果通知")
    public String payNotify(HttpServletRequest request) {
        try {
            // 读取通知数据
            String notifyData = readRequestBody(request);
            log.info("收到微信支付通知：{}", notifyData);
            
            // 处理支付通知
            boolean success = wechatPayService.handlePayNotify(notifyData);
            
            if (success) {
                // 返回成功响应给微信
                return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
            } else {
                // 返回失败响应给微信
                return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[处理失败]]></return_msg></xml>";
            }
        } catch (Exception e) {
            log.error("处理微信支付通知异常", e);
            return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[系统异常]]></return_msg></xml>";
        }
    }

    /**
     * 查询支付状态
     */
    @GetMapping("/status/{orderNo}")
    @Operation(summary = "查询支付状态", description = "查询订单支付状态")
    public Result<Map<String, Object>> queryPayStatus(@PathVariable String orderNo) {
        try {
            String status = wechatPayService.queryPayStatus(orderNo);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("orderNo", orderNo);
            result.put("payStatus", status);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询支付状态失败，订单号：{}", orderNo, e);
            return Result.error("查询支付状态失败");
        }
    }

    /**
     * 读取请求体内容
     */
    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}