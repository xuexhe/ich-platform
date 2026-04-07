// src/main/java/com/ich/mapper/UserCourseProgressMapper.java
package com.ich.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ich.entity.UserCourseProgress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserCourseProgressMapper extends BaseMapper<UserCourseProgress> {
    
    @Select("SELECT COUNT(*) FROM user_course_progress WHERE user_id = #{userId} AND is_completed = 1")
    Integer countCompletedCourses(@Param("userId") Long userId);
    
    @Select("SELECT * FROM user_course_progress WHERE user_id = #{userId} AND course_id = #{courseId}")
    UserCourseProgress selectByUserAndCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);
    
    @Update("UPDATE user_course_progress SET progress = #{progress}, last_watch_time = NOW() WHERE id = #{id}")
    int updateProgress(@Param("id") Long id, @Param("progress") Integer progress);
}