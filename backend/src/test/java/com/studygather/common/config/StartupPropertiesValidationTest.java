package com.studygather.common.config;

import com.studygather.auth.config.JwtProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class StartupPropertiesValidationTest {

    private static final String VALID_SECRET =
            "dGVzdC1qd3Qtc2VjcmV0LWtleS1tdXN0LWJlLWF0LWxlYXN0LTMyLWJ5dGVz";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesConfiguration.class)
            .withPropertyValues(
                    "spring.datasource.url=jdbc:mysql://localhost:3307/study_gather",
                    "spring.datasource.username=study_gather",
                    "spring.datasource.password=study_gather_dev_password",
                    "jwt.secret=" + VALID_SECRET,
                    "jwt.access-token-expiration=1h"
            );

    @Test
    void startsWithValidProperties() {
        contextRunner.run(context -> assertNull(context.getStartupFailure()));
    }

    @Test
    void rejectsBlankDatabaseUrl() {
        assertStartupFails("spring.datasource.url=");
    }

    @Test
    void rejectsBlankDatabaseUsername() {
        assertStartupFails("spring.datasource.username=");
    }

    @Test
    void rejectsBlankDatabasePassword() {
        assertStartupFails("spring.datasource.password=");
    }

    @Test
    void rejectsBlankJwtSecret() {
        assertStartupFails("jwt.secret=");
    }

    @Test
    void rejectsNonBase64JwtSecret() {
        assertStartupFails("jwt.secret=not-base64-secret!");
    }

    @Test
    void rejectsShortJwtSecret() {
        assertStartupFails("jwt.secret=c2hvcnQ=");
    }

    @Test
    void rejectsTooShortAccessTokenExpiration() {
        assertStartupFails("jwt.access-token-expiration=0s");
    }

    @Test
    void rejectsMissingAccessTokenExpiration() {
        assertStartupFails("jwt.access-token-expiration=");
    }

    private void assertStartupFails(String invalidProperty) {
        contextRunner
                .withPropertyValues(invalidProperty)
                .run(context -> assertNotNull(context.getStartupFailure()));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties({DatabaseProperties.class, JwtProperties.class})
    static class PropertiesConfiguration {
    }
}
