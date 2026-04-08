package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service("basicUserService")
@RequiredArgsConstructor
public class BasicUserService implements UserService {
    private final UserRepository userRepository;

//    public BasicUserService(@Qualifier("fileUserRepository") UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }

    @Override
    public User create(String username, String email, String password) {
        User user = new User(username, email, password);
        return userRepository.save(user);
    }

    @Override
    public User find(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User update(UUID userId, String newUsername, String newEmail, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));
        user.update(newUsername, newEmail, newPassword);
        return userRepository.save(user);
    }

    @Override
    public void delete(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("User with id " + userId + " not found");
        }
        userRepository.deleteById(userId);
    }

    public UserDto create(UserCreateRequest userCreateRequest) {
        User user = userCreateRequest.toUser();
        List<User> existingUsers = userRepository.findAll();

        for (User existingUser : existingUsers) {
            if (existingUser.getUsername().equals(user.getUsername())) {
                throw new IllegalArgumentException("User with Username " + user.getUsername() + " already exists");
            }

            if (existingUser.getEmail().equals(user.getEmail())) {
                throw new IllegalArgumentException("User with Email " + user.getEmail() + " already exists");
            }
        }

//        레포지 구성하고 완성할 때 추가할예정
//        UUID profileId = userCreateRequest.profileImageId();
//
//        if (profileId != null && BinaryContentRepository.isExists(profileId)) {
//
//        }

        UserStatus status = new UserStatus(user.getId());
//        userStatusRepository.save(status); // UserStatus 저장 -> UserStatusRepository 구현 후 추가할 예정

        return UserDto.from(userRepository.save(user));
    }
}
