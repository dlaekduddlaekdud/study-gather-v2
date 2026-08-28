package com.studygather.common.api;

import com.studygather.common.logging.CorrelationIdContext;

public record ErrorResponse(
        boolean success,
        String message,
        Void data,
        String traceId
) {

    public static ErrorResponse from(String message) {
        return new ErrorResponse(
                false,
                message,
                null,
                CorrelationIdContext.currentId()
        );
    }
}
