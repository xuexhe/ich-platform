// src/main/java/com/ich/service/Model3dService.java
package com.ich.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ich.entity.Model3d;
import com.ich.mapper.Model3dMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class Model3dService extends ServiceImpl<Model3dMapper, Model3d> {

    private final Model3dMapper model3dMapper;

    /**
     * 获取所有启用的模型（前台使用）
     */
    public List<Model3d> getAllEnabledModels() {
        return model3dMapper.selectAllEnabled();
    }

    /**
     * 获取模型详情
     */
    public Model3d getModelDetail(Long id) {
        Model3d model = getById(id);
        if (model != null && model.getStatus() == 1) {
            model.setViewCount((model.getViewCount() == null ? 0 : model.getViewCount()) + 1);
            updateById(model);
        }
        return model;
    }

    /**
     * 管理员分页查询
     */
    public Page<Model3d> getAdminList(Integer pageNum, Integer pageSize, String keyword) {
        Page<Model3d> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Model3d> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Model3d::getName, keyword);
        }
        wrapper.orderByAsc(Model3d::getSortOrder);
        return page(page, wrapper);
    }

    /**
     * 保存模型
     */
    public boolean saveModel(Model3d model) {
        if (model.getId() == null) {
            model.setViewCount(0);
            model.setStatus(1);
            model.setCreateTime(LocalDateTime.now());
        }
        model.setUpdateTime(LocalDateTime.now());
        return saveOrUpdate(model);
    }
}