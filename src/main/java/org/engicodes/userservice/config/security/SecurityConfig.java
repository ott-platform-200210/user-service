package org.engicodes.userservice.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.engicodes.userservice.util.AppConstants.*;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // ✅ Disable CSRF for API requests
                .authorizeExchange(auth -> auth
                        .pathMatchers(AUTH_BASE_URL + "/**").permitAll() // ✅ Allow all auth-related endpoints
                        .anyExchange().authenticated() // 🔒 Secure all other endpoints
                )
                .build();
    }
}
