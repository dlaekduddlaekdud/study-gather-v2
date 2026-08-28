package com.studygather.common.logging;

import org.slf4j.MDC;

public final class CorrelationIdContext {

    public static final String MDC_KEY = "correlationId";

    private CorrelationIdContext() {
    }

    public static String currentId() {
        return MDC.get(MDC_KEY);
    }
}
