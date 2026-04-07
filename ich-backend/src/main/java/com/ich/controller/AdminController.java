// src/main/java/com/ich/controller/AdminController.java
package com.ich.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ich.entity.*;
import com.ich.service.*;
import com.ich.utils.JwtUtils;
import com.ich.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final HeritageService heritageService;
    private final InheritorService inheritorService;
    private final ProductService productService;
    private final CommentService commentService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final CourseService courseService;
    private final Model3dService model3dService;
    private final KnowledgeService knowledgeService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final QuizService quizService;

    // ========== 管理员登录 ==========
    @PostMapping("/login")
    public Result<?> adminLogin(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        
        if (username == null || password == null) {
            return Result.error("用户名和密码不能为空");
        }
        
        User admin = userService.getOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getRole, "admin")
        );
        
        if (admin == null) {
            return Result.error("管理员账号不存在");
        }
        
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            return Result.error("密码错误");
        }
        
        if (admin.getStatus() != 1) {
            return Result.error("账号已被禁用");
        }
        
        String token = jwtUtils.generateToken(admin.getId());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("adminInfo", Map.of(
            "id", admin.getId(),
            "username", admin.getUsername(),
            "nickname", admin.getNickname(),
            "role", admin.getRole()
        ));
        
        return Result.success(result);
    }
    
    @GetMapping("/check")
    public Result<?> checkLogin(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error(401, "未登录");
        }
        String token = authHeader.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(401, "token已过期");
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        User admin = userService.getById(userId);
        if (admin == null || !"admin".equals(admin.getRole())) {
            return Result.error(401, "无权限");
        }
        return Result.success(Map.of("isLogin", true));
    }

    // ========== 非遗管理 ==========
    @GetMapping("/heritage/list")
    public Result<?> getHeritageList(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                      @RequestParam(required = false) String keyword) {
        Page<Heritage> page = heritageService.getAdminList(pageNum, pageSize, keyword);
        return Result.success(page);
    }

    @PostMapping("/heritage/save")
    public Result<?> saveHeritage(@RequestBody Heritage heritage) {
        boolean success = heritageService.saveHeritage(heritage);
        return success ? Result.success("保存成功") : Result.error("保存失败");
    }

    @DeleteMapping("/heritage/delete/{id}")
    public Result<?> deleteHeritage(@PathVariable Long id) {
        boolean success = heritageService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @GetMapping("/heritage/detail/{id}")
    public Result<?> getHeritageDetail(@PathVariable Long id) {
        return Result.success(heritageService.getById(id));
    }

    // ========== 传承人管理 ==========
    @GetMapping("/inheritor/list")
    public Result<?> getInheritorList(@RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @RequestParam(required = false) String keyword) {
        Page<Inheritor> page = inheritorService.getAdminList(pageNum, pageSize, keyword);
        return Result.success(page);
    }

    @PostMapping("/inheritor/save")
    public Result<?> saveInheritor(@RequestBody Inheritor inheritor) {
        boolean success = inheritorService.saveInheritor(inheritor);
        return success ? Result.success("保存成功") : Result.error("保存失败");
    }

    @DeleteMapping("/inheritor/delete/{id}")
    public Result<?> deleteInheritor(@PathVariable Long id) {
        boolean success = inheritorService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    // ========== 商品管理 ==========
    @GetMapping("/product/list")
    public Result<?> getProductList(@RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                     @RequestParam(required = false) String keyword) {
        Page<Product> page = productService.getAdminList(pageNum, pageSize, keyword);
        return Result.success(page);
    }

    @PostMapping("/product/save")
    public Result<?> saveProduct(@RequestBody Product product) {
        boolean success = productService.saveProduct(product);
        return success ? Result.success("保存成功") : Result.error("保存失败");
    }

    @DeleteMapping("/product/delete/{id}")
    public Result<?> deleteProduct(@PathVariable Long id) {
        boolean success = productService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    // ========== 评论审核 ==========
    @GetMapping("/comment/list")
    public Result<?> getCommentList(@RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                     @RequestParam(required = false) Integer status) {
        Page<Comment> page = commentService.getAdminList(pageNum, pageSize, status);
        return Result.success(page);
    }

    @PostMapping("/comment/audit/{id}")
    public Result<?> auditComment(@PathVariable Long id, @RequestParam Integer status) {
        boolean success = commentService.auditComment(id, status);
        return success ? Result.success("审核成功") : Result.error("审核失败");
    }

    @DeleteMapping("/comment/delete/{id}")
    public Result<?> deleteComment(@PathVariable Long id) {
        boolean success = commentService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    // ========== 用户管理 ==========
    @GetMapping("/user/list")
    public Result<?> getUserList(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  @RequestParam(required = false) String keyword) {
        Page<User> page = userService.getAdminList(pageNum, pageSize, keyword);
        return Result.success(page);
    }

    @PostMapping("/user/status/{id}")
    public Result<?> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean success = userService.updateStatus(id, status);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    // ========== 分类管理 ==========
    @GetMapping("/category/list")
    public Result<?> getCategoryList(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                      @RequestParam(required = false) String keyword) {
        return Result.success(categoryService.getAdminList(pageNum, pageSize, keyword));
    }

    @GetMapping("/category/all")
    public Result<?> getAllCategories() {
        return Result.success(categoryService.getAllEnabledCategories());
    }

    @PostMapping("/category/save")
    public Result<?> saveCategory(@RequestBody Category category) {
        boolean success = categoryService.saveCategory(category);
        return success ? Result.success("保存成功") : Result.error("保存失败");
    }

    @DeleteMapping("/category/delete/{id}")
    public Result<?> deleteCategory(@PathVariable Long id) {
        boolean success = categoryService.deleteCategory(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    // ========== 课程管理 ==========
    @GetMapping("/course/list")
    public Result<?> getCourseList(@RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                    @RequestParam(required = false) String keyword) {
        Page<Course> page = courseService.getAdminList(pageNum, pageSize, keyword);
        return Result.success(page);
    }

    @PostMapping("/course/save")
    public Result<?> saveCourse(@RequestBody Course course) {
        boolean success = courseService.saveCourse(course);
        return success ? Result.success("保存成功") : Result.error("保存失败");
    }

    @DeleteMapping("/course/delete/{id}")
    public Result<?> deleteCourse(@PathVariable Long id) {
        boolean success = courseService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    // ========== 3D模型管理 ==========
    @GetMapping("/model/list")
    public Result<?> getModelList(@RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   @RequestParam(required = false) String keyword) {
        Page<Model3d> page = model3dService.getAdminList(pageNum, pageSize, keyword);
        return Result.success(page);
    }

    @PostMapping("/model/save")
    public Result<?> saveModel(@RequestBody Model3d model) {
        boolean success = model3dService.saveModel(model);
        return success ? Result.success("保存成功") : Result.error("保存失败");
    }

    @DeleteMapping("/model/delete/{id}")
    public Result<?> deleteModel(@PathVariable Long id) {
        boolean success = model3dService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @GetMapping("/model/detail/{id}")
    public Result<?> getModelDetail(@PathVariable Long id) {
        return Result.success(model3dService.getById(id));
    }

    // ========== 知识库审核 ==========
    @GetMapping("/knowledge/pending")
    public Result<?> getPendingKnowledge(@RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) Integer status) {
        Page<KnowledgeItem> page = knowledgeService.getAdminList(pageNum, pageSize, keyword, status);
        return Result.success(page);
    }

    @PostMapping("/knowledge/audit/{id}")
    public Result<?> auditKnowledge(@PathVariable Long id, @RequestParam Integer status) {
        boolean success = knowledgeService.auditKnowledge(id, status, null);
        return success ? Result.success("审核成功") : Result.error("审核失败");
    }

    // ========== 每日一题管理 ==========
    @GetMapping("/quiz/list")
    public Result<?> getQuizList(@RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                @RequestParam(required = false) String keyword) {
        Page<DailyQuiz> page = quizService.getAdminList(pageNum, pageSize, keyword);
        return Result.success(page);
    }

    @PostMapping("/quiz/save")
    public Result<?> saveQuiz(@RequestBody DailyQuiz quiz) {
        boolean success = quizService.saveQuiz(quiz);
        return success ? Result.success("保存成功") : Result.error("保存失败");
    }

    @DeleteMapping("/quiz/delete/{id}")
    public Result<?> deleteQuiz(@PathVariable Long id) {
        boolean success = quizService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @GetMapping("/quiz/detail/{id}")
    public Result<?> getQuizDetail(@PathVariable Long id) {
        return Result.success(quizService.getById(id));
    }

    // ========== 统计数据 ==========
    @GetMapping("/statistics")
    public Result<?> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("heritageCount", heritageService.count(new LambdaQueryWrapper<Heritage>().eq(Heritage::getStatus, 1)));
        stats.put("userCount", userService.count(new LambdaQueryWrapper<User>().eq(User::getStatus, 1)));
        stats.put("productCount", productService.count(new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1)));
        stats.put("inheritorCount", inheritorService.count());
        stats.put("commentCount", commentService.count(new LambdaQueryWrapper<Comment>().eq(Comment::getStatus, 1)));
        stats.put("courseCount", courseService.count(new LambdaQueryWrapper<Course>().eq(Course::getStatus, 1)));
        stats.put("modelCount", model3dService.count(new LambdaQueryWrapper<Model3d>().eq(Model3d::getStatus, 1)));
        stats.put("knowledgeCount", knowledgeService.count(new LambdaQueryWrapper<KnowledgeItem>().eq(KnowledgeItem::getStatus, 1)));
        return Result.success(stats);
    }
}