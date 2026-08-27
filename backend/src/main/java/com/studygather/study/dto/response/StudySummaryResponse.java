package com.studygather.study.dto.response;

import com.studygather.study.entity.Study;
import com.studygather.study.entity.StudyStatus;

import java.time.LocalDateTime;

public record StudySummaryResponse(
        Long id,
        Long ownerId,
        String title,
        int capacity,
        int approvedCount,
        LocalDateTime recruitmentDeadline,
        StudyStatus status
) {

    public static StudySummaryResponse from(Study study) {
        return new StudySummaryResponse(
                study.getId(),
                study.getOwner().getId(),
                study.getTitle(),
                study.getCapacity(),
                study.getApprovedCount(),
                study.getRecruitmentDeadline(),
                study.getStatus()
        );
    }
}
