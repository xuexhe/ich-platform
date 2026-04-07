package com.ich.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderDTO {
    private Long userId;
    private BigDecimal totalAmount;
    private String address;
    private String phone;
    private String receiver;
    private List<OrderItemDTO> items;
}