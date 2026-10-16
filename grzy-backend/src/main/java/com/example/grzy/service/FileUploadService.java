package com.example.grzy.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务
 *
 * @author sodasmell
 */
public interface FileUploadService {

    /**
     * 上传图片文件
     *
     * @param file 文件
     * @return 访问 URL
     */
    String uploadImage(MultipartFile file);

    /**
     * 上传任意文件
     *
     * @param file 文件
     * @return 访问 URL
     */
    String uploadFile(MultipartFile file);
}
