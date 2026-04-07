// src/main/java/com/ich/mapper/UserQuizRecordMapper.java
package com.ich.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ich.entity.UserQuizRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserQuizRecordMapper extends BaseMapper<UserQuizRecord> {
    
    @Select("SELECT COUNT(*) FROM user_quiz_record WHERE user_id = #{userId} AND is_correct = 1")
    Integer countCorrectAnswers(@Param("userId") Long userId);
    
    @Select("SELECT COUNT(*) FROM user_quiz_record WHERE user_id = #{userId} AND DATE(answer_time) = CURDATE()")
    Integer countTodayAnswers(@Param("userId") Long userId);
}