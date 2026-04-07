// src/main/java/com/ich/service/KnowledgeService.java
package com.ich.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ich.entity.KnowledgeItem;
import com.ich.entity.KnowledgeLike;
import com.ich.entity.User;
import com.ich.mapper.KnowledgeItemMapper;
import com.ich.mapper.KnowledgeLikeMapper;
import com.ich.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeService extends ServiceImpl<KnowledgeItemMapper, KnowledgeItem> {

    private final KnowledgeItemMapper knowledgeItemMapper;
    private final KnowledgeLikeMapper knowledgeLikeMapper;
    private final UserMapper userMapper;

    // ========== 前台查询 ==========
    
    public Page<KnowledgeItem> getKnowledgeList(Integer pageNum, Integer pageSize, String category, String keyword) {
        Page<KnowledgeItem> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<KnowledgeItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeItem::getStatus, 1);
        
        if (category != null && !"all".equals(category)) {
            wrapper.eq(KnowledgeItem::getCategory, category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(KnowledgeItem::getTitle, keyword).or().like(KnowledgeItem::getSummary, keyword);
        }
        wrapper.orderByDesc(KnowledgeItem::getViewCount);
        return page(page, wrapper);
    }

    public KnowledgeItem getKnowledgeDetail(Long id) {
        KnowledgeItem item = getById(id);
        if (item != null && item.getStatus() == 1) {
            item.setViewCount((item.getViewCount() == null ? 0 : item.getViewCount()) + 1);
            updateById(item);
        }
        return item;
    }

    public List<String> getAllCategories() {
        List<KnowledgeItem> items = list(new LambdaQueryWrapper<KnowledgeItem>().eq(KnowledgeItem::getStatus, 1));
        return items.stream().map(KnowledgeItem::getCategory).distinct().toList();
    }

    // ========== 管理员查询 ==========
    
    public Page<KnowledgeItem> getAdminList(Integer pageNum, Integer pageSize, String keyword, Integer status) {
        Page<KnowledgeItem> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<KnowledgeItem> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(KnowledgeItem::getTitle, keyword).or().like(KnowledgeItem::getSummary, keyword);
        }
        if (status != null) {
            wrapper.eq(KnowledgeItem::getStatus, status);
        }
        wrapper.orderByDesc(KnowledgeItem::getCreateTime);
        return page(page, wrapper);
    }

    public Page<KnowledgeItem> getPendingList(Integer pageNum, Integer pageSize) {
        Page<KnowledgeItem> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<KnowledgeItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeItem::getStatus, 0)
               .orderByDesc(KnowledgeItem::getCreateTime);
        return page(page, wrapper);
    }

    // ========== 点赞功能 ==========
    
    @Transactional
    public boolean likeKnowledge(Long knowledgeId, Long userId) {
        Integer exist = knowledgeLikeMapper.checkUserLike(knowledgeId, userId);
        if (exist > 0) {
            return false;
        }
        
        KnowledgeLike like = new KnowledgeLike();
        like.setKnowledgeId(knowledgeId);
        like.setUserId(userId);
        like.setCreateTime(LocalDateTime.now());
        knowledgeLikeMapper.insert(like);
        
        KnowledgeItem item = getById(knowledgeId);
        if (item != null) {
            item.setLikeCount((item.getLikeCount() == null ? 0 : item.getLikeCount()) + 1);
            updateById(item);
        }
        return true;
    }
    
    @Transactional
    public boolean unlikeKnowledge(Long knowledgeId, Long userId) {
        LambdaQueryWrapper<KnowledgeLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeLike::getKnowledgeId, knowledgeId)
               .eq(KnowledgeLike::getUserId, userId);
        int deleted = knowledgeLikeMapper.delete(wrapper);
        
        if (deleted > 0) {
            KnowledgeItem item = getById(knowledgeId);
            if (item != null) {
                item.setLikeCount(Math.max(0, (item.getLikeCount() == null ? 0 : item.getLikeCount()) - 1));
                updateById(item);
            }
            return true;
        }
        return false;
    }
    
    public boolean isLiked(Long knowledgeId, Long userId) {
        if (userId == null) return false;
        Integer count = knowledgeLikeMapper.checkUserLike(knowledgeId, userId);
        return count > 0;
    }
    
    public Integer getLikeCount(Long knowledgeId) {
        return knowledgeLikeMapper.countByKnowledgeId(knowledgeId);
    }

    // ========== 用户添加知识 ==========
    
    @Transactional
    public boolean addKnowledge(KnowledgeItem item, Long userId) {
        User user = userMapper.selectById(userId);
        
        item.setUserId(userId);
        item.setAuthorName(user != null ? (user.getNickname() != null ? user.getNickname() : user.getUsername()) : "用户");
        item.setViewCount(0);
        item.setLikeCount(0);
        item.setStatus(0);
        item.setCreateTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        return save(item);
    }
    
    public Page<KnowledgeItem> getUserKnowledge(Long userId, Integer pageNum, Integer pageSize) {
        Page<KnowledgeItem> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<KnowledgeItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeItem::getUserId, userId)
               .orderByDesc(KnowledgeItem::getCreateTime);
        return page(page, wrapper);
    }
    
    @Transactional
    public boolean updateUserKnowledge(Long id, Long userId, KnowledgeItem item) {
        KnowledgeItem existing = getById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return false;
        }
        // 移除状态限制，允许编辑所有状态的内容
        // 编辑后，如果是已发布的内容，需要重新审核
        if (existing.getStatus() == 1) {
            // 已发布的内容编辑后，状态改为待审核
            existing.setStatus(0);
        }
        existing.setTitle(item.getTitle());
        existing.setCategory(item.getCategory());
        existing.setCover(item.getCover());
        existing.setSummary(item.getSummary());
        existing.setContent(item.getContent());
        existing.setUpdateTime(LocalDateTime.now());
        return updateById(existing);
    }
    
    @Transactional
    public boolean deleteUserKnowledge(Long id, Long userId) {
        KnowledgeItem existing = getById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return false;
        }
        if (existing.getStatus() != 0 && existing.getStatus() != 2) {
            return false;
        }
        return removeById(id);
    }

    // ========== 管理员审核 ==========
    
    @Transactional
    public boolean auditKnowledge(Long id, Integer status, String reason) {
        KnowledgeItem item = getById(id);
        if (item == null) return false;
        item.setStatus(status);
        item.setUpdateTime(LocalDateTime.now());
        return updateById(item);
    }
}