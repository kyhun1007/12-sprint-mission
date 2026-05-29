package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final UserStatusRepository userStatusRepository;
  private final UserMapper userMapper;
  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentStorage binaryContentStorage;

  @Transactional
  @Override
  public UserDto create(UserCreateRequest userCreateRequest,
      Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
    String username = userCreateRequest.username();
    String email = userCreateRequest.email();

    if (userRepository.existsByEmail(email)) {
      log.warn("회원가입 실패: 중복된 이메일 - Email: {}", email);
      throw new IllegalArgumentException("User with email " + email + " already exists");
    }
    if (userRepository.existsByUsername(username)) {
      log.warn("회원가입 실패: 중복된 사용자 이름 - Username: {}", username);
      throw new IllegalArgumentException("User with username " + username + " already exists");
    }

    BinaryContent nullableProfile = optionalProfileCreateRequest
        .map(profileRequest -> {
          String fileName = profileRequest.fileName();
          String contentType = profileRequest.contentType();
          byte[] bytes = profileRequest.bytes();

          log.debug("회원가입 프로필 이미지 업로드 진행 - 파일명: {}, 크기: {} bytes", fileName, bytes.length);

          BinaryContent binaryContent = new BinaryContent(fileName, (long) bytes.length,
              contentType);
          binaryContentRepository.save(binaryContent);
          binaryContentStorage.put(binaryContent.getId(), bytes);
          return binaryContent;
        })
        .orElse(null);
    String password = userCreateRequest.password();

    User user = new User(username, email, password, nullableProfile);
    Instant now = Instant.now();
    UserStatus userStatus = new UserStatus(user, now);

    userRepository.save(user);

    log.info("신규 회원가입 완료 - ID: {}, Username: {}, Email: {}, 프로필 첨부 여부: {}",
        user.getId(), username, email, (nullableProfile != null));

    return userMapper.toDto(user);
  }

  @Override
  public UserDto find(UUID userId) {
    return userRepository.findById(userId)
        .map(userMapper::toDto)
        .orElseThrow(() -> {
          log.warn("사용자 조회 실패: 존재하지 않는 ID - ID: {}", userId);
          return new NoSuchElementException("User with id " + userId + " not found");
        });
  }

  @Override
  public List<UserDto> findAll() {
    List<User> users = userRepository.findAllWithProfileAndStatus();
    log.debug("전체 사용자 목록 조회 완료 - 조회된 회원 수: {}명", users.size());

    return users.stream()
        .map(userMapper::toDto)
        .toList();
  }

  @Transactional
  @Override
  public UserDto update(UUID userId, UserUpdateRequest userUpdateRequest,
      Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
    User user = userRepository.findById(userId)
        .orElseThrow(() ->
            UserNotFoundException.withId(userId)
        );

    String newUsername = userUpdateRequest.newUsername();
    String newEmail = userUpdateRequest.newEmail();
    if (userRepository.existsByEmail(newEmail)) {
      throw UserAlreadyExistsException.withEmail(newEmail);
    }
    if (userRepository.existsByUsername(newUsername)) {
      throw UserAlreadyExistsException.withUsername(newUsername);
    }

    BinaryContent nullableProfile = optionalProfileCreateRequest
        .map(profileRequest -> {

          String fileName = profileRequest.fileName();
          String contentType = profileRequest.contentType();
          byte[] bytes = profileRequest.bytes();

          log.debug("사용자 프로필 이미지 변경 진행 - UserID: {}, 파일명: {}", userId, fileName);

          BinaryContent binaryContent = new BinaryContent(fileName, (long) bytes.length,
              contentType);
          binaryContentRepository.save(binaryContent);
          binaryContentStorage.put(binaryContent.getId(), bytes);
          return binaryContent;
        })
        .orElse(null);

    String newPassword = userUpdateRequest.newPassword();
    user.update(newUsername, newEmail, newPassword, nullableProfile);

    log.info("사용자 정보 수정 완료 - ID: {}, 변경된 Username: {}", userId, newUsername);
    return userMapper.toDto(user);
  }

  @Transactional
  @Override
  public void delete(UUID userId) {
    if (userRepository.existsById(userId)) {
      throw UserNotFoundException.withId(userId);
    }

    userRepository.deleteById(userId);
    log.info("사용자 회원 탈퇴 처리 완료 - 삭제된 User ID: {}", userId);
  }
}
