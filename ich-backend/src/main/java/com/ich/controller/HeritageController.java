// src/main/java/com/ich/controller/HeritageController.java
package com.ich.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ich.entity.Heritage;
import com.ich.service.HeritageService;
import com.ich.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/heritage")
@RequiredArgsConstructor
public class HeritageController {
    
    private final HeritageService heritageService;
    
    @GetMapping("/list")
    public Result<?> getHeritageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "12") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        
        Page<Heritage> page = heritageService.getHeritageList(pageNum, pageSize, category, keyword);
        return Result.success(page);
    }
    
    @GetMapping("/detail/{id}")
    public Result<?> getHeritageDetail(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        Heritage heritage = heritageService.getHeritageDetail(id, userId);
        return heritage != null ? Result.success(heritage) : Result.error("非遗项目不存在");
    }
    
    @GetMapping("/categories")
    public Result<?> getCategories() {
        List<String> categories = Arrays.asList("传统技艺", "传统戏剧", "传统美术", "民俗");
        return Result.success(categories);
    }
    
    @GetMapping("/hot")
    public Result<?> getHotHeritage(@RequestParam(defaultValue = "6") Integer limit) {
        List<Heritage> list = heritageService.getHotHeritage(limit);
        return Result.success(list);
    }
}