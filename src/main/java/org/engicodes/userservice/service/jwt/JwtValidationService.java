package org.engicodes.userservice.service.jwt;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JwtValidationService {
    @Value("${aws.cognito.jwksUrl}")
    private String jwksUrl;
    private final WebClient webClient;
    private final Map<String, RSAKey> publicKeysCache = new ConcurrentHashMap<>();

    public JwtValidationService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(jwksUrl).build();
    }

    public Mono<Boolean> validateToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                JWSObject jwsObject = JWSObject.parse(token);
                JWSHeader jwsHeader = jwsObject.getHeader();
                String keyId = jwsHeader.getKeyID();
                RSAKey rsaKey = publicKeysCache.get(keyId);

                if (rsaKey == null) {
                    fetchAndCacheJwks();
                    rsaKey = publicKeysCache.get(keyId);
                }
                if (rsaKey == null) {
                    throw new RuntimeException("Unable to find public key for key ID: " + keyId);
                }
                JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) rsaKey.toPublicKey());
                return jwsObject.verify(verifier);
            } catch (ParseException ex) {
                return false;
            }
        });
    }

    private void fetchAndCacheJwks() {
        webClient.get().retrieve()
                .bodyToMono(String.class)
                .map(jwksJson -> {
                    try {
                        JWKSet jwtSet = JWKSet.parse(jwksJson);
                        for (JWK jwk : jwtSet.getKeys()) {
                            if (jwk instanceof RSAKey rsaKey) {
                                publicKeysCache.put(jwk.getKeyID(), rsaKey);
                            }
                        }
                    } catch (ParseException ex) {
                        throw new RuntimeException("Failed to parse JWKS from Cognito", ex);
                    }
                    return true;
                }).block();
    }
}
