package com.studygather.application.dto.request;

import com.studygather.application.entity.StudyApplication;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateApplicationRequest(
        @Size(
                max = StudyApplication.MAX_MESSAGE_LENGTH,
                message = "신청 메시지는 500자 이하여야 합니다."
        )
        @Pattern(regexp = "(?s).*\\S.*", message = "신청 메시지는 공백일 수 없습니다.")
        String message
) {
}
