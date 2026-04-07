// src/main/java/com/ich/mapper/AiConversationMapper.java
package com.ich.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ich.entity.AiConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AiConversationMapper extends BaseMapper<AiConversation> {
    
    @Select("SELECT * FROM ai_conversation WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<AiConversation> selectRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);
}