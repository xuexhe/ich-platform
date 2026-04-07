package com.ich.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ich.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}