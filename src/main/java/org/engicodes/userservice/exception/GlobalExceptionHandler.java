package org.engicodes.userservice.exception;

import org.engicodes.userservice.util.ExceptionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CognitoSignupException.class)
    public Mono<ResponseEntity<?>> cognitoSignupExceptionHandler(CognitoSignupException exception) {
        ExceptionResponse exResponse = new ExceptionResponse(
                exception.getHttpStatus().value(),
                exception.getMessage(),
                Instant.now()
        );
        return Mono.just(ResponseEntity.status(exception.getHttpStatus().value()).body(exResponse));
    }
}