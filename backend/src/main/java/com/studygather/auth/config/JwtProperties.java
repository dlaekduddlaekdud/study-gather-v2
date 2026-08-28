package com.studygather.auth.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.Base64;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        @NotBlank(message = "JWT 비밀키는 필수입니다.")
        String secret,

        @NotNull(message = "JWT access token 만료 시간은 필수입니다.")
        Duration accessTokenExpiration
) {

    private static final int MINIMUM_SECRET_BYTES = 32;

    public JwtProperties {
        validateSecret(secret);

        if (accessTokenExpiration != null
                && accessTokenExpiration.compareTo(Duration.ofSeconds(1)) < 0) {
            throw new IllegalArgumentException("JWT access token 만료 시간은 1초 이상이어야 합니다.");
        }
    }

    private static void validateSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            return;
        }

        byte[] decodedSecret;
        try {
            decodedSecret = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("JWT 비밀키는 유효한 Base64 형식이어야 합니다.", exception);
        }

        if (decodedSecret.length < MINIMUM_SECRET_BYTES) {
            throw new IllegalArgumentException("JWT 비밀키는 디코딩 후 32바이트 이상이어야 합니다.");
        }
    }
}
