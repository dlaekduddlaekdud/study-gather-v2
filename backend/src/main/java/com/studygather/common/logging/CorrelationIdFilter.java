package com.studygather.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-Correlation-ID";
    public static final String REQUEST_ATTRIBUTE_NAME =
            CorrelationIdFilter.class.getName() + ".correlationId";

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
    private static final Pattern VALID_CORRELATION_ID =
            Pattern.compile("[A-Za-z0-9._-]{1,100}");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String correlationId = resolveCorrelationId(request.getHeader(HEADER_NAME));
        String previousCorrelationId = MDC.get(CorrelationIdContext.MDC_KEY);

        request.setAttribute(REQUEST_ATTRIBUTE_NAME, correlationId);
        response.setHeader(HEADER_NAME, correlationId);
        MDC.put(CorrelationIdContext.MDC_KEY, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            logRequestCompletion(request, response);
            restorePreviousCorrelationId(previousCorrelationId);
        }
    }

    private String resolveCorrelationId(String requestedCorrelationId) {
        if (requestedCorrelationId != null) {
            String trimmedCorrelationId = requestedCorrelationId.trim();
            if (VALID_CORRELATION_ID.matcher(trimmedCorrelationId).matches()) {
                return trimmedCorrelationId;
            }
        }

        return UUID.randomUUID().toString();
    }

    private void logRequestCompletion(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (!request.getRequestURI().startsWith("/actuator/health")) {
            log.info(
                    "요청 완료: method={}, uri={}, status={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus()
            );
        }
    }

    private void restorePreviousCorrelationId(String previousCorrelationId) {
        if (previousCorrelationId == null) {
            MDC.remove(CorrelationIdContext.MDC_KEY);
            return;
        }

        MDC.put(CorrelationIdContext.MDC_KEY, previousCorrelationId);
    }
}
