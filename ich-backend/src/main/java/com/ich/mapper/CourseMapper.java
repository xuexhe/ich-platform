// src/main/java/com/ich/mapper/CourseMapper.java
package com.ich.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ich.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
    
    @Select("SELECT * FROM course WHERE status = 1 ORDER BY view_count DESC LIMIT #{limit}")
    List<Course> selectHotCourses(@Param("limit") int limit);
}