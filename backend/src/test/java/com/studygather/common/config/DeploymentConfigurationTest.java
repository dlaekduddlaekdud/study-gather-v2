package com.studygather.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeploymentConfigurationTest {

    @Test
    void usesDefaultPortForLocalEnvironment() throws IOException {
        StandardEnvironment environment = loadApplicationEnvironment();

        assertEquals("8080", environment.getProperty("server.port"));
    }

    @Test
    void usesPortProvidedByDeploymentPlatform() throws IOException {
        StandardEnvironment environment = loadApplicationEnvironment();
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(environment, "PORT=10000");

        assertEquals("10000", environment.getProperty("server.port"));
    }

    private StandardEnvironment loadApplicationEnvironment() throws IOException {
        StandardEnvironment environment = new StandardEnvironment();
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        environment.getPropertySources().addFirst(
                loader.load("application", new ClassPathResource("application.yaml")).getFirst()
        );
        return environment;
    }
}
