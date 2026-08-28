package com.studygather.common.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "spring.datasource")
public record DatabaseProperties(
        @NotBlank(message = "DB URL은 필수입니다.")
        String url,

        @NotBlank(message = "DB 사용자 이름은 필수입니다.")
        String username,

        @NotBlank(message = "DB 비밀번호는 필수입니다.")
        String password
) {
}
