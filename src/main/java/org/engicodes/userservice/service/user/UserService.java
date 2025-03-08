package org.engicodes.userservice.service.user;

import org.engicodes.userservice.dto.SignupRequestDto;
import org.engicodes.userservice.model.User;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<User> saveUser(SignupRequestDto requestDto);
    Mono<Boolean> checkIfUserExistsInSignUp(SignupRequestDto signupRequestDto);
    Mono<User> getUserByUserEmail(String email);
}
