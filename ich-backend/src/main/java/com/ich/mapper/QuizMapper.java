// src/main/java/com/ich/mapper/QuizMapper.java
package com.ich.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ich.entity.DailyQuiz;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

@Mapper
public interface QuizMapper extends BaseMapper<DailyQuiz> {
    
    @Select("SELECT * FROM daily_quiz WHERE quiz_date = #{date}")
    DailyQuiz selectByDate(@Param("date") LocalDate date);
}