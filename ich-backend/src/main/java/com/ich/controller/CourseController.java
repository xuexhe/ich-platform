// src/main/java/com/ich/controller/CourseController.java
package com.ich.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ich.entity.Course;
import com.ich.service.CourseService;
import com.ich.utils.FileUploadUtils;
import com.ich.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final FileUploadUtils fileUploadUtils;

    @GetMapping("/list")
    public Result<?> getCourseList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "12") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        Page<Course> page = courseService.getCourseList(pageNum, pageSize, category, keyword);
        return Result.success(page);
    }

    @GetMapping("/detail/{id}")
    public Result<?> getCourseDetail(@PathVariable Long id) {
        Course course = courseService.getCourseDetail(id);
        return course != null ? Result.success(course) : Result.error("课程不存在");
    }

    @GetMapping("/hot")
    public Result<?> getHotCourses(@RequestParam(defaultValue = "6") Integer limit) {
        return Result.success(courseService.getHotCourses(limit));
    }

    @GetMapping("/categories")
    public Result<?> getCategories() {
        return Result.success(courseService.getAllCategories());
    }

    @PostMapping("/progress")
    public Result<?> updateProgress(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        Long courseId = Long.valueOf(params.get("courseId").toString());
        Integer progress = (Integer) params.get("progress");
        
        Map<String, Object> result = courseService.updateProgress(userId, courseId, progress);
        return Result.success(result);
    }

    @GetMapping("/progress/{userId}/{courseId}")
    public Result<?> getProgress(@PathVariable Long userId, @PathVariable Long courseId) {
        return Result.success(courseService.getUserProgress(userId, courseId));
    }

    @GetMapping("/stats/{userId}")
    public Result<?> getUserStats(@PathVariable Long userId) {
        return Result.success(courseService.getUserStats(userId));
    }

    // ========== 新增：视频上传接口 ==========
    @PostMapping("/upload/video")
    public Result<?> uploadVideo(@RequestParam("file") MultipartFile file) {
        try {
            // 验证视频文件
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.startsWith("video/") && !contentType.equals("application/x-mpegURL"))) {
                return Result.error("请上传视频文件");
            }
            
            // 限制视频大小（100MB）
            if (file.getSize() > 100 * 1024 * 1024) {
                return Result.error("视频文件不能超过100MB");
            }
            
            String videoUrl = fileUploadUtils.uploadVideo(file);
            Map<String, String> result = new HashMap<>();
            result.put("url", videoUrl);
            return Result.success(result);
        } catch (IOException e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }
    
    // ========== 新增：图片上传接口（课程封面） ==========
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

    /**
     * 点赞课程
     */
    @PostMapping("/like")
    public Result<?> likeCourse(@RequestBody Map<String, Long> params) {
        Long courseId = params.get("courseId");
        Long userId = params.get("userId");
        
        if (userId == null) {
            return Result.error("请先登录");
        }
        
        boolean success = courseService.likeCourse(courseId, userId);
        return success ? Result.success("点赞成功") : Result.error("已经点过赞了");
    }

    /**
     * 取消点赞
     */
    @PostMapping("/unlike")
    public Result<?> unlikeCourse(@RequestBody Map<String, Long> params) {
        Long courseId = params.get("courseId");
        Long userId = params.get("userId");
        
        if (userId == null) {
            return Result.error("请先登录");
        }
        
        boolean success = courseService.unlikeCourse(courseId, userId);
        return success ? Result.success("取消点赞") : Result.error("操作失败");
    }

    /**
     * 检查是否已点赞
     */
    @GetMapping("/liked/{courseId}")
    public Result<?> isLiked(@PathVariable Long courseId, @RequestParam Long userId) {
        boolean isLiked = courseService.isLiked(courseId, userId);
        return Result.success(isLiked);
    }
}