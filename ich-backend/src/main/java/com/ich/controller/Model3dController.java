// src/main/java/com/ich/controller/Model3dController.java
package com.ich.controller;

import com.ich.entity.Model3d;
import com.ich.service.Model3dService;
import com.ich.utils.FileUploadUtils;
import com.ich.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/model")
@RequiredArgsConstructor
public class Model3dController {

    private final Model3dService model3dService;
    private final FileUploadUtils fileUploadUtils;

    /**
     * 获取所有可用模型（前台）
     */
    @GetMapping("/list")
    public Result<?> getModelList() {
        List<Model3d> models = model3dService.getAllEnabledModels();
        return Result.success(models);
    }

    /**
     * 获取模型详情
     */
    @GetMapping("/detail/{id}")
    public Result<?> getModelDetail(@PathVariable Long id) {
        Model3d model = model3dService.getModelDetail(id);
        return model != null ? Result.success(model) : Result.error("模型不存在");
    }

    /**
     * 上传3D模型文件（.glb / .gltf）
     */
    @PostMapping("/upload/model")
    public Result<?> uploadModelFile(@RequestParam("file") MultipartFile file) {
        try {
            String contentType = file.getContentType();
            String originalFilename = file.getOriginalFilename();
            
            // 检查文件类型
            if (originalFilename == null || 
                (!originalFilename.endsWith(".glb") && !originalFilename.endsWith(".gltf") && !originalFilename.endsWith(".obj"))) {
                return Result.error("请上传 .glb、.gltf 或 .obj 格式的3D模型文件");
            }
            
            // 限制文件大小（200MB）
            if (file.getSize() > 200 * 1024 * 1024) {
                return Result.error("模型文件不能超过200MB");
            }
            
            String modelUrl = fileUploadUtils.upload3dModel(file);
            Map<String, String> result = new HashMap<>();
            result.put("url", modelUrl);
            return Result.success(result);
        } catch (IOException e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传模型封面图
     */
    @PostMapping("/upload/cover")
    public Result<?> uploadCover(@RequestParam("file") MultipartFile file) {
        try {
            String url = fileUploadUtils.uploadImage(file);
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            return Result.success(result);
        } catch (IOException e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}