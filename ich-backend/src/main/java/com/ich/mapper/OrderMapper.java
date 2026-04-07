package com.ich.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ich.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}