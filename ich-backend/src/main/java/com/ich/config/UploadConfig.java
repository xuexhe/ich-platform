package com.ich.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class UploadConfig implements WebMvcConfigurer {

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.url}")
    private String uploadUrl;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 获取绝对路径
        String absolutePath = getAbsoluteUploadPath();

        // 配置静态资源映射，使上传的文件可以通过URL访问
        registry.addResourceHandler(uploadUrl + "**")
                .addResourceLocations("file:" + absolutePath);

        System.out.println("上传文件目录: " + absolutePath);
        System.out.println("访问URL前缀: " + uploadUrl);
    }

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
}