// src/main/java/com/ich/entity/UserQuizRecord.java
package com.ich.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_quiz_record")
public class UserQuizRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long quizId;
    private Integer isCorrect;
    private LocalDateTime answerTime;
}