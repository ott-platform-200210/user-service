package org.engicodes.userservice.dao;

import jakarta.validation.constraints.Email;
import org.engicodes.userservice.model.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public interface UserRepository extends ReactiveCrudRepository<User,Long> {
    Mono<Boolean> existsUserByEmail(String email);
    Mono<Boolean> existsUserByUserName(String username);
    Mono<User> getUsersByEmail(@Email(message = "email is not correct format!") String email);
}
