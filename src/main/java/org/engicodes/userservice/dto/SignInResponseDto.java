package org.engicodes.userservice.dto;


public record SignInResponseDto(
        String fullName,
        String email,
        String userName,
        Integer age,
        String role,
        String subStatus
) {
}