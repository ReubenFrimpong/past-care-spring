package com.reuben.pastcare_spring.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculateSmsCostResponse {

    private BigDecimal cost;
    private Integer messageCount;
    private String countryCode;
    private String destination;
    private Boolean isLocal;
    private BigDecimal ratePerSms;
}
