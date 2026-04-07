package com.ich.controller;

import com.ich.service.CollectService;
import com.ich.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/collect")
@RequiredArgsConstructor
public class CollectController {
    
    private final CollectService collectService;
    
    @PostMapping("/add")
    public Result<?> collect(@RequestBody Map<String, Long> params) {
        Long userId = params.get("userId");
        Long heritageId = params.get("heritageId");
        boolean success = collectService.collectHeritage(userId, heritageId);
        return success ? Result.success("收藏成功") : Result.error("已收藏");
    }
    
    @PostMapping("/cancel")
    public Result<?> cancel(@RequestBody Map<String, Long> params) {
        Long userId = params.get("userId");
        Long heritageId = params.get("heritageId");
        boolean success = collectService.cancelCollect(userId, heritageId);
        return success ? Result.success("取消成功") : Result.error("未收藏");
    }
    
    @GetMapping("/my/{userId}")
    public Result<?> myCollects(@PathVariable Long userId) {
        return Result.success(collectService.getMyCollects(userId));
    }
}