package com.studygather.study.dto.response;

import com.studygather.study.entity.Study;
import com.studygather.study.entity.StudyStatus;

import java.time.LocalDateTime;

public record StudyResponse(
        Long id,
        Long ownerId,
        String title,
        String description,
        int capacity,
        int approvedCount,
        LocalDateTime recruitmentDeadline,
        StudyStatus status,
        LocalDateTime createdAt
) {

    public static StudyResponse from(Study study) {
        return new StudyResponse(
                study.getId(),
                study.getOwner().getId(),
                study.getTitle(),
                study.getDescription(),
                study.getCapacity(),
                study.getApprovedCount(),
                study.getRecruitmentDeadline(),
                study.getStatus(),
                study.getCreatedAt()
        );
    }
}
