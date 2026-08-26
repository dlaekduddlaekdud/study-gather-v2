package com.studygather.auth.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        @NotBlank String secret,
        @NotNull Duration accessTokenExpiration
) {

    public JwtProperties {
        if (accessTokenExpiration != null
                && accessTokenExpiration.compareTo(Duration.ofSeconds(1)) < 0) {
            throw new IllegalArgumentException("JWT access token 만료 시간은 1초 이상이어야 합니다.");
        }
    }
}
