package org.engicodes.userservice.dto;

public record LogInRequestDto(
        String email,
        String password
) {
}
