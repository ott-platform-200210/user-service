package org.engicodes.userservice.util;

public record ApiResponse(
        String status,
        String message,
        Object data
) {
}
