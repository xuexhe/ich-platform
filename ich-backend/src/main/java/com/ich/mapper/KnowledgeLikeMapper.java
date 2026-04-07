// src/main/java/com/ich/mapper/KnowledgeLikeMapper.java
package com.ich.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ich.entity.KnowledgeLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface KnowledgeLikeMapper extends BaseMapper<KnowledgeLike> {
    
    @Select("SELECT COUNT(*) FROM knowledge_like WHERE knowledge_id = #{knowledgeId}")
    Integer countByKnowledgeId(@Param("knowledgeId") Long knowledgeId);
    
    @Select("SELECT COUNT(*) FROM knowledge_like WHERE knowledge_id = #{knowledgeId} AND user_id = #{userId}")
    Integer checkUserLike(@Param("knowledgeId") Long knowledgeId, @Param("userId") Long userId);
}