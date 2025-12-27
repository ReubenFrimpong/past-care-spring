package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.ChurchSmsCredit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for ChurchSmsCredit responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChurchSmsCreditResponse {

    private Long id;
    private Long churchId;
    private String churchName;
    private BigDecimal balance;
    private BigDecimal totalPurchased;
    private BigDecimal totalUsed;
    private LocalDateTime lastPurchaseAt;
    private Boolean lowBalanceAlertSent;
    private BigDecimal lowBalanceThreshold;
    private Boolean hasLowBalance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert entity to response DTO
     */
    public static ChurchSmsCreditResponse fromEntity(ChurchSmsCredit credit) {
        ChurchSmsCreditResponse response = new ChurchSmsCreditResponse();
        response.setId(credit.getId());
        response.setChurchId(credit.getChurch().getId());
        response.setChurchName(credit.getChurch().getName());
        response.setBalance(credit.getBalance());
        response.setTotalPurchased(credit.getTotalPurchased());
        response.setTotalUsed(credit.getTotalUsed());
        response.setLastPurchaseAt(credit.getLastPurchaseAt());
        response.setLowBalanceAlertSent(credit.getLowBalanceAlertSent());
        response.setLowBalanceThreshold(credit.getLowBalanceThreshold());
        response.setHasLowBalance(credit.hasLowBalance());
        response.setCreatedAt(credit.getCreatedAt());
        response.setUpdatedAt(credit.getUpdatedAt());
        return response;
    }
}
