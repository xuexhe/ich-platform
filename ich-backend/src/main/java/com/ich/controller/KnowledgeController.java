// src/main/java/com/ich/controller/KnowledgeController.java
package com.ich.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ich.entity.KnowledgeItem;
import com.ich.service.KnowledgeService;
import com.ich.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    // 获取知识库列表
    @GetMapping("/list")
    public Result<?> getKnowledgeList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "12") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        Page<KnowledgeItem> page = knowledgeService.getKnowledgeList(pageNum, pageSize, category, keyword);
        return Result.success(page);
    }

    // 获取详情
    @GetMapping("/detail/{id}")
    public Result<?> getKnowledgeDetail(@PathVariable Long id) {
        KnowledgeItem item = knowledgeService.getKnowledgeDetail(id);
        return item != null ? Result.success(item) : Result.error("内容不存在");
    }

    // 获取分类列表
    @GetMapping("/categories")
    public Result<?> getCategories() {
        return Result.success(knowledgeService.getAllCategories());
    }

    // ========== 点赞功能 ==========
    
    @PostMapping("/like")
    public Result<?> like(@RequestBody Map<String, Long> params) {
        Long knowledgeId = params.get("knowledgeId");
        Long userId = params.get("userId");
        
        if (userId == null) {
            return Result.error("请先登录");
        }
        
        boolean success = knowledgeService.likeKnowledge(knowledgeId, userId);
        return success ? Result.success("点赞成功") : Result.error("已经点过赞了");
    }
    
    @PostMapping("/unlike")
    public Result<?> unlike(@RequestBody Map<String, Long> params) {
        Long knowledgeId = params.get("knowledgeId");
        Long userId = params.get("userId");
        
        if (userId == null) {
            return Result.error("请先登录");
        }
        
        boolean success = knowledgeService.unlikeKnowledge(knowledgeId, userId);
        return success ? Result.success("取消点赞") : Result.error("操作失败");
    }
    
    @GetMapping("/liked/{knowledgeId}")
    public Result<?> isLiked(@PathVariable Long knowledgeId, @RequestParam Long userId) {
        boolean isLiked = knowledgeService.isLiked(knowledgeId, userId);
        return Result.success(isLiked);
    }
    
    @GetMapping("/likeCount/{knowledgeId}")
    public Result<?> getLikeCount(@PathVariable Long knowledgeId) {
        return Result.success(knowledgeService.getLikeCount(knowledgeId));
    }

    // ========== 用户添加知识 ==========
    
    @PostMapping("/add")
    public Result<?> addKnowledge(@RequestBody KnowledgeItem item, @RequestParam Long userId) {
        if (item.getTitle() == null || item.getTitle().isEmpty()) {
            return Result.error("请输入标题");
        }
        if (item.getContent() == null || item.getContent().isEmpty()) {
            return Result.error("请输入内容");
        }
        
        boolean success = knowledgeService.addKnowledge(item, userId);
        return success ? Result.success("提交成功，等待审核") : Result.error("提交失败");
    }
    
    @GetMapping("/user/{userId}")
    public Result<?> getUserKnowledge(@PathVariable Long userId,
                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(knowledgeService.getUserKnowledge(userId, pageNum, pageSize));
    }
    
    @PutMapping("/update/{id}")
    public Result<?> updateKnowledge(@PathVariable Long id, 
                                      @RequestBody KnowledgeItem item,
                                      @RequestParam Long userId) {
        boolean success = knowledgeService.updateUserKnowledge(id, userId, item);
        return success ? Result.success("更新成功") : Result.error("更新失败，只能修改待审核的内容");
    }
    
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteKnowledge(@PathVariable Long id, @RequestParam Long userId) {
        boolean success = knowledgeService.deleteUserKnowledge(id, userId);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}