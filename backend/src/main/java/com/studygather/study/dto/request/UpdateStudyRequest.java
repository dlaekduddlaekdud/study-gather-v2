package com.studygather.study.dto.request;

import com.studygather.study.entity.Study;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateStudyRequest(
        @Size(max = Study.MAX_TITLE_LENGTH, message = "제목은 100자 이하여야 합니다.")
        @Pattern(regexp = "(?s).*\\S.*", message = "제목은 공백일 수 없습니다.")
        String title,

        @Size(max = Study.MAX_DESCRIPTION_LENGTH, message = "설명은 2000자 이하여야 합니다.")
        @Pattern(regexp = "(?s).*\\S.*", message = "설명은 공백일 수 없습니다.")
        String description,

        @Min(value = Study.MIN_CAPACITY, message = "정원은 2명 이상이어야 합니다.")
        Integer capacity,

        @Future(message = "모집 마감일은 현재 시각 이후여야 합니다.")
        LocalDateTime recruitmentDeadline
) {

    @AssertTrue(message = "수정할 내용을 하나 이상 입력해야 합니다.")
    public boolean isAnyFieldPresent() {
        return title != null
                || description != null
                || capacity != null
                || recruitmentDeadline != null;
    }
}
