package org.engicodes.userservice.dto;

public record SignupRequestDto(
        String fullName,
        String email,
        String password,
        Integer age
) {
}