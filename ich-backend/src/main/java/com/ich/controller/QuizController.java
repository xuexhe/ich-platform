// src/main/java/com/ich/controller/QuizController.java
package com.ich.controller;

import com.ich.dto.QuizSubmitDTO;
import com.ich.entity.DailyQuiz;
import com.ich.service.QuizService;
import com.ich.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    /**
     * 获取今日题目
     */
    @GetMapping("/today")
    public Result<?> getTodayQuiz(@RequestParam(required = false) Long userId) {
        DailyQuiz quiz = quizService.getTodayQuiz();
        if (quiz == null) {
            return Result.error("暂无题目");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("quiz", quiz);
        
        // 如果提供了userId，返回用户今日是否已答题
        if (userId != null) {
            Map<String, Object> stats = quizService.getUserStats(userId);
            result.put("todayAnswered", stats.get("todayAnswered"));
        }
        
        return Result.success(result);
    }

    /**
     * 提交答案
     */
    @PostMapping("/submit")
    public Result<?> submitAnswer(@RequestBody QuizSubmitDTO submitDTO) {
        if (submitDTO.getUserId() == null) {
            return Result.error("请先登录");
        }
        
        Map<String, Object> result = quizService.submitAnswer(
            submitDTO.getUserId(),
            submitDTO.getQuizId(),
            submitDTO.getAnswer()
        );
        
        if (result.containsKey("alreadyAnswered") && (Boolean) result.get("alreadyAnswered")) {
            return Result.error("今天已经答过题了");
        }
        
        return Result.success(result);
    }

    /**
     * 获取用户答题统计
     */
    @GetMapping("/stats/{userId}")
    public Result<?> getUserStats(@PathVariable Long userId) {
        return Result.success(quizService.getUserStats(userId));
    }
}