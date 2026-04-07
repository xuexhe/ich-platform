package com.ich.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("comment_reply")
public class CommentReply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long commentId;
    private Long userId;
    private String content;
    private Long replyToUserId;
    private String replyToNickname;
    private Integer likeCount;
    private Integer status;
    private LocalDateTime createTime;
}