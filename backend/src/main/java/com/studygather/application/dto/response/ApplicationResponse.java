package com.studygather.application.dto.response;

import com.studygather.application.entity.ApplicationStatus;
import com.studygather.application.entity.StudyApplication;

import java.time.LocalDateTime;

public record ApplicationResponse(
        Long id,
        Long studyId,
        Long applicantId,
        String message,
        ApplicationStatus status,
        LocalDateTime decidedAt,
        LocalDateTime createdAt
) {

    public static ApplicationResponse from(StudyApplication application) {
        return new ApplicationResponse(
                application.getId(),
                application.getStudy().getId(),
                application.getApplicant().getId(),
                application.getMessage(),
                application.getStatus(),
                application.getDecidedAt(),
                application.getCreatedAt()
        );
    }
}
