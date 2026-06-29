package com.mv.event_gateway_api.config;

import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

@Component
public class TraceInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        String traceId = TraceContext.get();
        if (traceId == null || traceId.isBlank()) {
            traceId = MDC.get("traceId");
        }
        if (traceId != null && !traceId.isBlank()) {
            request.getHeaders().set(TraceFilter.TRACE_HEADER, traceId);
        }
        return execution.execute(request, body);
    }
}
