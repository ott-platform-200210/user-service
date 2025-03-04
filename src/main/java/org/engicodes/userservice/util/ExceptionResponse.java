package org.engicodes.userservice.util;

import java.time.Instant;

public record ExceptionResponse(
        Integer statusCode,
        String message,
        Instant timestamp
) {
}
