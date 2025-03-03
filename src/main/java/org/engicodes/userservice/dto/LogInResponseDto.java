package org.engicodes.userservice.dto;


public record LogInResponseDto(
        String fullName,
        String email,
        String userName,
        Integer age,
        String role,
        String subStatus,
        boolean emailVerified,
        String token
) {
}