package com.ich.controller;

import com.ich.service.InheritorService;
import com.ich.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inheritor")
@RequiredArgsConstructor
public class InheritorController {
    
    private final InheritorService inheritorService;
    
    @GetMapping("/list")
    public Result<?> getInheritorList() {
        return Result.success(inheritorService.getAllInheritors());
    }
    
    @GetMapping("/detail/{id}")
    public Result<?> getInheritorDetail(@PathVariable Long id) {
        return Result.success(inheritorService.getInheritorDetail(id));
    }
}