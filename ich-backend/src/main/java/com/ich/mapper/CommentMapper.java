package com.ich.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ich.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    
    @Select("SELECT * FROM comment WHERE heritage_id = #{heritageId} AND status = 1 ORDER BY create_time DESC")
    List<Comment> selectByHeritageId(@Param("heritageId") Long heritageId);
}