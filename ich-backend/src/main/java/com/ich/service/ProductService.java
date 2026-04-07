// src/main/java/com/ich/service/ProductService.java
package com.ich.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ich.entity.Product;
import com.ich.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ProductService extends ServiceImpl<ProductMapper, Product> {
    
    // 前台分页查询
    public Page<Product> getProductList(Integer pageNum, Integer pageSize, String category, String keyword) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        
        if (category != null && !"all".equals(category)) {
            wrapper.eq(Product::getCategory, category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Product::getName, keyword).or().like(Product::getIntro, keyword);
        }
        wrapper.orderByDesc(Product::getSales);
        return page(page, wrapper);
    }
    
    // 前台获取详情
    public Product getProductDetail(Long id) {
        return getById(id);
    }
    
    // 管理员分页查询
    public Page<Product> getAdminList(Integer pageNum, Integer pageSize, String keyword) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Product::getName, keyword);
        }
        wrapper.orderByDesc(Product::getCreateTime);
        return page(page, wrapper);
    }
    
    // 保存商品（新增或编辑）
    public boolean saveProduct(Product product) {
        if (product.getId() == null) {
            product.setStatus(1);
            product.setSales(0);
            product.setCreateTime(LocalDateTime.now());
        }
        product.setUpdateTime(LocalDateTime.now());
        return saveOrUpdate(product);
    }
}