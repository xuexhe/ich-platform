package com.ich.dto;

import com.ich.entity.User;
import lombok.Data;

@Data
public class LoginResponseDTO {
    private String token;
    private User userInfo;
    
    public LoginResponseDTO(String token, User userInfo) {
        this.token = token;
        this.userInfo = userInfo;
    }
}