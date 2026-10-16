package com.example.grzy.service.impl;

import com.example.grzy.common.exception.BusinessException;
import com.example.grzy.common.result.ResultCode;
import com.example.grzy.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传服务实现 — 本地存储
 *
 * @author sodasmell
 */
@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.upload.max-size:10485760}")
    private long maxSize;

    /**
     * 图片上传扩展名白名单（/api/upload/image 使用）。
     * 注意：svg 因可内嵌 <script>（存储型 XSS 风险）被排除在外，
     * ico/bmp 虽然冷门但属于纯位图格式，安全可用。
     */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp", ".ico"
    );

    /**
     * 通用上传扩展名白名单（/api/upload、/api/upload/file 使用）。
     * 原实现为「黑名单」校验，但黑名单无法穷举危险类型，.html/.svg/.xml/.hta/.css 等
     * 可被浏览器内联解析的文件落盘同源 /uploads/ 目录后会构成存储型 XSS。
     * 故改为「白名单」：仅放行图片 + 常见文档/媒体类型，其余一律拒绝。
     * 明确排除：svg/html/htm/xml/js/mjs/css/hta/htc/xsl/xslt/swf
     * 及 exe/bat/cmd/sh/php/jsp 等一切可执行或可内联脚本的扩展名。
     */
    private static final Set<String> FILE_ALLOWED_EXTENSIONS = Set.of(
            // 图片（与图片白名单一致，不含 svg）
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp", ".ico",
            // 常见文档
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".md", ".csv",
            // 压缩包
            ".zip",
            // 常见音视频
            ".mp4", ".mp3", ".wav"
    );

    /**
     * 扩展名 -> 允许的 Content-Type 集合。
     * Content-Type 必须与扩展名匹配才允许上传（客户端可控的请求头，
     * 单独校验可被伪造，因此仅作为扩展名白名单之外的一致性校验）。
     */
    private static final Map<String, Set<String>> EXT_CONTENT_TYPES = Map.ofEntries(
            Map.entry(".jpg", Set.of("image/jpeg", "image/pjpeg")),
            Map.entry(".jpeg", Set.of("image/jpeg", "image/pjpeg")),
            Map.entry(".png", Set.of("image/png")),
            Map.entry(".gif", Set.of("image/gif")),
            Map.entry(".webp", Set.of("image/webp")),
            Map.entry(".bmp", Set.of("image/bmp", "image/x-ms-bmp")),
            Map.entry(".ico", Set.of("image/x-icon", "image/vnd.microsoft.icon", "image/icon")),
            Map.entry(".pdf", Set.of("application/pdf")),
            Map.entry(".doc", Set.of("application/msword")),
            Map.entry(".docx", Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
            Map.entry(".xls", Set.of("application/vnd.ms-excel")),
            Map.entry(".xlsx", Set.of("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
            Map.entry(".ppt", Set.of("application/vnd.ms-powerpoint")),
            Map.entry(".pptx", Set.of("application/vnd.openxmlformats-officedocument.presentationml.presentation")),
            Map.entry(".txt", Set.of("text/plain")),
            Map.entry(".md", Set.of("text/markdown", "text/x-markdown", "text/plain")),
            Map.entry(".csv", Set.of("text/csv", "text/plain", "application/vnd.ms-excel")),
            Map.entry(".zip", Set.of("application/zip", "application/x-zip-compressed", "application/x-zip")),
            Map.entry(".mp4", Set.of("video/mp4")),
            Map.entry(".mp3", Set.of("audio/mpeg", "audio/mp3")),
            Map.entry(".wav", Set.of("audio/wav", "audio/x-wav"))
    );

    @Override
    public String uploadImage(MultipartFile file) {
        // 1. 校验文件
        validateFile(file);

        // 2. 生成存储路径: uploads/2026/06/uuid.ext
        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;

        // 3. 创建目录并保存文件
        try {
            Path targetDir = Paths.get(uploadPath, datePath);
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(newFilename);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            // 4. 返回相对访问路径: /uploads/2026/06/xxx.jpg
            String relativePath = "/uploads/" + datePath + "/" + newFilename;
            log.info("文件上传成功: {}", relativePath);
            return relativePath;
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.ERROR, "文件上传失败");
        }
    }

    @Override
    public String uploadFile(MultipartFile file) {
        // 1. 基础校验（大小）
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.VALID_ERROR, "请选择要上传的文件");
        }
        if (file.getSize() > maxSize) {
            throw new BusinessException(ResultCode.VALID_ERROR, "文件大小不能超过 " + (maxSize / 1024 / 1024) + "MB");
        }

        // 2. 扩展名白名单校验：文件必须带扩展名，且在白名单内
        //    （svg/html/htm/xml/js/css/hta 等可内联脚本类型不在白名单，直接拒绝）
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename) || !originalFilename.contains(".")) {
            throw new BusinessException(ResultCode.VALID_ERROR, "文件缺少扩展名，已拒绝上传");
        }
        String extension = getExtension(originalFilename);
        if (!FILE_ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ResultCode.VALID_ERROR, "文件类型不允许上传：" + extension);
        }

        // 3. Content-Type 一致性校验：必须与扩展名匹配（或为二进制流兜底值），不匹配拒绝
        if (!isContentTypeAllowed(extension, file.getContentType(), false)) {
            throw new BusinessException(ResultCode.VALID_ERROR, "文件类型与扩展名不匹配，已拒绝上传");
        }

        // 4. 生成存储路径
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;

        // 5. 创建目录并保存文件
        try {
            Path targetDir = Paths.get(uploadPath, datePath);
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(newFilename);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            String relativePath = "/uploads/" + datePath + "/" + newFilename;
            log.info("文件上传成功: {}", relativePath);
            return relativePath;
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.ERROR, "文件上传失败");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.VALID_ERROR, "请选择要上传的文件");
        }

        // 文件大小
        if (file.getSize() > maxSize) {
            throw new BusinessException(ResultCode.VALID_ERROR, "文件大小不能超过 " + (maxSize / 1024 / 1024) + "MB");
        }

        // 扩展名白名单
        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ResultCode.VALID_ERROR, "不支持的文件扩展名: " + extension);
        }

        // Content-Type 必须为 image/ 前缀的真实图片类型，且与扩展名匹配，不匹配拒绝
        // （Content-Type 是客户端可控的请求头，可被伪造，真实内容由下方魔数校验兜底）
        String contentType = file.getContentType();
        Set<String> allowedMimeTypes = EXT_CONTENT_TYPES.get(extension);
        if (!StringUtils.hasText(contentType) || allowedMimeTypes == null
                || !allowedMimeTypes.contains(contentType.toLowerCase())) {
            throw new BusinessException(ResultCode.VALID_ERROR, "不支持的文件类型: " + contentType);
        }

        // 魔数校验：Content-Type 是客户端可控的请求头，可被伪造，
        // 必须检查文件真实字节头，防止把任意内容伪装成图片上传
        if (!matchesMagicNumber(file, extension)) {
            throw new BusinessException(ResultCode.VALID_ERROR, "文件内容与声明类型不符，已拒绝上传");
        }
    }

    /**
     * 校验 Content-Type 与扩展名是否匹配（依据 EXT_CONTENT_TYPES 映射表）
     *
     * @param strictImage 图片入口传 true：严格要求匹配映射表（均为 image/ 前缀类型）；
     *                    通用上传传 false：额外放行 application/octet-stream
     *                    （部分客户端对不认识的扩展名统一发送该值，属中性类型，
     *                    危险类型已被扩展名白名单拦截）
     */
    private boolean isContentTypeAllowed(String extension, String contentType, boolean strictImage) {
        if (!StringUtils.hasText(contentType)) {
            return false;
        }
        String type = contentType.toLowerCase();
        if (!strictImage && "application/octet-stream".equals(type)) {
            return true;
        }
        Set<String> allowedMimeTypes = EXT_CONTENT_TYPES.get(extension);
        return allowedMimeTypes != null && allowedMimeTypes.contains(type);
    }

    /**
     * 校验文件真实字节头是否与声明的图片扩展名匹配
     */
    private boolean matchesMagicNumber(MultipartFile file, String extension) {
        try (java.io.InputStream is = file.getInputStream()) {
            byte[] header = new byte[12];
            int read = is.read(header);
            if (read < 4) {
                return false;
            }
            switch (extension) {
                case ".jpg":
                case ".jpeg":
                    return (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF;
                case ".png":
                    return (header[0] & 0xFF) == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47;
                case ".gif":
                    return header[0] == 'G' && header[1] == 'I' && header[2] == 'F' && header[3] == '8';
                case ".webp":
                    return header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                            && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
                case ".bmp":
                    return header[0] == 'B' && header[1] == 'M';
                case ".ico":
                    // ICONDIR：保留字段 0x0000 + 类型字段 0x0001（小端）
                    return header[0] == 0x00 && header[1] == 0x00 && header[2] == 0x01 && header[3] == 0x00;
                default:
                    return false;
            }
        } catch (IOException e) {
            log.error("读取上传文件失败: {}", e.getMessage());
            return false;
        }
    }

    private String getExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }
}
