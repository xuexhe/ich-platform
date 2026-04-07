package com.ich.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemDTO {
    private Long productId;
    private String productName;
    private String productCover;
    private Integer quantity;
    private BigDecimal price;
}