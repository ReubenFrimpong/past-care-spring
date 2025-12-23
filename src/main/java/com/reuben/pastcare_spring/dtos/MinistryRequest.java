package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.enums.MinistryStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MinistryRequest {

    @NotBlank(message = "Ministry name is required")
    @Size(min = 2, max = 100, message = "Ministry name must be between 2 and 100 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private Long leaderId;

    private MinistryStatus status = MinistryStatus.ACTIVE;

    @Size(max = 200, message = "Meeting schedule cannot exceed 200 characters")
    private String meetingSchedule;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String contactEmail;

    @Size(max = 20, message = "Phone cannot exceed 20 characters")
    private String contactPhone;

    private Set<Long> requiredSkillIds;

    private Set<Long> memberIds;
}
