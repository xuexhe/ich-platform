// src/main/java/com/ich/mapper/AchievementMapper.java
package com.ich.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ich.entity.Achievement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AchievementMapper extends BaseMapper<Achievement> {
    
    @Select("SELECT * FROM achievement ORDER BY points ASC")
    List<Achievement> selectAllOrderByPoints();
}