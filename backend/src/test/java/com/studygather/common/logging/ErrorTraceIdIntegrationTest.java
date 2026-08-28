package com.studygather.common.logging;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ErrorTraceIdIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void includesSameTraceIdInValidationErrorHeaderAndBody() throws Exception {
        String traceId = "validation-error-123";

        mockMvc.perform(post("/api/auth/signup")
                        .header(CorrelationIdFilter.HEADER_NAME, traceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invalid-email",
                                  "password": "short",
                                  "nickname": "x"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CorrelationIdFilter.HEADER_NAME, traceId))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.traceId").value(traceId));
    }

    @Test
    void includesSameTraceIdInAuthenticationErrorHeaderAndBody() throws Exception {
        String traceId = "authentication-error-123";

        mockMvc.perform(get("/api/users/me")
                        .header(CorrelationIdFilter.HEADER_NAME, traceId))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(CorrelationIdFilter.HEADER_NAME, traceId))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."))
                .andExpect(jsonPath("$.traceId").value(traceId));
    }
}
