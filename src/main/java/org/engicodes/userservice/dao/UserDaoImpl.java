package org.engicodes.userservice.dao;

import lombok.RequiredArgsConstructor;
import org.engicodes.userservice.model.User;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserDaoImpl implements UserDao {
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

    @Override
    public Mono<User> createNewUser(User user) {
        return userRepository.save(user);
    }
}