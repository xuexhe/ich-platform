// src/main/java/com/ich/entity/UserCourseProgress.java
package com.ich.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_course_progress")
public class UserCourseProgress {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long courseId;
    private Integer progress;
    private Integer isCompleted;
    private LocalDateTime lastWatchTime;
    private LocalDateTime completeTime;
}