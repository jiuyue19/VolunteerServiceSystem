package com.volunteer.controller;

import com.volunteer.common.Result;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class UploadController {

    @Value("${file.upload.path:/uploads/}")
    private String uploadPath;

    @Value("${server.port:8080}")
    private String serverPort;

    @PostMapping("/image")
    public Result<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        System.out.println("收到上传请求，文件名: " + (file != null ? file.getOriginalFilename() : "null")); // 添加日志
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("只能上传图片文件");
            }

            // 检查文件大小（2MB）
            if (file.getSize() > 2 * 1024 * 1024) {
                return Result.error("文件大小不能超过2MB");
            }

            // 创建上传目录
            String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fullUploadPath = uploadPath + dateFolder;
            Path uploadDir = Paths.get(fullUploadPath);
            System.out.println("创建上传目录: " + fullUploadPath); // 添加日志
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                System.out.println("目录创建成功: " + fullUploadPath);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            System.out.println("文件保存到: " + filePath.toAbsolutePath()); // 添加日志

            // 构建访问URL
            String fileUrl = "/uploads/" + dateFolder + "/" + filename;
            System.out.println("文件访问URL: " + fileUrl); // 添加日志

            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("filename", filename);
            result.put("originalName", originalFilename);
            result.put("size", file.getSize());

            System.out.println("文件上传成功: " + fileUrl); // 添加日志
            return Result.success(result);

        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}
