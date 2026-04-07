// src/main/java/com/ich/mapper/Model3dMapper.java
package com.ich.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ich.entity.Model3d;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface Model3dMapper extends BaseMapper<Model3d> {
    
    @Select("SELECT * FROM model_3d WHERE status = 1 ORDER BY sort_order ASC")
    List<Model3d> selectAllEnabled();
}