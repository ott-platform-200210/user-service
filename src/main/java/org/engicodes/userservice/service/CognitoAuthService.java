package org.engicodes.userservice.service;

import lombok.RequiredArgsConstructor;
import org.engicodes.userservice.dao.UserDao;
import org.engicodes.userservice.dto.SignupRequestDto;
import org.engicodes.userservice.dto.SignupResponseDto;
import org.engicodes.userservice.util.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderAsyncClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

@Service
@RequiredArgsConstructor
public class CognitoAuthService {

    @Value("${aws.cognito.clientId}")
    private String cognitoUserClientId;

    private final CognitoIdentityProviderAsyncClient cognitoClient;
    private final UserDao userDao;

    public Mono<ApiResponse<SignupResponseDto>> registerUserWithCognito(SignupRequestDto requestDto) {
        return checkUserExists(requestDto)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.just(new ApiResponse<SignupResponseDto>("FAILURE", "Email or Username already exists.", null));
                    }
                    return registerUserInCognito(requestDto)
                            .map(_ -> new ApiResponse<>("SUCCESS", "Signup successful!",
                                    new SignupResponseDto(requestDto.fullName(), requestDto.email(), requestDto.userName(), "Please verify your email.")));
                })
                .onErrorResume(error -> Mono.just(new ApiResponse<>("FAILURE", "Signup failed: " + error.getMessage(), null)));
    }

    private Mono<Boolean> checkUserExists(SignupRequestDto requestDto) {
        return Mono.zip(
                        userDao.checkIfEmailExists(requestDto.email()),
                        userDao.checkIfUserNameExists(requestDto.userName())
                )
                .map(tuple -> tuple.getT1() || tuple.getT2()); // Returns true if either email or username exists
    }

    private Mono<SignUpResponse> registerUserInCognito(SignupRequestDto requestDto) {
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .clientId(cognitoUserClientId)
                .username(requestDto.email())
                .password(requestDto.password())
                .userAttributes(
                        AttributeType.builder().name("email").value(requestDto.email()).build(),
                        AttributeType.builder().name("custom:userName").value(requestDto.userName()).build(),
                        AttributeType.builder().name("custom:fullName").value(requestDto.fullName()).build()
                )
                .build();

        return Mono.fromFuture(() -> cognitoClient.signUp(signUpRequest));
    }
}
