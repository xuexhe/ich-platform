// src/main/java/com/ich/service/CollectService.java
// 在 collectHeritage 方法中添加成就检查

package com.ich.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ich.entity.Collect;
import com.ich.entity.Heritage;
import com.ich.mapper.CollectMapper;
import com.ich.mapper.HeritageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CollectService extends ServiceImpl<CollectMapper, Collect> {
    
    private final CollectMapper collectMapper;
    private final HeritageMapper heritageMapper;
    private final AchievementService achievementService;
    
    @Transactional
    public boolean collectHeritage(Long userId, Long heritageId) {
        Collect exist = collectMapper.selectByUserAndHeritage(userId, heritageId);
        if (exist != null) return false;
        
        Collect collect = new Collect();
        collect.setUserId(userId);
        collect.setHeritageId(heritageId);
        collect.setCreateTime(LocalDateTime.now());
        boolean success = save(collect);
        
        if (success) {
            Heritage heritage = heritageMapper.selectById(heritageId);
            heritage.setCollectCount((heritage.getCollectCount() == null ? 0 : heritage.getCollectCount()) + 1);
            heritageMapper.updateById(heritage);
            
            // 成就检查：收藏数量
            Long collectCount = collectMapper.selectCount(
                new LambdaQueryWrapper<Collect>().eq(Collect::getUserId, userId)
            );
            achievementService.checkAndGrantAchievements(userId, "collect_count", collectCount);
        }
        return success;
    }
    
    @Transactional
    public boolean cancelCollect(Long userId, Long heritageId) {
        Collect exist = collectMapper.selectByUserAndHeritage(userId, heritageId);
        if (exist == null) return false;
        
        boolean success = removeById(exist.getId());
        
        if (success) {
            Heritage heritage = heritageMapper.selectById(heritageId);
            heritage.setCollectCount(Math.max(0, (heritage.getCollectCount() == null ? 0 : heritage.getCollectCount()) - 1));
            heritageMapper.updateById(heritage);
        }
        return success;
    }
    
    public List<Collect> getMyCollects(Long userId) {
        return collectMapper.selectByUserId(userId);
    }
}