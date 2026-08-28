package com.studygather.common.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
        @NotEmpty(message = "CORS 허용 Origin은 하나 이상이어야 합니다.")
        List<@NotBlank(message = "CORS 허용 Origin은 빈 값일 수 없습니다.") String> allowedOrigins
) {
}
