package com.mv.event_gateway_api.controller;

import com.mv.event_gateway_api.service.EventNotFoundException;
import com.mv.event_gateway_api.service.InvalidAccountException;
import com.mv.event_ledger_domain.model.ErrorResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import java.time.Instant;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(EventNotFoundException.class)
    ResponseEntity<ErrorResponse> notFound(EventNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Validation failed");
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(InvalidAccountException.class)
    ResponseEntity<ErrorResponse> invalidAccount(InvalidAccountException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(RequestNotPermitted.class)
    ResponseEntity<ErrorResponse> rateLimited(RequestNotPermitted ex) {
        return error(HttpStatus.TOO_MANY_REQUESTS, "Gateway rate limit exceeded");
    }

    @ExceptionHandler({CallNotPermittedException.class, RestClientException.class})
    ResponseEntity<ErrorResponse> dependencyUnavailable(Exception ex) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "Account Service unavailable");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> unexpected(Exception ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .traceId(MDC.get("traceId"))
                .build());
    }
}
