package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.enums.ProficiencyLevel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberSkillRequest {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull(message = "Skill ID is required")
    private Long skillId;

    @NotNull(message = "Proficiency level is required")
    private ProficiencyLevel proficiencyLevel;

    private Boolean willingToServe = true;

    private Boolean currentlyServing = false;

    @Min(value = 0, message = "Years of experience cannot be negative")
    private Integer yearsOfExperience;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    private LocalDateTime acquiredDate;

    private LocalDateTime lastVerifiedDate;
}
