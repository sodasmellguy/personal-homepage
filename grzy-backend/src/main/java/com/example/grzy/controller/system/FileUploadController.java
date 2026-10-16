package com.example.grzy.controller.system;

import com.example.grzy.common.result.Result;
import com.example.grzy.common.utils.ClientIpUtils;
import com.example.grzy.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 文件上传控制器
 *
 * @author sodasmell
 */
@Tag(name = "文件上传", description = "文件上传接口")
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;
    private final StringRedisTemplate stringRedisTemplate;

    /** 图片上传频率限制：同一 IP 每小时最多上传 20 次 */
    private static final long UPLOAD_RATE_LIMIT_COUNT = 20;
    /** 频控窗口：1 小时 */
    private static final long UPLOAD_RATE_LIMIT_HOURS = 1;

    @Operation(summary = "上传文件（统一）", description = "上传任意文件（图片/文件），返回访问URL")
    @PostMapping
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadFile(file);
        return Result.success(Collections.singletonMap("url", url));
    }

    @Operation(summary = "上传图片", description = "上传图片文件，返回访问URL")
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        // IP 频率限制：该接口对访客开放（无需登录），加频控防止脚本无限上传撑爆磁盘
        // （Redis INCR 计数，首次访问设置 1 小时过期，支持多实例部署）
        String clientIp = ClientIpUtils.getClientIp();
        String rateLimitKey = "rate_limit:upload:" + clientIp;
        Long count = stringRedisTemplate.opsForValue().increment(rateLimitKey);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(rateLimitKey, UPLOAD_RATE_LIMIT_HOURS, TimeUnit.HOURS);
        }
        if (count != null && count > UPLOAD_RATE_LIMIT_COUNT) {
            return Result.error("上传太频繁，请稍后再试");
        }

        String url = fileUploadService.uploadImage(file);
        return Result.success(Collections.singletonMap("url", url));
    }

    @Operation(summary = "上传文件", description = "上传任意文件，返回访问URL")
    @PostMapping("/file")
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadFile(file);
        return Result.success(Collections.singletonMap("url", url));
    }
}
