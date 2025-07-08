package com.biangqiang.freshdelivery.controller;

import com.biangqiang.freshdelivery.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 文件上传控制器
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Slf4j
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
                return Result.error("文件不能为空");
            }

            // 验证文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !isImageFile(originalFilename)) {
                return Result.error("只支持jpg、png、gif格式的图片");
            }

            // 验证文件大小（5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                return Result.error("文件大小不能超过5MB");
            }

            // 生成文件名
            String fileExtension = getFileExtension(originalFilename);
            String fileName = UUID.randomUUID().toString() + "." + fileExtension;

            // 创建日期目录
            String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String relativePath = "images/" + dateDir;
            String fullPath = uploadPath + "/" + relativePath;

            // 创建目录
            File directory = new File(fullPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 保存文件
            File targetFile = new File(directory, fileName);
            file.transferTo(targetFile);

            // 构建返回数据
            String relativeUrl = "/uploads/" + relativePath + "/" + fileName;
            String fullUrl = domain + "/api" + relativeUrl;

            Map<String, String> result = new HashMap<>();
            result.put("url", relativeUrl);
            result.put("fullUrl", fullUrl);
            result.put("fileName", fileName);
            result.put("originalName", originalFilename);
            result.put("size", String.valueOf(file.getSize()));

            log.info("文件上传成功: {} -> {}", originalFilename, relativeUrl);
            return Result.success(result);

        } catch (IOException e) {
            log.error("文件上传失败", e);
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
        result.put("files", new java.util.ArrayList<>());
        result.put("errors", new java.util.ArrayList<>());

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            try {
                Result<Map<String, String>> uploadResult = uploadImage(file);
                if (uploadResult.getCode() == 200) {
                    ((java.util.List<Map<String, String>>) result.get("files")).add(uploadResult.getData());
                    result.put("success", (Integer) result.get("success") + 1);
                } else {
                    ((java.util.List<String>) result.get("errors")).add("文件" + (i + 1) + ": " + uploadResult.getMessage());
                    result.put("failed", (Integer) result.get("failed") + 1);
                }
            } catch (Exception e) {
                ((java.util.List<String>) result.get("errors")).add("文件" + (i + 1) + ": " + e.getMessage());
                result.put("failed", (Integer) result.get("failed") + 1);
            }
        }

        return Result.success(result);
    }

    @Operation(summary = "获取图片列表", description = "分页获取已上传的图片列表")
    @GetMapping("/images/list")
    public Result<Map<String, Object>> getImageList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        try {
            // 获取图片目录
            String imagesPath = uploadPath + "/images";
            File imagesDir = new File(imagesPath);
            
            if (!imagesDir.exists()) {
                Map<String, Object> result = new HashMap<>();
                result.put("list", new ArrayList<>());
                result.put("total", 0);
                result.put("page", page);
                result.put("size", size);
                return Result.success(result);
            }
            
            // 递归获取所有图片文件
            List<Map<String, Object>> imageList = new ArrayList<>();
            collectImages(imagesDir, imageList, keyword);
            
            // 按修改时间倒序排序
            imageList.sort((a, b) -> ((Long) b.get("lastModified")).compareTo((Long) a.get("lastModified")));
            
            // 分页处理
            int total = imageList.size();
            int start = (page - 1) * size;
            int end = Math.min(start + size, total);
            
            List<Map<String, Object>> pageList = start < total ? 
                imageList.subList(start, end) : new ArrayList<>();
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageList);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", (total + size - 1) / size);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取图片列表失败", e);
            return Result.error("获取图片列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除图片", description = "删除指定的图片文件")
    @DeleteMapping("/images")
    public Result<String> deleteImage(@RequestParam String url) {
        try {
            // 验证URL格式
            if (!url.startsWith("/uploads/")) {
                return Result.error("无效的图片URL");
            }
            
            // 构建文件路径
            String relativePath = url.substring("/uploads/".length());
            String filePath = uploadPath + "/" + relativePath;
            File file = new File(filePath);
            
            if (!file.exists()) {
                return Result.error("文件不存在");
            }
            
            if (file.delete()) {
                log.info("删除图片成功: {}", url);
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除图片失败: url={}", url, e);
            return Result.error("删除图片失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量删除图片", description = "批量删除多个图片文件")
    @DeleteMapping("/images/batch")
    public Result<Map<String, Object>> batchDeleteImages(@RequestBody Map<String, List<String>> request) {
        List<String> urls = request.get("urls");
        if (urls == null || urls.isEmpty()) {
            return Result.error("请提供要删除的图片URL列表");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", urls.size());
        result.put("success", 0);
        result.put("failed", 0);
        result.put("errors", new ArrayList<String>());
        
        for (String url : urls) {
            try {
                Result<String> deleteResult = deleteImage(url);
                if (deleteResult.getCode() == 200) {
                    result.put("success", (Integer) result.get("success") + 1);
                } else {
                    result.put("failed", (Integer) result.get("failed") + 1);
                    ((List<String>) result.get("errors")).add(url + ": " + deleteResult.getMessage());
                }
            } catch (Exception e) {
                result.put("failed", (Integer) result.get("failed") + 1);
                ((List<String>) result.get("errors")).add(url + ": " + e.getMessage());
            }
        }
        
        return Result.success(result);
    }

    /**
     * 递归收集图片文件信息
     */
    private void collectImages(File dir, List<Map<String, Object>> imageList, String keyword) {
        File[] files = dir.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                collectImages(file, imageList, keyword);
            } else if (file.isFile() && isImageFile(file.getName())) {
                // 关键词过滤
                if (keyword != null && !keyword.trim().isEmpty() && 
                    !file.getName().toLowerCase().contains(keyword.toLowerCase())) {
                    continue;
                }
                
                Map<String, Object> imageInfo = new HashMap<>();
                String relativePath = file.getAbsolutePath().substring(uploadPath.length()).replace("\\", "/");
                String url = "/uploads" + relativePath;
                String fullUrl = domain + "/api" + url;
                
                imageInfo.put("name", file.getName());
                imageInfo.put("url", url);
                imageInfo.put("fullUrl", fullUrl);
                imageInfo.put("size", file.length());
                imageInfo.put("lastModified", file.lastModified());
                imageInfo.put("lastModifiedDate", new Date(file.lastModified()));
                
                imageList.add(imageInfo);
            }
        }
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