package com.ich.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ich.entity.Collect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CollectMapper extends BaseMapper<Collect> {
    
    @Select("SELECT * FROM collect WHERE user_id = #{userId}")
    List<Collect> selectByUserId(@Param("userId") Long userId);
    
    @Select("SELECT * FROM collect WHERE user_id = #{userId} AND heritage_id = #{heritageId}")
    Collect selectByUserAndHeritage(@Param("userId") Long userId, @Param("heritageId") Long heritageId);
}