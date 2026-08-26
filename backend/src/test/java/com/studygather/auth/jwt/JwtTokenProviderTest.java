package com.studygather.auth.jwt;

import com.studygather.auth.config.JwtProperties;
import com.studygather.user.entity.UserRole;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private static final String TEST_SECRET =
            "dGVzdC1qd3Qtc2VjcmV0LWtleS1tdXN0LWJlLWF0LWxlYXN0LTMyLWJ5dGVz";

    @Test
    void validatesTokenSignedWithConfiguredSecret() {
        JwtTokenProvider tokenProvider = createTokenProvider(Duration.ofHours(1));
        String token = tokenProvider.createAccessToken(1L, UserRole.USER);

        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    void rejectsTamperedToken() {
        JwtTokenProvider tokenProvider = createTokenProvider(Duration.ofHours(1));
        String token = tokenProvider.createAccessToken(1L, UserRole.USER);
        String tamperedToken = tamperSignature(token);

        assertFalse(tokenProvider.validateToken(tamperedToken));
    }

    @Test
    void rejectsExpiredToken() {
        JwtProperties properties = createProperties(Duration.ofHours(1));
        Instant issuedAt = Instant.parse("2026-08-26T00:00:00Z");
        JwtTokenProvider issuer = new JwtTokenProvider(
                properties,
                Clock.fixed(issuedAt, ZoneOffset.UTC)
        );
        JwtTokenProvider validator = new JwtTokenProvider(
                properties,
                Clock.fixed(issuedAt.plus(Duration.ofHours(2)), ZoneOffset.UTC)
        );
        String token = issuer.createAccessToken(1L, UserRole.USER);

        assertFalse(validator.validateToken(token));
    }

    private JwtTokenProvider createTokenProvider(Duration expiration) {
        return new JwtTokenProvider(createProperties(expiration));
    }

    private JwtProperties createProperties(Duration expiration) {
        return new JwtProperties(TEST_SECRET, expiration);
    }

    private String tamperSignature(String token) {
        int signatureStart = token.lastIndexOf('.') + 1;
        char current = token.charAt(signatureStart);
        char replacement = current == 'a' ? 'b' : 'a';

        return token.substring(0, signatureStart)
                + replacement
                + token.substring(signatureStart + 1);
    }
}
