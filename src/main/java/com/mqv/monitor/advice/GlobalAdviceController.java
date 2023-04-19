package com.mqv.monitor.advice;

import com.mqv.monitor.exception.RateLimitExceedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public record GlobalAdviceController() {
    private static final String X_RETRY_AFTER_HEADER = "X-Retry-After";

    @ExceptionHandler(value = {RateLimitExceedException.class})
    public ResponseEntity<Void> handleRateLimitExceedException(RateLimitExceedException exception) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(X_RETRY_AFTER_HEADER, String.valueOf(exception.getRetryAfter().toSeconds()))
                .body(null);
    }

    @ExceptionHandler({ RequestNotPermitted.class })
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public void handleRequestNotPermitted() {
    }
}
