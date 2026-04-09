package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.AuthRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("basicAuthService")
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {
    private final UserRepository userRepository;

    public User login(AuthRequest authRequest) {
        if (authRequest.username() == null  || authRequest.password() == null) {
            throw new IllegalArgumentException("Username and password must not be null (login)");
        }

        return userRepository.findAll().stream()
                .filter(u -> u.getUsername().equals(authRequest.username()))
                .filter(u -> u.getPassword().equals(authRequest.password()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
    }
}
