// src/main/java/com/ich/dto/QuizSubmitDTO.java
package com.ich.dto;

import lombok.Data;

@Data
public class QuizSubmitDTO {
    private Long userId;
    private Long quizId;
    private String answer;
}