// src/main/java/com/ich/service/HeritageService.java
// 在 getHeritageDetail 方法中添加成就检查

package com.ich.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ich.entity.Heritage;
import com.ich.mapper.HeritageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HeritageService extends ServiceImpl<HeritageMapper, Heritage> {
    
    @Autowired
    private AchievementService achievementService;
    
    public Page<Heritage> getHeritageList(Integer pageNum, Integer pageSize, String category, String keyword) {
        Page<Heritage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Heritage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Heritage::getStatus, 1);
        
        if (category != null && !"all".equals(category)) {
            wrapper.eq(Heritage::getCategory, category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Heritage::getName, keyword).or().like(Heritage::getIntro, keyword));
        }
        wrapper.orderByDesc(Heritage::getCreateTime);
        return page(page, wrapper);
    }
    
    public Page<Heritage> getAdminList(Integer pageNum, Integer pageSize, String keyword) {
        Page<Heritage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Heritage> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Heritage::getName, keyword);
        }
        wrapper.orderByDesc(Heritage::getCreateTime);
        return page(page, wrapper);
    }
    
    public boolean saveHeritage(Heritage heritage) {
        if (heritage.getId() == null) {
            heritage.setStatus(1);
            heritage.setViewCount(0);
            heritage.setCollectCount(0);
            heritage.setCreateTime(LocalDateTime.now());
        }
        heritage.setUpdateTime(LocalDateTime.now());
        return saveOrUpdate(heritage);
    }
    
    public Heritage getHeritageDetail(Long id) {
        return getHeritageDetail(id, null);
    }
    
    public Heritage getHeritageDetail(Long id, Long userId) {
        Heritage heritage = getById(id);
        if (heritage != null && heritage.getStatus() == 1) {
            heritage.setViewCount((heritage.getViewCount() == null ? 0 : heritage.getViewCount()) + 1);
            updateById(heritage);
            
            // 成就检查：浏览数量（用户登录时才检查）
            if (userId != null && achievementService != null) {
                try {
                    Long browseCount = count(new LambdaQueryWrapper<Heritage>().eq(Heritage::getStatus, 1));
                    achievementService.checkAndGrantAchievements(userId, "browse_count", browseCount);
                } catch (Exception e) {
                    System.err.println("成就检查失败: " + e.getMessage());
                }
            }
        }
        return heritage;
    }
    
    public List<Heritage> getHotHeritage(Integer limit) {
        LambdaQueryWrapper<Heritage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Heritage::getStatus, 1)
               .orderByDesc(Heritage::getViewCount)
               .last("limit " + limit);
        return list(wrapper);
    }
    
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        long heritageCount = count(new LambdaQueryWrapper<Heritage>().eq(Heritage::getStatus, 1));
        stats.put("heritageCount", heritageCount);
        return stats;
    }
}