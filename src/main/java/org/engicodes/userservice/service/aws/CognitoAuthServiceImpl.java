package org.engicodes.userservice.service.aws;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.engicodes.userservice.dto.LogInRequestDto;
import org.engicodes.userservice.dto.LogInResponseDto;
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
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CognitoAuthServiceImpl implements CognitoAuthService {

    @Value("${aws.cognito.clientId}")
    private String cognitoUserClientId;
    @Value("${aws.cognito.userPoolId}")
    private String cognitoUserPoolId;

    private final CognitoIdentityProviderAsyncClient cognitoClient;
    private final UserService userService;

    @Override
    public Mono<ApiResponse<SignupResponseDto>> registerUserWithCognito(SignupRequestDto requestDto) {
        return userService.checkIfUserExistsInSignUp(requestDto)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.just(new ApiResponse<>("FAILURE", "Email or Username already exists.", null));
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

    @Override
    public Mono<ApiResponse<LogInResponseDto>> loginUserWithCognito(LogInRequestDto logInRequestDto) {
        return initiateAuthResponse(logInRequestDto)
                .flatMap(authResult -> {
                    String accessToken = authResult.authenticationResult().accessToken();
                    String idToken = authResult.authenticationResult().idToken();
                    String refreshToken = authResult.authenticationResult().refreshToken();
                    log.info("access_token: {}\nrefresh_token: {}\nidToken: {}", accessToken, refreshToken, idToken);
                    return userService.getUserByUserEmail(logInRequestDto.email())
                            .map(user -> new ApiResponse<>("SUCCESS", "Login successful", new LogInResponseDto(user.getFullName(), user.getEmail(), user.getUserName(), user.getAge(), user.getRole().name(), user.getSubscriptionStatus().getStatusInfo(), user.isEmailVerified(), accessToken)))
                            .switchIfEmpty(Mono.just(new ApiResponse<>("FAILURE", "User not found!", null)));
                });
    }

    private Mono<InitiateAuthResponse> initiateAuthResponse(LogInRequestDto logInRequestDto) {
        InitiateAuthRequest initiateAuthRequest = InitiateAuthRequest.builder()
                .clientId(cognitoUserClientId)
                .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                .authParameters(Map.of(
                        "USERNAME", logInRequestDto.email(),
                        "PASSWORD", logInRequestDto.password()
                ))
                .build();

        return Mono.fromFuture(() -> cognitoClient.initiateAuth(initiateAuthRequest))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2))
                        .filter(error -> error instanceof TooManyRequestsException))
                .doOnSuccess(_ -> log.info("✅ User {} authenticated successfully!", logInRequestDto.email()))
                .onErrorResume(error -> {
                    log.error("❌ Cognito authentication failed: {}", error.getMessage());
                    return Mono.error(new RuntimeException(error.getMessage()));
                });
    }

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