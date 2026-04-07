package com.ich.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ich.entity.Inheritor;
import com.ich.mapper.InheritorMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InheritorService extends ServiceImpl<InheritorMapper, Inheritor> {
    
    // 前台获取所有传承人
    public List<Inheritor> getAllInheritors() {
        return list();
    }
    
    // 前台获取详情
    public Inheritor getInheritorDetail(Long id) {
        return getById(id);
    }
    
    // 管理员分页查询
    public Page<Inheritor> getAdminList(Integer pageNum, Integer pageSize, String keyword) {
        Page<Inheritor> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Inheritor> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Inheritor::getName, keyword);
        }
        wrapper.orderByDesc(Inheritor::getCreateTime);
        return page(page, wrapper);
    }
    
    // 保存传承人（新增或编辑）
    public boolean saveInheritor(Inheritor inheritor) {
        if (inheritor.getId() == null) {
            inheritor.setCreateTime(LocalDateTime.now());
        }
        return saveOrUpdate(inheritor);
    }
}