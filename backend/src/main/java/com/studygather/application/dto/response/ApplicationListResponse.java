package com.studygather.application.dto.response;

import com.studygather.application.entity.ApplicationStatus;
import com.studygather.application.entity.StudyApplication;

import java.time.LocalDateTime;

public record ApplicationListResponse(
        Long id,
        Long studyId,
        String studyTitle,
        Long applicantId,
        String applicantNickname,
        String message,
        ApplicationStatus status,
        LocalDateTime decidedAt,
        LocalDateTime createdAt
) {

    public static ApplicationListResponse from(StudyApplication application) {
        return new ApplicationListResponse(
                application.getId(),
                application.getStudy().getId(),
                application.getStudy().getTitle(),
                application.getApplicant().getId(),
                application.getApplicant().getNickname(),
                application.getMessage(),
                application.getStatus(),
                application.getDecidedAt(),
                application.getCreatedAt()
        );
    }
}
