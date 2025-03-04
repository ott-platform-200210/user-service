package org.engicodes.userservice.service.aws;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.engicodes.userservice.dto.SignupRequestDto;
import org.engicodes.userservice.dto.SignupResponseDto;
import org.engicodes.userservice.exception.CognitoSignupException;
import org.engicodes.userservice.service.user.UserService;
import org.engicodes.userservice.util.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderAsyncClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class CognitoAuthService {

    @Value("${aws.cognito.clientId}")
    private String cognitoUserClientId;
    @Value("${aws.cognito.userPoolId}")
    private String cognitoUserPoolId;

    private final CognitoIdentityProviderAsyncClient cognitoClient;
    private final UserService userService;

    public Mono<ApiResponse<SignupResponseDto>> registerUserWithCognito(SignupRequestDto requestDto) {
        return userService.checkIfUserExistsInSignUp(requestDto)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.just(new ApiResponse<SignupResponseDto>("FAILURE", "Email or Username already exists.", null));
                    }
                    return registerUserInCognito(requestDto)
                            .flatMap(response -> userService.saveUser(requestDto)
                                    .thenReturn(response)
                                    .onErrorResume(dbError -> {
                                        log.error("❌ {}", dbError.getMessage());
                                        return deleteUserFromCognito(requestDto.userName())
                                                .then(Mono.error(new CognitoSignupException("Signup failed, rollback performed.", HttpStatus.INTERNAL_SERVER_ERROR)));
                                    }))
                            .map(_ -> new ApiResponse<>("SUCCESS", "Signup successful!", new SignupResponseDto(requestDto.fullName(), requestDto.email(), requestDto.userName(), "Please verify your email.")));
                });
    }

//        return Mono.zip(
//                        userDao.checkIfEmailExists(requestDto.email()),
//                        userDao.checkIfUserNameExists(requestDto.userName())
//                )
//                .map(tuple -> tuple.getT1() || tuple.getT2()); // Returns true if either email or username exists

    private Mono<SignUpResponse> registerUserInCognito(SignupRequestDto requestDto) {
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .clientId(cognitoUserClientId)
                .username(requestDto.userName())
                .password(requestDto.password())
                .userAttributes(
                        AttributeType.builder().name("email").value(requestDto.email()).build(),
                        AttributeType.builder().name("custom:userName").value(requestDto.userName()).build(),
                        AttributeType.builder().name("custom:fullName").value(requestDto.fullName()).build()
                )
                .build();

        return Mono.fromFuture(() -> cognitoClient.signUp(signUpRequest))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2))
                        .filter(error -> error instanceof TooManyRequestsException))
                .doOnSuccess(_ -> log.info("✅ Cognito user {} registered successfully!", requestDto.email()))
                .onErrorResume(error -> {
                    log.error("❌ Cognito signup failed: {}", error.getMessage());
                    return switch (error) {
                        case UsernameExistsException _ ->
                                Mono.error(new CognitoSignupException(error.getMessage(), HttpStatus.CONFLICT));
                        case InvalidPasswordException _ ->
                                Mono.error(new CognitoSignupException(error.getMessage(), HttpStatus.BAD_REQUEST));
                        case TooManyRequestsException _ ->
                                Mono.error(new CognitoSignupException(error.getMessage(), HttpStatus.TOO_MANY_REQUESTS));
                        case InvalidParameterException _ ->
                                Mono.error(new CognitoSignupException(error.getMessage(), HttpStatus.BAD_REQUEST));
                        default ->
                                Mono.error(new CognitoSignupException(error.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
                    };
                });
    }

    public Mono<Void> deleteUserFromCognito(String username) {
        AdminDeleteUserRequest deleteUserRequest = AdminDeleteUserRequest.builder()
                .userPoolId(cognitoUserPoolId)
                .username(username)
                .build();
        return Mono.fromFuture(() -> cognitoClient.adminDeleteUser(deleteUserRequest))
                .doOnSuccess(_ -> log.info("✅ Rolled back Cognito user: {}", username))
                .doOnError(error -> log.error("❌ Failed to rollback Cognito user: {}", username, error))
                .then();
    }
}