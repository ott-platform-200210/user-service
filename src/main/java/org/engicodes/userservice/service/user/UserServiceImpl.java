package org.engicodes.userservice.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.engicodes.userservice.dao.UserDao;
import org.engicodes.userservice.dto.SignupRequestDto;
import org.engicodes.userservice.model.Roles;
import org.engicodes.userservice.model.SubscriptionStatus;
import org.engicodes.userservice.model.User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserDao userDao;

    @Override
    public Mono<User> saveUser(SignupRequestDto requestDto) {
        User user = new User();
        user.setUserName(requestDto.userName());
        user.setEmail(requestDto.email());
        user.setFullName(requestDto.fullName());
        user.setRole(Roles.USER);
        user.setSubscriptionStatus(SubscriptionStatus.FREE);
        user.setEmailVerified(false);
        return userDao.createNewUser(user);
    }

    @Override
    public Mono<Boolean> checkIfUserExistsInSignUp(SignupRequestDto signupRequestDto) {
        return userDao.checkIfEmailExists(signupRequestDto.email())
                .filter(emailExists -> emailExists)
                .switchIfEmpty(userDao.checkIfUserNameExists(signupRequestDto.userName()))
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<User> getUserByUserEmail(String email) {
        return userDao.getUserDetailsByEmail(email);
    }
}