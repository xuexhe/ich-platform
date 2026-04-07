// src/main/java/com/ich/controller/UserController.java
package com.ich.controller;

import com.ich.dto.LoginDTO;
import com.ich.dto.LoginResponseDTO;
import com.ich.entity.User;
import com.ich.service.UserService;
import com.ich.utils.FileUploadUtils;
import com.ich.utils.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final FileUploadUtils fileUploadUtils;
    
    // 登录
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginResponseDTO result = userService.login(loginDTO);
        if (result != null) {
            return Result.success(result);
        }
        return Result.error("用户名或密码错误");
    }
    
    // 注册
    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        boolean success = userService.register(user);
        return success ? Result.success("注册成功") : Result.error("用户名已存在");
    }
    
    // 获取用户信息
    @GetMapping("/info/{id}")
    public Result<?> getUserInfo(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return user != null ? Result.success(user) : Result.error("用户不存在");
    }
    
    // 更新用户信息
    @PutMapping("/update")
    public Result<?> updateUser(@RequestBody User user) {
        boolean success = userService.updateUser(user);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }
    
    // 上传头像
    @PostMapping("/uploadAvatar")
    public Result<?> uploadAvatar(@RequestParam("file") MultipartFile file,
                                   @RequestParam("userId") Long userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            if (user.getAvatar() != null && !user.getAvatar().contains("dicebear") && !user.getAvatar().contains("/uploads/")) {
                fileUploadUtils.deleteOldAvatar(user.getAvatar());
            }
            
            String avatarUrl = fileUploadUtils.uploadAvatar(file, userId);
            
            User updateUser = new User();
            updateUser.setId(userId);
            updateUser.setAvatar(avatarUrl);
            userService.updateUser(updateUser);
            
            Map<String, String> result = new HashMap<>();
            result.put("avatarUrl", avatarUrl);
            return Result.success(result);
        } catch (IOException e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }
    
    // ========== 修改密码 ==========
    @PostMapping("/changePassword")
    public Result<?> changePassword(@RequestBody Map<String, String> params) {
        try {
            Long userId = Long.valueOf(params.get("userId"));
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");
            
            if (oldPassword == null || newPassword == null) {
                return Result.error("密码不能为空");
            }
            if (newPassword.length() < 6) {
                return Result.error("新密码至少6位");
            }
            
            boolean success = userService.changePassword(userId, oldPassword, newPassword);
            return success ? Result.success("密码修改成功") : Result.error("原密码错误");
        } catch (NumberFormatException e) {
            return Result.error("参数错误");
        }
    }
    
    // ========== 注销账号 ==========
    @PostMapping("/delete")
    public Result<?> deleteAccount(@RequestBody Map<String, String> params) {
        try {
            Long userId = Long.valueOf(params.get("userId"));
            String password = params.get("password");
            
            if (password == null) {
                return Result.error("请输入密码");
            }
            
            boolean success = userService.deleteAccount(userId, password);
            return success ? Result.success("账号已注销") : Result.error("密码错误");
        } catch (NumberFormatException e) {
            return Result.error("参数错误");
        }
    }
}