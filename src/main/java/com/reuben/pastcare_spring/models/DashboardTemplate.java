package com.reuben.pastcare_spring.models;

import com.reuben.pastcare_spring.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity representing a predefined dashboard template for specific roles.
 * Templates provide pre-configured layouts that users can apply to their dashboard.
 * Each role can have multiple templates, but only one default template.
 */
@Entity
@Table(name = "dashboard_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DashboardTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_name", nullable = false, length = 100)
    private String templateName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private Role role;

    @Column(name = "layout_config", nullable = false, columnDefinition = "TEXT")
    private String layoutConfig; // JSON configuration matching DashboardLayout format

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Column(name = "preview_image_url", length = 500)
    private String previewImageUrl; // Optional screenshot/preview image

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }

    /**
     * Helper method to check if this template is the default for its role
     */
    public boolean isDefaultTemplate() {
        return isDefault != null && isDefault;
    }

    /**
     * Helper method to get a display-friendly role name
     */
    public String getRoleDisplayName() {
        if (role == null) return "Unknown";
        return switch (role) {
            case ADMIN -> "Administrator";
            case PASTOR -> "Pastor";
            case TREASURER -> "Treasurer";
            case FELLOWSHIP_LEADER -> "Fellowship Leader";
            case MEMBER -> "Member";
            default -> role.name();
        };
    }
}
