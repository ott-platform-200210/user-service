package org.engicodes.userservice.config.aws;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.engicodes.userservice.config.redis.RedisConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderAsyncClient;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest;

@Configuration
@RequiredArgsConstructor
public class AWSCognitoConfig {

    @Value("${aws.cognito.region}")
    private String awsRegion;
    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;
    private final ApplicationContext applicationContext;

    @EventListener(RedisConfig.RedisReadyEvent.class)
    public void verifyCognitoConnection() {
        System.out.println("⏳ Verifying AWS Cognito IAM Credentials...");
        CognitoIdentityProviderAsyncClient cognitoClient = applicationContext.getBean(CognitoIdentityProviderAsyncClient.class);
        ListUsersRequest request = ListUsersRequest.builder()
                .userPoolId(userPoolId)
                .limit(1) // Fetch only 1 user to test access
                .build();

        Mono.fromFuture(() -> cognitoClient.listUsers(request))
                .doOnSuccess(response -> System.out.println("✅ AWS Cognito Access Verified! Retrieved " + response.users().size() + " users."))
                .doOnError(error -> System.err.println("❌ AWS Cognito Access Error: " + error.getMessage()))
                .subscribe();
    }

    @Bean
    public SdkAsyncHttpClient sdkAsyncHttpClient() {
        return NettyNioAsyncHttpClient.builder().build(); // ✅ Singleton client
    }

    @Bean
    public CognitoIdentityProviderAsyncClient cognitoProviderAsyncClient(SdkAsyncHttpClient sdkAsyncHttpClient) {
        return CognitoIdentityProviderAsyncClient.builder()
                .httpClient(sdkAsyncHttpClient)
                .credentialsProvider(DefaultCredentialsProvider.create()) // ✅ Uses IAM credentials from AWS CLI
                .region(Region.of(awsRegion))
                .build();
    }
}
