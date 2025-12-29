package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.enums.InsightCategory;
import com.reuben.pastcare_spring.enums.InsightSeverity;
import com.reuben.pastcare_spring.enums.InsightType;
import com.reuben.pastcare_spring.models.Insight;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for insights.
 * Dashboard Phase 2.4: Advanced Analytics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightResponse {

    private Long id;
    private Long churchId;
    private InsightType insightType;
    private InsightCategory category;
    private String title;
    private String description;
    private InsightSeverity severity;
    private Boolean actionable;
    private String actionUrl;
    private Boolean dismissed;
    private Instant dismissedAt;
    private Long dismissedBy;
    private String dismissedByName;
    private String metadata;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Create InsightResponse from Insight entity
     */
    public static InsightResponse fromEntity(Insight insight) {
        return InsightResponse.builder()
            .id(insight.getId())
            .churchId(insight.getChurchId())
            .insightType(insight.getInsightType())
            .category(insight.getCategory())
            .title(insight.getTitle())
            .description(insight.getDescription())
            .severity(insight.getSeverity())
            .actionable(insight.getActionable())
            .actionUrl(insight.getActionUrl())
            .dismissed(insight.getDismissed())
            .dismissedAt(insight.getDismissedAt())
            .dismissedBy(insight.getDismissedBy() != null ? insight.getDismissedBy().getId() : null)
            .dismissedByName(insight.getDismissedBy() != null ? insight.getDismissedBy().getName() : null)
            .metadata(insight.getMetadata())
            .createdAt(insight.getCreatedAt())
            .updatedAt(insight.getUpdatedAt())
            .build();
    }
}
