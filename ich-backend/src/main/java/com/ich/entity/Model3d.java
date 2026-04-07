// src/main/java/com/ich/entity/Model3d.java
package com.ich.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("model_3d")
public class Model3d {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String category;
    private String cover;
    private String modelUrl;
    private String description;
    private Integer viewCount;
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}