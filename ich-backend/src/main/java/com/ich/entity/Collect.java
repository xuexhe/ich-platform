package com.ich.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("collect")
public class Collect {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long heritageId;
    private LocalDateTime createTime;
}