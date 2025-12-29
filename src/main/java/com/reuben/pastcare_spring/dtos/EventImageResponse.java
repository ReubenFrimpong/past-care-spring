package com.reuben.pastcare_spring.dtos;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventImageResponse {

    private Long id;
    private Long eventId;
    private String imageUrl;
    private String caption;
    private Integer displayOrder;
    private Boolean isCoverImage;
    private Long uploadedById;
    private LocalDateTime uploadedAt;
}
