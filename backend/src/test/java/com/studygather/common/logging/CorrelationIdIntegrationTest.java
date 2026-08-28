package com.studygather.common.logging;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CorrelationIdIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void addsGeneratedCorrelationIdToResponse() throws Exception {
        MvcResult result = mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists(CorrelationIdFilter.HEADER_NAME))
                .andReturn();

        String correlationId = result.getResponse()
                .getHeader(CorrelationIdFilter.HEADER_NAME);
        assertDoesNotThrow(() -> UUID.fromString(correlationId));
    }

    @Test
    void returnsValidIncomingCorrelationIdInResponse() throws Exception {
        String correlationId = "integration-request-123";

        mockMvc.perform(get("/actuator/health")
                        .header(CorrelationIdFilter.HEADER_NAME, correlationId))
                .andExpect(status().isOk())
                .andExpect(header().string(CorrelationIdFilter.HEADER_NAME, correlationId));
    }
}
