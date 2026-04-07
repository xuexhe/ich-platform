// src/main/java/com/ich/controller/AchievementController.java
package com.ich.controller;

import com.ich.service.AchievementService;
import com.ich.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/achievement")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping("/list")
    public Result<?> getAllAchievements() {
        return Result.success(achievementService.getAllAchievements());
    }

    @GetMapping("/user/{userId}")
    public Result<?> getUserAchievements(@PathVariable Long userId) {
        return Result.success(achievementService.getUserAchievementsWithStatus(userId));
    }

    @GetMapping("/stats/{userId}")
    public Result<?> getUserStats(@PathVariable Long userId) {
        return Result.success(achievementService.getUserAchievementStats(userId));
    }
}