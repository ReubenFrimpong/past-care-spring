package com.reuben.pastcare_spring.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SmsCreditResponse {

    private Long id;
    private Long userId;
    private String userName;
    private BigDecimal balance;
    private BigDecimal totalPurchased;
    private BigDecimal totalUsed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
