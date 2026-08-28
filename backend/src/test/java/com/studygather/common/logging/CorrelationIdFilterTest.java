package com.studygather.common.logging;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void propagatesValidIncomingCorrelationIdAndClearsMdc() throws Exception {
        String correlationId = "client-request-123";
        MockHttpServletRequest request = createRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(CorrelationIdFilter.HEADER_NAME, correlationId);
        FilterChain filterChain = (servletRequest, servletResponse) -> {
            assertEquals(correlationId, MDC.get(CorrelationIdFilter.MDC_KEY));
            assertEquals(
                    correlationId,
                    servletRequest.getAttribute(CorrelationIdFilter.REQUEST_ATTRIBUTE_NAME)
            );
        };

        filter.doFilter(request, response, filterChain);

        assertEquals(correlationId, response.getHeader(CorrelationIdFilter.HEADER_NAME));
        assertNull(MDC.get(CorrelationIdFilter.MDC_KEY));
    }

    @Test
    void generatesUuidWhenCorrelationIdIsMissing() throws Exception {
        MockHttpServletRequest request = createRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
        });

        String generatedCorrelationId = response.getHeader(CorrelationIdFilter.HEADER_NAME);
        assertDoesNotThrow(() -> UUID.fromString(generatedCorrelationId));
    }

    @Test
    void replacesInvalidIncomingCorrelationId() throws Exception {
        String invalidCorrelationId = "invalid correlation id";
        MockHttpServletRequest request = createRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(CorrelationIdFilter.HEADER_NAME, invalidCorrelationId);

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
        });

        String generatedCorrelationId = response.getHeader(CorrelationIdFilter.HEADER_NAME);
        assertNotEquals(invalidCorrelationId, generatedCorrelationId);
        assertDoesNotThrow(() -> UUID.fromString(generatedCorrelationId));
    }

    private MockHttpServletRequest createRequest() {
        return new MockHttpServletRequest("GET", "/api/studies");
    }
}
