package com.ich.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ich.entity.Product;
import com.ich.service.ProductService;
import com.ich.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    @GetMapping("/list")
    public Result<?> getProductList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "12") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        
        Page<Product> page = productService.getProductList(pageNum, pageSize, category, keyword);
        return Result.success(page);
    }
    
    @GetMapping("/detail/{id}")
    public Result<?> getProductDetail(@PathVariable Long id) {
        Product product = productService.getProductDetail(id);
        return product != null ? Result.success(product) : Result.error("商品不存在");
    }
}