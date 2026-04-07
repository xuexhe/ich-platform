// src/main/java/com/ich/mapper/UserAchievementMapper.java
package com.ich.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ich.entity.UserAchievement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserAchievementMapper extends BaseMapper<UserAchievement> {
    
    @Select("SELECT achievement_id FROM user_achievement WHERE user_id = #{userId}")
    List<Long> selectAchievementIdsByUserId(@Param("userId") Long userId);
    
    @Select("SELECT COUNT(*) FROM user_achievement WHERE user_id = #{userId}")
    Integer countByUserId(@Param("userId") Long userId);
}