package org.engicodes.userservice.dto;

public record SignInRequestDto(
        String email,
        String password
) {
}
