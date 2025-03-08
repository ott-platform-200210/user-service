package org.engicodes.userservice.service.aws;

import org.engicodes.userservice.dto.LogInRequestDto;
import org.engicodes.userservice.dto.LogInResponseDto;
import org.engicodes.userservice.dto.SignupRequestDto;
import org.engicodes.userservice.dto.SignupResponseDto;
import org.engicodes.userservice.util.ApiResponse;
import reactor.core.publisher.Mono;

public interface CognitoAuthService {
    Mono<ApiResponse<SignupResponseDto>> registerUserWithCognito(SignupRequestDto requestDto);

    Mono<ApiResponse<LogInResponseDto>> loginUserWithCognito(LogInRequestDto logInRequestDto);
}
