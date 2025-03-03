package org.engicodes.userservice.dao;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserDaoJpa implements UserDao {
    private final UserRepository userRepository;
    private final DSLContext dslContext;
    @Override
    public Mono<Boolean> checkIfEmailExists(String email) {
        return userRepository.existsUserByEmail(email);
    }

    @Override
    public Mono<Boolean> checkIfUserNameExists(String username) {
        return userRepository.existsUserByUserName(username);
    }
}