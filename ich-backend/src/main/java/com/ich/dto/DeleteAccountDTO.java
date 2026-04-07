package com.ich.dto;

import lombok.Data;

@Data
public class DeleteAccountDTO {
    private Long userId;
    private String password;
}