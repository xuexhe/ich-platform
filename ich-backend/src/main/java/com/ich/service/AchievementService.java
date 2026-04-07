// src/main/java/com/ich/service/AchievementService.java
package com.ich.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ich.entity.Achievement;
import com.ich.entity.UserAchievement;
import com.ich.mapper.AchievementMapper;
import com.ich.mapper.UserAchievementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementMapper achievementMapper;
    private final UserAchievementMapper userAchievementMapper;

    /**
     * 获取所有成就列表
     */
    public List<Achievement> getAllAchievements() {
        return achievementMapper.selectAllOrderByPoints();
    }

    /**
     * 获取用户已获得的成就ID列表
     */
    public Set<Long> getUserAchievementIds(Long userId) {
        if (userId == null) {
            return new HashSet<>();
        }
        List<Long> ids = userAchievementMapper.selectAchievementIdsByUserId(userId);
        return new HashSet<>(ids);
    }

    /**
     * 获取用户成就详情（包含是否已获得）
     */
    public List<Map<String, Object>> getUserAchievementsWithStatus(Long userId) {
        List<Achievement> allAchievements = getAllAchievements();
        Set<Long> obtainedIds = getUserAchievementIds(userId);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Achievement achievement : allAchievements) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", achievement.getId());
            item.put("name", achievement.getName());
            item.put("description", achievement.getDescription());
            item.put("icon", achievement.getIcon());
            item.put("points", achievement.getPoints());
            item.put("conditionType", achievement.getConditionType());
            item.put("conditionValue", achievement.getConditionValue());
            item.put("isObtained", obtainedIds.contains(achievement.getId()));
            result.add(item);
        }
        return result;
    }

    /**
     * 获取用户成就统计（包含总点数）
     */
    public Map<String, Object> getUserAchievementStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        if (userId == null) {
            stats.put("obtainedCount", 0);
            stats.put("totalCount", 0);
            stats.put("totalPoints", 0);
            return stats;
        }
        
        // 获取用户已获得的成就ID列表
        List<Long> achievementIds = userAchievementMapper.selectAchievementIdsByUserId(userId);
        int obtainedCount = achievementIds.size();
        
        // 计算总点数
        int totalPoints = 0;
        if (!achievementIds.isEmpty()) {
            List<Achievement> achievements = achievementMapper.selectBatchIds(achievementIds);
            totalPoints = achievements.stream().mapToInt(achievement -> {
                Integer points = achievement.getPoints();
                return points != null ? points : 0;
            }).sum();
        }
        
        // 获取总成就数量
        Long totalCountLong = achievementMapper.selectCount(null);
        int totalCount = totalCountLong != null ? totalCountLong.intValue() : 0;
        
        stats.put("obtainedCount", obtainedCount);
        stats.put("totalCount", totalCount);
        stats.put("totalPoints", totalPoints);
        
        return stats;
    }

    /**
     * 检查并授予成就（根据用户行为触发）
     * @param userId 用户ID
     * @param actionType 行为类型（browse_count, collect_count, comment_count, course_count, diy_count）
     * @param currentValue 当前数值
     * @return 新获得的成就列表
     */
    @Transactional
    public List<Achievement> checkAndGrantAchievements(Long userId, String actionType, Long currentValue) {
        System.out.println("========== AchievementService.checkAndGrantAchievements ==========");
        System.out.println("userId: " + userId);
        System.out.println("actionType: " + actionType);
        System.out.println("currentValue: " + currentValue);
        
        if (userId == null || actionType == null) {
            System.out.println("参数为空，跳过");
            return new ArrayList<>();
        }
        
        List<Achievement> allAchievements = getAllAchievements();
        Set<Long> obtainedIds = getUserAchievementIds(userId);
        List<Achievement> newAchievements = new ArrayList<>();
        
        for (Achievement achievement : allAchievements) {
            // 如果已经获得，跳过
            if (obtainedIds.contains(achievement.getId())) {
                continue;
            }
            
            // 检查条件是否匹配
            String conditionType = achievement.getConditionType();
            Integer conditionValue = achievement.getConditionValue();
            
            System.out.println("检查成就: " + achievement.getName() + 
                               ", conditionType=" + conditionType + 
                               ", conditionValue=" + conditionValue);
            
            if (conditionType == null || conditionValue == null) {
                continue;
            }
            
            // 检查条件是否满足
            boolean shouldGrant = false;
            if (conditionType.equals(actionType)) {
                if (currentValue != null && currentValue >= conditionValue.longValue()) {
                    shouldGrant = true;
                    System.out.println("条件满足！授予成就: " + achievement.getName());
                }
            }
            
            if (shouldGrant) {
                // 授予成就
                UserAchievement userAchievement = new UserAchievement();
                userAchievement.setUserId(userId);
                userAchievement.setAchievementId(achievement.getId());
                userAchievement.setObtainTime(LocalDateTime.now());
                userAchievementMapper.insert(userAchievement);
                newAchievements.add(achievement);
                System.out.println("✅ 用户 " + userId + " 获得成就: " + achievement.getName() + " (+" + achievement.getPoints() + "点)");
            }
        }
        
        if (newAchievements.isEmpty()) {
            System.out.println("未获得新成就");
        }
        
        return newAchievements;
    }

    /**
     * 手动授予成就（管理员用）
     */
    @Transactional
    public boolean grantAchievement(Long userId, Long achievementId) {
        if (userId == null || achievementId == null) {
            return false;
        }
        
        // 检查是否已获得
        Set<Long> obtainedIds = getUserAchievementIds(userId);
        if (obtainedIds.contains(achievementId)) {
            System.out.println("用户 " + userId + " 已拥有成就ID: " + achievementId);
            return false;
        }
        
        UserAchievement userAchievement = new UserAchievement();
        userAchievement.setUserId(userId);
        userAchievement.setAchievementId(achievementId);
        userAchievement.setObtainTime(LocalDateTime.now());
        userAchievementMapper.insert(userAchievement);
        
        System.out.println("✅ 手动授予用户 " + userId + " 成就ID: " + achievementId);
        return true;
    }

    /**
     * 检查用户是否已获得某个成就
     */
    public boolean hasAchievement(Long userId, Long achievementId) {
        if (userId == null) {
            return false;
        }
        Set<Long> obtainedIds = getUserAchievementIds(userId);
        return obtainedIds.contains(achievementId);
    }

    /**
     * 获取用户获得的所有成就详情
     */
    public List<Map<String, Object>> getUserObtainedAchievements(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        
        List<Long> achievementIds = userAchievementMapper.selectAchievementIdsByUserId(userId);
        if (achievementIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Achievement> achievements = achievementMapper.selectBatchIds(achievementIds);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Achievement achievement : achievements) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", achievement.getId());
            item.put("name", achievement.getName());
            item.put("description", achievement.getDescription());
            item.put("icon", achievement.getIcon());
            item.put("points", achievement.getPoints());
            result.add(item);
        }
        
        return result;
    }
}