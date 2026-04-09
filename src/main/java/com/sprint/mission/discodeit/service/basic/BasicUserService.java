package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("basicUserService")
@RequiredArgsConstructor
public class BasicUserService implements UserService {
    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserStatusRepository userStatusRepository;

//    public BasicUserService(@Qualifier("fileUserRepository") UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }

//    @Override
//    public User create(String username, String email, String password) {
//        User user = new User(username, email, password);
//        return userRepository.save(user);
//    }

    public User create(UserCreateRequest request) {
        List<User> existingUsers = userRepository.findAll();

        for (User existingUser : existingUsers) {
            if (existingUser.getUsername().equals(request.username())) {
                throw new IllegalArgumentException("User with Username " + request.username() + " already exists");
            }

            if (existingUser.getEmail().equals(request.email())) {
                throw new IllegalArgumentException("User with Email " + request.email() + " already exists");
            }
        }

        User user = request.toUser();

        UserStatus status = new UserStatus(user.getId());
        userStatusRepository.save(status);

        return userRepository.save(user);
    }

    @Override
    public UserResponse find(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found (UserService-find)"));

        UserStatus status = userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NoSuchElementException("UserStatus for user id " + userId + " not found (UserService-find)"));

        return  UserResponse.from(user, status);
    }

    @Override
    public List<UserResponse> findAll() {
        List<User> users = userRepository.findAll();
        List<UserStatus> statuses = userStatusRepository.findAll();

        Map<UUID, UserStatus> statusMap = statuses.stream()
                .collect(Collectors.toMap(UserStatus::getUserId, status -> status));

        return users.stream()
                .map(user -> {
                    UserStatus status = statusMap.getOrDefault(user.getId(), new UserStatus());
                    return UserResponse.from(user, status);
                })
                .toList();
    }

    @Override
    public UserResponse update(UserUpdateRequest request) {
        User user = userRepository.findById(request.id())
                .orElseThrow(() -> new NoSuchElementException("User with id " + request.id() + " not found (UserService-update)"));

        if (request.profileImageId() != null && !request.profileImageId().equals(user.getProfileImageId())) {
            if (user.getProfileImageId() != null) {
                binaryContentRepository.delete(user.getProfileImageId());
            }
        }

        user.update(
                request.username(),
                request.email(),
                request.password(),
                request.profileImageId()
        );

        userRepository.save(user);

        UserStatus status = userStatusRepository.findByUserId(request.id())
                .orElse(new UserStatus());

        return UserResponse.from(user, status);
    }

    @Override
    public void delete(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found (UserService-delete)"));

        userStatusRepository.deleteByUserId(userId);

        UUID profileImageId = user.getProfileImageId();
        if (profileImageId != null) {
            binaryContentRepository.delete(profileImageId);
        }

        userRepository.deleteById(userId);
    }
}
