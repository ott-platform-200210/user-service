package org.engicodes.userservice.service.user;

import lombok.RequiredArgsConstructor;
import org.engicodes.userservice.dao.UserRepository;
import org.engicodes.userservice.dto.SignupRequestDto;
import org.engicodes.userservice.model.Roles;
import org.engicodes.userservice.model.SubscriptionStatus;
import org.engicodes.userservice.model.User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Mono<User> saveUser(SignupRequestDto requestDto) {
        User user = new User();
        user.setUserName(requestDto.userName());
        user.setEmail(requestDto.email());
        user.setFullName(requestDto.fullName());
        user.setRole(Roles.USER);
        user.setSubscriptionStatus(SubscriptionStatus.FREE);
        user.setEmailVerified(false);
        return userRepository.save(user);
    }
}