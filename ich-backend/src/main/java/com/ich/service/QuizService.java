// src/main/java/com/ich/service/QuizService.java
package com.ich.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ich.entity.DailyQuiz;
import com.ich.entity.UserQuizRecord;
import com.ich.mapper.QuizMapper;
import com.ich.mapper.UserQuizRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuizService extends ServiceImpl<QuizMapper, DailyQuiz> {

    private final QuizMapper quizMapper;
    private final UserQuizRecordMapper userQuizRecordMapper;

    /**
     * 获取今日题目
     */
    public DailyQuiz getTodayQuiz() {
        DailyQuiz quiz = quizMapper.selectByDate(LocalDate.now());
        if (quiz == null) {
            // 如果没有今天的题目，返回最近的一道题
            quiz = getOne(new LambdaQueryWrapper<DailyQuiz>()
                    .orderByDesc(DailyQuiz::getQuizDate)
                    .last("limit 1"));
        }
        return quiz;
    }

    /**
     * 获取指定日期的题目
     */
    public DailyQuiz getQuizByDate(LocalDate date) {
        return quizMapper.selectByDate(date);
    }

    /**
     * 提交答案
     */
    @Transactional
    public Map<String, Object> submitAnswer(Long userId, Long quizId, String answer) {
        Map<String, Object> result = new HashMap<>();
        
        Integer todayCount = userQuizRecordMapper.countTodayAnswers(userId);
        if (todayCount > 0) {
            result.put("alreadyAnswered", true);
            result.put("message", "今天已经答过题了");
            return result;
        }
        
        DailyQuiz quiz = getById(quizId);
        if (quiz == null) {
            result.put("success", false);
            result.put("message", "题目不存在");
            return result;
        }
        
        boolean isCorrect = quiz.getCorrectAnswer().equalsIgnoreCase(answer);
        
        UserQuizRecord record = new UserQuizRecord();
        record.setUserId(userId);
        record.setQuizId(quizId);
        record.setIsCorrect(isCorrect ? 1 : 0);
        record.setAnswerTime(LocalDateTime.now());
        userQuizRecordMapper.insert(record);
        
        result.put("success", true);
        result.put("isCorrect", isCorrect);
        result.put("correctAnswer", quiz.getCorrectAnswer());
        result.put("explanation", quiz.getExplanation());
        result.put("message", isCorrect ? "回答正确！" : "回答错误");
        
        return result;
    }

    /**
     * 获取用户答题统计
     */
    public Map<String, Object> getUserStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        Integer correctCount = userQuizRecordMapper.countCorrectAnswers(userId);
        Integer todayAnswered = userQuizRecordMapper.countTodayAnswers(userId);
        
        stats.put("correctCount", correctCount != null ? correctCount : 0);
        stats.put("todayAnswered", todayAnswered != null && todayAnswered > 0);
        
        return stats;
    }

    /**
     * 管理员分页查询
     */
    public Page<DailyQuiz> getAdminList(Integer pageNum, Integer pageSize, String keyword) {
        Page<DailyQuiz> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DailyQuiz> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(DailyQuiz::getQuestion, keyword);
        }
        wrapper.orderByDesc(DailyQuiz::getQuizDate);
        return page(page, wrapper);
    }
    
    /**
     * 保存题目（新增或编辑）
     * 如果同一日期已存在题目，则更新；否则新增
     */
    public boolean saveQuiz(DailyQuiz quiz) {
        if (quiz.getId() == null) {
            // 检查该日期是否已有题目
            DailyQuiz existing = getOne(new LambdaQueryWrapper<DailyQuiz>()
                    .eq(DailyQuiz::getQuizDate, quiz.getQuizDate()));
            if (existing != null) {
                // 已存在，更新该题目
                quiz.setId(existing.getId());
                quiz.setCreateTime(existing.getCreateTime());
                return updateById(quiz);
            }
            // 不存在，新增
            quiz.setCreateTime(LocalDateTime.now());
            return save(quiz);
        } else {
            // 有ID，直接更新
            return updateById(quiz);
        }
    }
}