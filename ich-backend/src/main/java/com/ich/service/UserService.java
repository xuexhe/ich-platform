// src/main/java/com/ich/service/UserService.java
package com.ich.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ich.dto.LoginDTO;
import com.ich.dto.LoginResponseDTO;
import com.ich.entity.User;
import com.ich.mapper.UserMapper;
import com.ich.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    // 登录
    public LoginResponseDTO login(LoginDTO loginDTO) {
        User user = baseMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getUsername, loginDTO.getUsername())
        );
        
        if (user != null && passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            if (user.getStatus() != null && user.getStatus() == 0) {
                return null;
            }
            String token = jwtUtils.generateToken(user.getId());
            user.setPassword(null);
            return new LoginResponseDTO(token, user);
        }
        return null;
    }

    // 注册
    public boolean register(User user) {
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<User>()
                .eq(User::getUsername, user.getUsername())
        );
        if (count > 0) {
            return false;
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("user");
        user.setStatus(1);
        user.setAvatar("/images/default-avatar.png");
        user.setCreateTime(LocalDateTime.now());
        return save(user);
    }

    // 根据ID获取用户信息
    public User getUserById(Long id) {
        User user = getById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    // 更新用户信息
    public boolean updateUser(User user) {
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        user.setUpdateTime(LocalDateTime.now());
        return updateById(user);
    }

    // 修改密码
    @Transactional
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            return false;
        }
        
        // 验证原密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        
        // 新密码加密
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        return updateById(user);
    }

    // 注销账号
    @Transactional
    public boolean deleteAccount(Long userId, String password) {
        User user = getById(userId);
        if (user == null) {
            return false;
        }
        
        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return false;
        }
        
        // 软删除（设置status为0）
        user.setStatus(0);
        user.setUpdateTime(LocalDateTime.now());
        return updateById(user);
    }

    // 管理员分页查询用户
    public Page<User> getAdminList(Integer pageNum, Integer pageSize, String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword).or().like(User::getNickname, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return page(page, wrapper);
    }

    // 更新用户状态
    public boolean updateStatus(Long id, Integer status) {
        User user = getById(id);
        if (user == null) {
            return false;
        }
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        return updateById(user);
    }
}