package org.engicodes.userservice.util;

public record ApiResponse<T>(
        String status,
        String message,
        T data
) {
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data);
    }

    public static <T> ApiResponse<T> failure(String message, T data) {
        return new ApiResponse<>("FAILURE", message, data);
    }
}
