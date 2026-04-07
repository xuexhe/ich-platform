// src/main/java/com/ich/service/CategoryService.java
package com.ich.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ich.entity.Category;
import com.ich.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryService extends ServiceImpl<CategoryMapper, Category> {
    
    /**
     * 获取所有启用的分类
     */
    public List<Category> getAllEnabledCategories() {
        return baseMapper.selectAllEnabled();
    }
    
    /**
     * 获取分类名称列表（用于前端）
     */
    public List<String> getCategoryNames() {
        return baseMapper.selectAllEnabled().stream()
            .map(Category::getName)
            .toList();
    }
    
    /**
     * 保存或更新分类
     */
    public boolean saveCategory(Category category) {
        if (category.getId() == null) {
            category.setStatus(1);
            category.setCreateTime(LocalDateTime.now());
        }
        category.setUpdateTime(LocalDateTime.now());
        return saveOrUpdate(category);
    }
    
    /**
     * 删除分类（软删除）
     */
    public boolean deleteCategory(Long id) {
        Category category = getById(id);
        if (category == null) return false;
        category.setStatus(0);
        category.setUpdateTime(LocalDateTime.now());
        return updateById(category);
    }
    
    /**
     * 管理员分页查询
     */
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Category> getAdminList(
            Integer pageNum, Integer pageSize, String keyword) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Category> page = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Category::getName, keyword);
        }
        wrapper.orderByAsc(Category::getSortOrder);
        return page(page, wrapper);
    }
}