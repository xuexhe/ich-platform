// src/main/java/com/ich/mapper/CategoryMapper.java
package com.ich.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ich.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    
    @Select("SELECT * FROM category WHERE status = 1 ORDER BY sort_order ASC")
    List<Category> selectAllEnabled();
}