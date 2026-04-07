// src/main/java/com/ich/entity/KnowledgeItem.java
package com.ich.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("knowledge_item")
public class KnowledgeItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String category;
    private String cover;
    private String summary;
    private String content;
    private Long userId;           // 添加者用户ID
    private String authorName;     // 添加者昵称
    private Integer viewCount;
    private Integer likeCount;
    private Integer status;         // 0:待审核 1:已发布 2:已驳回
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}