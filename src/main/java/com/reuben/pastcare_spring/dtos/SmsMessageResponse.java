package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.SmsStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SmsMessageResponse {

    private Long id;
    private Long senderId;
    private String senderName;
    private Long memberId;
    private String recipientPhone;
    private String recipientName;
    private String message;
    private Integer messageCount;
    private BigDecimal cost;
    private SmsStatus status;
    private String gatewayMessageId;
    private String deliveryStatus;
    private LocalDateTime deliveryTime;
    private LocalDateTime scheduledTime;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}
