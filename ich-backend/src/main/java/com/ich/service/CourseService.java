// src/main/java/com/ich/service/CourseService.java
package com.ich.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ich.entity.Achievement;
import com.ich.entity.Course;
import com.ich.entity.CourseLike;
import com.ich.entity.UserCourseProgress;
import com.ich.mapper.CourseLikeMapper;
import com.ich.mapper.CourseMapper;
import com.ich.mapper.UserCourseProgressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService extends ServiceImpl<CourseMapper, Course> {

    private final CourseMapper courseMapper;
    private final UserCourseProgressMapper progressMapper;
    private final CourseLikeMapper courseLikeMapper;
    private final AchievementService achievementService;

    // ========== 课程基础功能 ==========
    
    public Page<Course> getCourseList(Integer pageNum, Integer pageSize, String category, String keyword) {
        Page<Course> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getStatus, 1);
        
        if (category != null && !"all".equals(category)) {
            wrapper.eq(Course::getCategory, category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Course::getTitle, keyword);
        }
        wrapper.orderByDesc(Course::getCreateTime);
        return page(page, wrapper);
    }

    public Page<Course> getAdminList(Integer pageNum, Integer pageSize, String keyword) {
        Page<Course> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Course::getTitle, keyword);
        }
        wrapper.orderByDesc(Course::getCreateTime);
        return page(page, wrapper);
    }

    public boolean saveCourse(Course course) {
        if (course.getId() == null) {
            course.setViewCount(0);
            course.setLikeCount(0);
            course.setStatus(1);
            course.setCreateTime(LocalDateTime.now());
        }
        course.setUpdateTime(LocalDateTime.now());
        return saveOrUpdate(course);
    }

    public Course getCourseDetail(Long id) {
        Course course = getById(id);
        if (course != null && course.getStatus() == 1) {
            course.setViewCount((course.getViewCount() == null ? 0 : course.getViewCount()) + 1);
            updateById(course);
        }
        return course;
    }

    public List<Course> getHotCourses(Integer limit) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getStatus, 1)
               .orderByDesc(Course::getViewCount)
               .last("limit " + limit);
        return list(wrapper);
    }

    public List<String> getAllCategories() {
        List<Course> courses = list(new LambdaQueryWrapper<Course>().eq(Course::getStatus, 1));
        return courses.stream().map(Course::getCategory).distinct().toList();
    }

    // ========== 学习进度功能 ==========
    
    @Transactional
    public Map<String, Object> updateProgress(Long userId, Long courseId, Integer progress) {
        Map<String, Object> result = new HashMap<>();
        
        UserCourseProgress userProgress = progressMapper.selectByUserAndCourse(userId, courseId);
        
        if (userProgress == null) {
            // 首次学习，创建记录
            userProgress = new UserCourseProgress();
            userProgress.setUserId(userId);
            userProgress.setCourseId(courseId);
            userProgress.setProgress(progress);
            userProgress.setIsCompleted(progress >= 100 ? 1 : 0);
            userProgress.setLastWatchTime(LocalDateTime.now());
            if (progress >= 100) {
                userProgress.setCompleteTime(LocalDateTime.now());
            }
            progressMapper.insert(userProgress);
            
            // 如果直接完成100%，触发成就检查
            if (progress >= 100) {
                checkAndGrantCourseAchievements(userId);
                result.put("isNewCompleted", true);
            }
        } else {
            // 更新已有进度
            boolean wasCompleted = userProgress.getIsCompleted() == 1;
            userProgress.setProgress(progress);
            userProgress.setLastWatchTime(LocalDateTime.now());
            
            // 刚刚完成课程（进度达到100且之前未完成）
            if (progress >= 100 && !wasCompleted) {
                userProgress.setIsCompleted(1);
                userProgress.setCompleteTime(LocalDateTime.now());
                result.put("isNewCompleted", true);
                
                // 触发成就检查
                checkAndGrantCourseAchievements(userId);
            }
            progressMapper.updateById(userProgress);
        }
        
        result.put("progress", progress);
        result.put("isCompleted", progress >= 100);
        return result;
    }
    
    /**
     * 检查并授予课程相关成就
     */
    private void checkAndGrantCourseAchievements(Long userId) {
        if (achievementService == null) {
            System.err.println("成就服务未注入");
            return;
        }
        
        try {
            // 获取用户完成的课程总数
            Integer completedCount = progressMapper.countCompletedCourses(userId);
            System.out.println("========== 成就检查触发 ==========");
            System.out.println("用户ID: " + userId);
            System.out.println("已完成课程数量: " + completedCount);
            
            // 触发成就检查（course_count 类型）
            List<Achievement> newAchievements = achievementService.checkAndGrantAchievements(
                userId, 
                "course_count", 
                completedCount != null ? completedCount.longValue() : 0L
            );
            
            if (!newAchievements.isEmpty()) {
                String achievementNames = newAchievements.stream()
                    .map(Achievement::getName)
                    .collect(Collectors.joining(", "));
                System.out.println("新获得成就: " + achievementNames);
            } else {
                System.out.println("未获得新成就，当前完成课程数: " + completedCount);
            }
        } catch (Exception e) {
            System.err.println("成就检查失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public UserCourseProgress getUserProgress(Long userId, Long courseId) {
        return progressMapper.selectByUserAndCourse(userId, courseId);
    }

    public Map<String, Object> getUserStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        Integer completedCount = progressMapper.countCompletedCourses(userId);
        stats.put("completedCount", completedCount != null ? completedCount : 0);
        return stats;
    }

    // ========== 点赞功能 ==========
    
    @Transactional
    public boolean likeCourse(Long courseId, Long userId) {
        LambdaQueryWrapper<CourseLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseLike::getCourseId, courseId)
               .eq(CourseLike::getUserId, userId);
        CourseLike existing = courseLikeMapper.selectOne(wrapper);
        
        if (existing != null) {
            return false;
        }
        
        CourseLike courseLike = new CourseLike();
        courseLike.setCourseId(courseId);
        courseLike.setUserId(userId);
        courseLike.setCreateTime(LocalDateTime.now());
        courseLikeMapper.insert(courseLike);
        
        Course course = getById(courseId);
        if (course != null) {
            course.setLikeCount((course.getLikeCount() == null ? 0 : course.getLikeCount()) + 1);
            updateById(course);
        }
        return true;
    }

    @Transactional
    public boolean unlikeCourse(Long courseId, Long userId) {
        LambdaQueryWrapper<CourseLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseLike::getCourseId, courseId)
               .eq(CourseLike::getUserId, userId);
        int deleted = courseLikeMapper.delete(wrapper);
        
        if (deleted > 0) {
            Course course = getById(courseId);
            if (course != null) {
                course.setLikeCount(Math.max(0, (course.getLikeCount() == null ? 0 : course.getLikeCount()) - 1));
                updateById(course);
            }
            return true;
        }
        return false;
    }

    public boolean isLiked(Long courseId, Long userId) {
        if (userId == null) return false;
        LambdaQueryWrapper<CourseLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseLike::getCourseId, courseId)
               .eq(CourseLike::getUserId, userId);
        return courseLikeMapper.selectCount(wrapper) > 0;
    }
}