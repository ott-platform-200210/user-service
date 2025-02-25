package org.engicodes.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequestDto(
        @NotBlank
        String fullName,
        @Email
        @NotBlank
        String email,
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,
        @NotBlank(message = "Username is required")
        @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Username must contain only alphabets and numbers")
        @Size(min = 3)
        String userName
) {
}