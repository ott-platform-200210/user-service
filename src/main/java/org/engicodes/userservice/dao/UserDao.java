package org.engicodes.userservice.dao;

import org.engicodes.userservice.model.User;
import reactor.core.publisher.Mono;

public interface UserDao {
    Mono<Boolean> checkIfEmailExists(String email);

    Mono<Boolean> checkIfUserNameExists(String username);

    Mono<User> createNewUser(User user);
    Mono<User> getUserDetailsByEmail(String email);
}
