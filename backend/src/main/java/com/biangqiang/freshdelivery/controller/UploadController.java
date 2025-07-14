package com.biangqiang.freshdelivery.controller;

import com.biangqiang.freshdelivery.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 文件上传控制器
 *
 * @author biangqiang
 * @since 2024-01-01
 */

@Tag(name = "文件上传", description = "文件上传相关接口")
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${upload.path:./uploads}")
    private String uploadPath;
    
    @Value("${file.upload.domain:http://localhost:8081}")
    private String domain;

    @Operation(summary = "上传图片", description = "上传图片文件，支持jpg、png、gif格式")
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件
            if (file.isEmpty()) {
                return Result.error("请选择要上传的文件");
            }

            // 验证文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !isImageFile(originalFilename)) {
                return Result.error("只支持jpg、png、gif格式的图片文件");
            }

            // 验证文件大小（5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                return Result.error("文件大小不能超过5MB");
            }

            // 创建上传目录
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String uploadDir = uploadPath + "/images/" + datePath;
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成新文件名
            String extension = getFileExtension(originalFilename);
            String newFilename = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + extension;
            String filePath = uploadDir + "/" + newFilename;

            // 保存文件
            Path path = Paths.get(filePath);
            Files.write(path, file.getBytes());

            // 构建返回URL
            String relativePath = "/uploads/images/" + datePath + "/" + newFilename;
            String fullUrl = domain + "/api" + relativePath;

            Map<String, String> result = new HashMap<>();
            result.put("url", relativePath);
            result.put("fullUrl", fullUrl);
            result.put("filename", newFilename);
            result.put("originalName", originalFilename);
            result.put("size", String.valueOf(file.getSize()));

            // 文件上传成功
            return Result.success(result);
        } catch (IOException e) {
            // 文件上传失败
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量上传图片", description = "批量上传多个图片文件")
    @PostMapping("/images")
    public Result<Map<String, Object>> uploadImages(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Result.error("请选择要上传的文件");
        }

        if (files.length > 10) {
            return Result.error("一次最多只能上传10个文件");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", files.length);
        result.put("success", 0);
        result.put("failed", 0);
        result.put("files", new ArrayList<>());
        result.put("errors", new ArrayList<>());

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            try {
                Result<Map<String, String>> uploadResult = uploadImage(file);
                if (uploadResult.getCode() == 200) {
                    ((List<Map<String, String>>) result.get("files")).add(uploadResult.getData());
                    result.put("success", (Integer) result.get("success") + 1);
                } else {
                    ((List<String>) result.get("errors")).add("文件" + (i + 1) + ": " + uploadResult.getMessage());
                    result.put("failed", (Integer) result.get("failed") + 1);
                }
            } catch (Exception e) {
                ((List<String>) result.get("errors")).add("文件" + (i + 1) + ": " + e.getMessage());
                result.put("failed", (Integer) result.get("failed") + 1);
            }
        }

        return Result.success(result);
    }

    /**
     * 验证是否为图片文件
     */
    private boolean isImageFile(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return "jpg".equals(extension) || "jpeg".equals(extension) || 
               "png".equals(extension) || "gif".equals(extension) || 
               "bmp".equals(extension) || "webp".equals(extension);
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }
}