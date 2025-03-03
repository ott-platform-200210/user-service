package org.engicodes.userservice.dao;

import reactor.core.publisher.Mono;

public interface UserDao {
    Mono<Boolean> checkIfEmailExists(String email);
    Mono<Boolean> checkIfUserNameExists(String username);
}
