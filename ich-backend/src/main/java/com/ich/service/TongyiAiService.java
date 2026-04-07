// src/main/java/com/ich/service/TongyiAiService.java
package com.ich.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ich.entity.AiConversation;
import com.ich.mapper.AiConversationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TongyiAiService {
    
    @Value("${ai.tongyi.api-key}")
    private String apiKey;
    
    @Value("${ai.tongyi.model:qwen-turbo}")
    private String model;
    
    @Value("${ai.tongyi.url}")
    private String apiUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AiConversationMapper aiConversationMapper;
    
    // 非遗知识库（API 失败时的备用）
    private static final Map<String, String> FALLBACK_KNOWLEDGE = new HashMap<>();
    
    static {
        FALLBACK_KNOWLEDGE.put("剪纸", "剪纸是中国最古老的民间艺术之一，2006年被列入国家级非物质文化遗产名录。主要流派有蔚县剪纸、佛山剪纸等。");
        FALLBACK_KNOWLEDGE.put("苏绣", "苏绣是中国四大名绣之一，起源于苏州，特点是图案秀丽、针法活泼、色彩清雅。");
        FALLBACK_KNOWLEDGE.put("皮影戏", "皮影戏始于西汉，兴于唐朝，2011年被列入人类非物质文化遗产代表作名录。");
        FALLBACK_KNOWLEDGE.put("青花瓷", "青花瓷是中国瓷器的主流品种之一，始于唐宋，成熟于元代景德镇。");
        FALLBACK_KNOWLEDGE.put("京剧", "京剧是中国五大戏曲剧种之一，2010年被列入人类非物质文化遗产代表作名录。");
        FALLBACK_KNOWLEDGE.put("端午节", "端午节是中国传统节日，2009年被列入人类非物质文化遗产代表作名录。");
        FALLBACK_KNOWLEDGE.put("非物质文化遗产", "非物质文化遗产是指各族人民世代相传并视为其文化遗产组成部分的各种传统文化表现形式。");
    }
    
    /**
     * 主对话方法（带历史记录）
     */
    public String chat(String question, Long userId) {
        // 参数校验
        if (question == null || question.trim().isEmpty()) {
            return "您好！我是非遗小助手，请问有什么可以帮您？";
        }
        
        String answer;
        
        // 1. 先检查本地知识库（快速响应）
        String localAnswer = searchLocalKnowledge(question);
        if (localAnswer != null) {
            answer = localAnswer + "\n\n（来自非遗知识库）";
            saveConversation(userId, question, answer, "本地知识库");
            return answer;
        }
        
        // 2. 调用通义千问 API
        try {
            answer = callTongyiAPI(question);
            saveConversation(userId, question, answer, model);
            return answer;
        } catch (Exception e) {
            log.error("调用通义千问失败: {}", e.getMessage());
            // 3. API 失败时使用本地知识库兜底
            answer = getFallbackAnswer(question);
            saveConversation(userId, question, answer, "本地知识库(兜底)");
            return answer;
        }
    }
    
    /**
     * 保存对话历史
     */
    private void saveConversation(Long userId, String question, String answer, String modelUsed) {
        if (userId == null || userId <= 0) {
            return;
        }
        if (question == null || answer == null) {
            return;
        }
        
        try {
            AiConversation conversation = new AiConversation();
            conversation.setUserId(userId);
            // 截取过长的内容，避免数据库字段溢出
            conversation.setQuestion(question.length() > 500 ? question.substring(0, 500) : question);
            conversation.setAnswer(answer.length() > 2000 ? answer.substring(0, 2000) : answer);
            conversation.setModel(modelUsed != null ? modelUsed : "unknown");
            conversation.setCreateTime(LocalDateTime.now());
            aiConversationMapper.insert(conversation);
        } catch (Exception e) {
            log.error("保存对话历史失败: {}", e.getMessage());
        }
    }
    
    /**
     * 获取用户对话历史
     */
    public List<Map<String, Object>> getConversationHistory(Long userId, int limit) {
        if (userId == null || userId <= 0) {
            return new ArrayList<>();
        }
        
        // 限制 limit 范围
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        
        List<AiConversation> list = aiConversationMapper.selectRecentByUserId(userId, safeLimit);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (AiConversation conv : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", conv.getId());
            item.put("question", conv.getQuestion());
            item.put("answer", conv.getAnswer());
            item.put("model", conv.getModel());
            item.put("createTime", conv.getCreateTime());
            result.add(item);
        }
        return result;
    }
    
    /**
     * 清空用户对话历史
     */
    public boolean clearConversationHistory(Long userId) {
        if (userId == null || userId <= 0) {
            return false;
        }
        
        try {
            int deleted = aiConversationMapper.delete(
                new LambdaQueryWrapper<AiConversation>().eq(AiConversation::getUserId, userId)
            );
            log.info("清空用户 {} 的对话历史，共删除 {} 条记录", userId, deleted);
            return deleted > 0;
        } catch (Exception e) {
            log.error("清空对话历史失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 删除单条对话记录
     */
    public boolean deleteConversation(Long id, Long userId) {
        if (id == null || userId == null) {
            return false;
        }
        
        try {
            AiConversation conv = aiConversationMapper.selectById(id);
            if (conv == null || !conv.getUserId().equals(userId)) {
                log.warn("删除失败：记录不存在或用户不匹配，id={}, userId={}", id, userId);
                return false;
            }
            return aiConversationMapper.deleteById(id) > 0;
        } catch (Exception e) {
            log.error("删除对话记录失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 搜索本地知识库
     */
    private String searchLocalKnowledge(String question) {
        if (question == null) {
            return null;
        }
        
        for (Map.Entry<String, String> entry : FALLBACK_KNOWLEDGE.entrySet()) {
            if (question.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
    
    /**
     * 调用通义千问 API（添加空值检查）
     */
    private String callTongyiAPI(String question) throws Exception {
        // 空值检查
        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("sk-xxx")) {
            log.warn("API Key 未配置或使用默认值，使用本地知识库");
            throw new Exception("API Key 未配置");
        }
        
        if (apiUrl == null || apiUrl.isEmpty()) {
            log.warn("API URL 未配置");
            throw new Exception("API URL 未配置");
        }
        
        if (question == null || question.isEmpty()) {
            throw new Exception("问题不能为空");
        }
        
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        
        // 构建系统提示词
        String systemPrompt = "你是一位专业的非遗文化讲解员，名叫「非遗小助手」。\n" +
                "请用亲切、易懂、富有文化气息的语言回答用户的问题。\n" +
                "回答要简洁明了，控制在150字以内。\n" +
                "如果问题与非遗文化无关，请礼貌地引导用户关注非遗话题。\n" +
                "可以适当使用emoji表情增强趣味性。";
        
        // 构建消息列表
        List<Map<String, String>> messages = new ArrayList<>();
        
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);
        
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", question);
        messages.add(userMessage);
        
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.8);
        requestBody.put("max_tokens", 500);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        log.info("调用通义千问 API，问题: {}", question);
        log.debug("API URL: {}", apiUrl);
        
        // 发送请求 - 使用 Objects.requireNonNull 确保参数非空
        ResponseEntity<String> response = restTemplate.exchange(
            Objects.requireNonNull(apiUrl, "API URL cannot be null"),
            Objects.requireNonNull(HttpMethod.POST, "HTTP method cannot be null"),
            entity,
            String.class
        );
        
        // 检查响应
        if (response.getBody() == null) {
            log.warn("API 响应内容为空");
            return "抱歉，我暂时无法回答这个问题。";
        }
        
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.warn("API 响应状态码异常: {}", response.getStatusCode());
            return "抱歉，服务暂时不可用，请稍后再试。";
        }
        
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            
            if (root.has("choices") && root.get("choices").isArray() && root.get("choices").size() > 0) {
                JsonNode choice = root.get("choices").get(0);
                if (choice.has("message") && choice.get("message").has("content")) {
                    String answer = choice.get("message").get("content").asText();
                    log.info("通义千问回答: {}", answer);
                    return answer;
                }
            }
            
            // 错误信息处理
            if (root.has("error") && root.get("error").has("message")) {
                String errorMsg = root.get("error").get("message").asText();
                log.error("API 返回错误: {}", errorMsg);
                return "抱歉，AI服务出现错误：" + errorMsg;
            }
        } catch (Exception e) {
            log.error("解析 API 响应失败: {}", e.getMessage());
            return "抱歉，响应解析失败，请稍后再试。";
        }
        
        return "抱歉，我暂时无法回答这个问题。";
    }
    
    /**
     * API 失败时的兜底回答
     */
    private String getFallbackAnswer(String question) {
        if (question == null) {
            return "您好！我是非遗小助手，请问有什么可以帮您？";
        }
        
        if (question.contains("剪纸")) {
            return "剪纸是中国最古老的民间艺术之一！用剪刀或刻刀在纸上剪刻花纹，2006年入选国家级非遗名录。✂️";
        }
        if (question.contains("苏绣")) {
            return "苏绣是苏州的传统技艺，以精细雅洁著称，被誉为「东方明珠」！🧵";
        }
        if (question.contains("皮影戏")) {
            return "皮影戏始于西汉，艺人在幕后操纵影人讲故事，2011年入选人类非遗！🎭";
        }
        if (question.contains("青花瓷")) {
            return "青花瓷是中国瓷器代表，白底蓝花，元代景德镇最著名，周杰伦还唱过《青花瓷》呢！🏺";
        }
        if (question.contains("京剧")) {
            return "京剧是中国的国粹，2010年入选人类非遗。红脸忠勇、白脸奸诈，脸谱超有讲究！🎭";
        }
        if (question.contains("端午")) {
            return "端午节是纪念屈原的传统节日，2009年入选人类非遗。赛龙舟、吃粽子、挂艾草！🐲";
        }
        if (question.contains("非遗") || question.contains("非物质文化遗产")) {
            return "非物质文化遗产是世代相传的文化瑰宝，包括传统技艺、表演艺术、民俗节庆等。想了解哪个具体项目？我给您详细介绍！✨";
        }
        
        return "您好！我是非遗小助手，可以为您介绍剪纸、苏绣、皮影戏、青花瓷、京剧等非遗文化。您想了解哪个？🎨";
    }
    
    /**
     * 检查 API 是否可用
     */
    public boolean isApiAvailable() {
        // 先检查配置
        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("sk-xxx")) {
            log.warn("API Key 未配置");
            return false;
        }
        if (apiUrl == null || apiUrl.isEmpty()) {
            log.warn("API URL 未配置");
            return false;
        }
        
        try {
            callTongyiAPI("你好");
            return true;
        } catch (Exception e) {
            log.warn("API 不可用: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取模型信息
     */
    public Map<String, Object> getModelInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("model", model != null ? model : "qwen-turbo");
        info.put("provider", "阿里云通义千问");
        info.put("status", isApiAvailable() ? "connected" : "disconnected");
        info.put("apiKeyConfigured", apiKey != null && !apiKey.isEmpty() && !apiKey.startsWith("sk-xxx"));
        info.put("apiUrlConfigured", apiUrl != null && !apiUrl.isEmpty());
        return info;
    }
}