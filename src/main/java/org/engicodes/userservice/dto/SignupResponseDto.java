package org.engicodes.userservice.dto;

public record SignupResponseDto(
        String fullName,
        String email,
        String userName
) {
}