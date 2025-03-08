package org.engicodes.userservice.controller.auth;

import lombok.RequiredArgsConstructor;
import org.engicodes.userservice.dto.LogInRequestDto;
import org.engicodes.userservice.dto.SignupRequestDto;
import org.engicodes.userservice.service.aws.CognitoAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static org.engicodes.userservice.util.AppConstants.*;

@RestController
@RequestMapping(AUTH_BASE_URL)
@RequiredArgsConstructor
public class CognitoAuthController {
    private final CognitoAuthService cognitoAuthService;

    @PostMapping(AUTH_SIGNUP_URL)
    public Mono<ResponseEntity<?>> signUpWithCognito(
            @Validated @RequestBody SignupRequestDto signupRequestDto
    ) {
        return cognitoAuthService.registerUserWithCognito(signupRequestDto)
                .map(response -> {
                    if ("SUCCESS".equals(response.status())) {
                        return ResponseEntity.status(HttpStatus.CREATED).body(response);
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    }
                });
    }

    @PostMapping(AUTH_LOGIN_URL)
    public Mono<ResponseEntity<?>> loginWithCognito(
            @Validated @RequestBody LogInRequestDto logInRequestDto
    ) {
        return cognitoAuthService.loginUserWithCognito(logInRequestDto)
                .map(response -> {
                    if ("SUCCESS".equals(response.status())) {
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    }
                });
    }
}
