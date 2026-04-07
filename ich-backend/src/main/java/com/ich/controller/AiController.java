// src/main/java/com/ich/controller/AiController.java
package com.ich.controller;

import com.ich.service.TongyiAiService;
import com.ich.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
    
    private final TongyiAiService aiService;
    
    /**
     * AI 对话接口（带用户ID，用于保存历史）
     */
    @PostMapping("/chat")
    public Result<?> chat(@RequestBody Map<String, String> params) {
        String question = params.get("question");
        String userIdStr = params.get("userId");
        
        if (question == null || question.trim().isEmpty()) {
            return Result.error("请输入问题");
        }
        
        Long userId = null;
        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                userId = Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                // 忽略，不保存历史
            }
        }
        
        String answer = aiService.chat(question, userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("answer", answer);
        result.put("question", question);
        result.put("model", "通义千问");
        
        return Result.success(result);
    }
    
    /**
     * 获取用户对话历史
     */
    @GetMapping("/history/{userId}")
    public Result<?> getHistory(@PathVariable Long userId, 
                                @RequestParam(defaultValue = "20") Integer limit) {
        List<Map<String, Object>> history = aiService.getConversationHistory(userId, limit);
        return Result.success(history);
    }
    
    /**
     * 清空用户对话历史
     */
    @DeleteMapping("/history/clear/{userId}")
    public Result<?> clearHistory(@PathVariable Long userId) {
        boolean success = aiService.clearConversationHistory(userId);
        return success ? Result.success("清空成功") : Result.error("清空失败");
    }
    
    /**
     * 删除单条对话记录
     */
    @DeleteMapping("/history/delete/{id}")
    public Result<?> deleteHistory(@PathVariable Long id, @RequestParam Long userId) {
        boolean success = aiService.deleteConversation(id, userId);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
    
    /**
     * 获取 AI 服务状态
     */
    @GetMapping("/status")
    public Result<?> getStatus() {
        return Result.success(aiService.getModelInfo());
    }
    
    /**
     * 获取可用模型列表
     */
    @GetMapping("/models")
    public Result<?> getAvailableModels() {
        Map<String, String> models = new HashMap<>();
        models.put("qwen-turbo", "最快响应，适合日常问答");
        models.put("qwen-plus", "平衡性能，效果更好");
        models.put("qwen-max", "最强能力，适合复杂问题");
        return Result.success(models);
    }
    
    /**
     * 非遗专属提示词列表
     */
    @GetMapping("/prompts")
    public Result<?> getPrompts() {
        List<Map<String, String>> prompts = List.of(
            Map.of("title", "介绍京剧", "content", "请介绍一下京剧的起源和主要特点"),
            Map.of("title", "剪纸怎么做", "content", "请教我如何制作简单的剪纸"),
            Map.of("title", "苏绣艺术", "content", "苏绣有哪些独特的针法和艺术特点"),
            Map.of("title", "皮影戏历史", "content", "皮影戏的历史渊源和表演形式"),
            Map.of("title", "青花瓷鉴赏", "content", "如何鉴赏青花瓷的艺术价值"),
            Map.of("title", "端午节习俗", "content", "端午节有哪些传统习俗和文化内涵")
        );
        return Result.success(prompts);
    }
}