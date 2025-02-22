package org.engicodes.userservice.config.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderAsyncClient;

@Configuration
public class AWSCognitoConfig {
    @Value("${aws.cognito.region}")
    private String awsRegion;
    @Value("${aws.credentials.secretKey}")
    private String awsSecretKey;
    @Value("${aws.credentials.accessKey}")
    private String awsAccessKey;

    @Bean
    public CognitoIdentityProviderAsyncClient cognitoProviderAsyncClient() {
        try (SdkAsyncHttpClient asyncHttpClient = NettyNioAsyncHttpClient.builder().build()) {
            StaticCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(awsAccessKey, awsSecretKey)
            );
            return CognitoIdentityProviderAsyncClient.builder()
                    .httpClient(asyncHttpClient)
                    .credentialsProvider(staticCredentialsProvider)
                    .region(Region.of(awsRegion))
                    .build();
        } catch (HttpClientErrorException ex) {
            throw new HttpClientErrorException(HttpStatus.GATEWAY_TIMEOUT, "Http Connection failed!");
        }

    }
}
