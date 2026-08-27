package com.studygather.study.dto.request;

import com.studygather.study.entity.Study;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateStudyRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = Study.MAX_TITLE_LENGTH, message = "제목은 100자 이하여야 합니다.")
        String title,

        @NotBlank(message = "설명은 필수입니다.")
        @Size(max = Study.MAX_DESCRIPTION_LENGTH, message = "설명은 2000자 이하여야 합니다.")
        String description,

        @Min(value = Study.MIN_CAPACITY, message = "정원은 2명 이상이어야 합니다.")
        int capacity,

        @NotNull(message = "모집 마감일은 필수입니다.")
        @Future(message = "모집 마감일은 현재 시각 이후여야 합니다.")
        LocalDateTime recruitmentDeadline
) {
}
