// src/main/java/com/ich/utils/FileUploadUtils.java
package com.ich.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class FileUploadUtils {
    
    @Value("${upload.path}")
    private String uploadPath;
    
    @Value("${upload.url}")
    private String uploadUrl;
    
    // 允许的图片类型
    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/png", "image/jpg", "image/gif", "image/webp"};
    
    // 最大文件大小 200MB
    private static final long MAX_FILE_SIZE = 200 * 1024 * 1024;
    
    /**
     * 获取绝对路径
     */
    private String getAbsoluteUploadPath() {
        String path = uploadPath;
        // 如果是相对路径，转换为绝对路径
        if (path.startsWith("./")) {
            path = System.getProperty("user.dir") + File.separator + path.substring(2);
        } else if (path.startsWith(".\\")) {
            path = System.getProperty("user.dir") + File.separator + path.substring(2);
        }
        // 确保路径以分隔符结尾
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        return path;
    }
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("文件大小不能超过5MB");
        }
        
        String contentType = file.getContentType();
        boolean isAllowed = false;
        for (String type : ALLOWED_TYPES) {
            if (type.equals(contentType)) {
                isAllowed = true;
                break;
            }
        }
        
        if (!isAllowed) {
            throw new RuntimeException("只支持 JPG、PNG、GIF、WEBP 格式的图片");
        }
    }
    
    /**
     * 上传头像
     */
    public String uploadAvatar(MultipartFile file, Long userId) throws IOException {
        validateFile(file);
        
        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = "avatar_" + userId + "_" + System.currentTimeMillis() + extension;
        
        // 获取绝对路径
        String basePath = getAbsoluteUploadPath();
        String avatarsDirPath = basePath + "avatars";
        
        // 创建目录
        File avatarsDir = new File(avatarsDirPath);
        if (!avatarsDir.exists()) {
            boolean created = avatarsDir.mkdirs();
            if (!created) {
                throw new IOException("无法创建目录: " + avatarsDirPath);
            }
            System.out.println("创建目录成功: " + avatarsDirPath);
        }
        
        // 保存文件
        File targetFile = new File(avatarsDir, filename);
        file.transferTo(targetFile);
        
        System.out.println("文件保存成功: " + targetFile.getAbsolutePath());
        
        // 返回相对路径（不是绝对路径）
        return uploadUrl + "avatars/" + filename;
    }
    
    /**
     * 通用图片上传
     */
    public String uploadImage(MultipartFile file) throws IOException {
        validateFile(file);
        
        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + extension;
        
        // 获取绝对路径
        String basePath = getAbsoluteUploadPath();
        String imagesDirPath = basePath + "images";
        
        // 创建目录
        File imagesDir = new File(imagesDirPath);
        if (!imagesDir.exists()) {
            boolean created = imagesDir.mkdirs();
            if (!created) {
                throw new IOException("无法创建目录: " + imagesDirPath);
            }
            System.out.println("创建目录成功: " + imagesDirPath);
        }
        
        // 保存文件
        File targetFile = new File(imagesDir, filename);
        file.transferTo(targetFile);
        
        System.out.println("图片保存成功: " + targetFile.getAbsolutePath());
        
        // 返回相对路径
        return uploadUrl + "images/" + filename;
    }
    
    /**
     * 删除旧头像
     */
    public void deleteOldAvatar(String avatarUrl) {
        if (avatarUrl == null || !avatarUrl.startsWith(uploadUrl)) {
            return;
        }
        
        try {
            String relativePath = avatarUrl.substring(uploadUrl.length());
            String basePath = getAbsoluteUploadPath();
            File file = new File(basePath + relativePath);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    System.out.println("删除旧头像成功: " + file.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            System.err.println("删除旧头像失败: " + e.getMessage());
        }
    }

    /**
     * 上传视频
     */
    public String uploadVideo(MultipartFile file) throws IOException {
        // 验证文件
        validateVideoFile(file);
        
        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = "video_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
        
        // 获取绝对路径
        String basePath = getAbsoluteUploadPath();
        String videosDirPath = basePath + "videos";
        
        // 创建目录
        File videosDir = new File(videosDirPath);
        if (!videosDir.exists()) {
            boolean created = videosDir.mkdirs();
            if (!created) {
                throw new IOException("无法创建目录: " + videosDirPath);
            }
            System.out.println("创建视频目录成功: " + videosDirPath);
        }
        
        // 保存文件
        File targetFile = new File(videosDir, filename);
        file.transferTo(targetFile);
        
        System.out.println("视频保存成功: " + targetFile.getAbsolutePath());
        
        // 返回相对路径
        return uploadUrl + "videos/" + filename;
    }

    /**
     * 验证视频文件
     */
    private void validateVideoFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        if (file.getSize() > 100 * 1024 * 1024) {
            throw new RuntimeException("视频文件不能超过100MB");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("video/") && !contentType.equals("application/x-mpegURL"))) {
            throw new RuntimeException("只支持视频格式文件（MP4、AVI、MOV等）");
        }
    }

    /**
     * 上传3D模型文件
     */
    public String upload3dModel(MultipartFile file) throws IOException {
        // 验证文件
        validate3dModelFile(file);
        
        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = "model_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
        
        // 获取绝对路径
        String basePath = getAbsoluteUploadPath();
        String modelsDirPath = basePath + "models";
        
        // 创建目录
        File modelsDir = new File(modelsDirPath);
        if (!modelsDir.exists()) {
            boolean created = modelsDir.mkdirs();
            if (!created) {
                throw new IOException("无法创建目录: " + modelsDirPath);
            }
            System.out.println("创建3D模型目录成功: " + modelsDirPath);
        }
        
        // 保存文件
        File targetFile = new File(modelsDir, filename);
        file.transferTo(targetFile);
        
        System.out.println("3D模型保存成功: " + targetFile.getAbsolutePath());
        
        // 返回相对路径
        return uploadUrl + "models/" + filename;
    }

    /**
     * 验证3D模型文件
     */
    private void validate3dModelFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new RuntimeException("模型文件不能超过50MB");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("文件名不能为空");
        }
        
        String lowerName = originalFilename.toLowerCase();
        if (!lowerName.endsWith(".glb") && !lowerName.endsWith(".gltf") && !lowerName.endsWith(".obj")) {
            throw new RuntimeException("只支持 .glb、.gltf、.obj 格式的3D模型文件");
        }
    }

}