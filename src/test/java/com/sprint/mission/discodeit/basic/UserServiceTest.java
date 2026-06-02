package com.sprint.mission.discodeit.basic;

import com.sprint.mission.discodeit.dto.data.UserDto;
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
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @Mock
  private BinaryContentRepository binaryContentRepository;

  @Mock
  private BinaryContentStorage binaryContentStorage;

  @Mock
  private UserStatusRepository userStatusRepository;

  @InjectMocks
  private BasicUserService userService;

  private UUID userId;
  private String username;
  private String email;
  private String password;

  private User user;
  private UserDto expectedUserDto;

  @BeforeEach
  public void setup() {
    userId = UUID.randomUUID();
    username = "test1";
    email = "test1@discodeit.com";
    password = "password1234";

    user = new User(username, email, password, null);
    ReflectionTestUtils.setField(user, "id", userId);
    expectedUserDto = new UserDto(userId, username, email, null, true);
  }

  @Test
  @DisplayName("회원가입 성공 - 프로필 이미지 없음")
  void createUser_Success() {
    // given
    UserCreateRequest request = new UserCreateRequest(
        "test1", "test1@discodeit.com", "password1234");

    given(userRepository.existsByEmail(email)).willReturn(false);
    given(userRepository.existsByUsername(username)).willReturn(false);
    given(userMapper.toDto(any(User.class))).willReturn(expectedUserDto);

    // when
    UserDto result = userService.create(request, Optional.empty());

    // then
    assertThat(result).isEqualTo(expectedUserDto);

    verify(binaryContentRepository, never()).save(any(BinaryContent.class));
    verify(binaryContentStorage, never()).put(any(UUID.class), any(byte[].class));
    verify(userRepository, times(1)).save(any(User.class));
    verify(userStatusRepository, times(1)).save(any(UserStatus.class));
  }

  @Test
  @DisplayName("회원가입 실패 - 이미 존재하는 이메일로 생성")
  void create_WithExistingEmail_Fail() {
    // given
    UserCreateRequest request = new UserCreateRequest(
        "test1", "test1@discodeit.com", "password1234");

    given(userRepository.existsByEmail(email)).willReturn(true);

    // when
    // then
    assertThatThrownBy(() -> userService.create(request, Optional.empty()))
        .isInstanceOf(UserAlreadyExistsException.class);
  }

  @Test
  @DisplayName("회원 정보 수정 성공")
  void updateUser_Success() {
    String newUsername = "newUsername";
    String newEmail = "newEmail@codeit.com";
    String newPassword = "newPassword";
    // given
    UserUpdateRequest request = new UserUpdateRequest(newUsername, newEmail, newPassword);

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(userRepository.existsByEmail(newEmail)).willReturn(false);
    given(userRepository.existsByUsername(newUsername)).willReturn(false);
    given(userMapper.toDto(any(User.class))).willReturn(expectedUserDto);

    // when
    UserDto result = userService.update(userId, request, Optional.empty());

    // then
    assertThat(result).isEqualTo(expectedUserDto);
  }

  @Test
  @DisplayName("테스트 정보 수정 실패 - 중복된 유저이름")
  void update_UserWithExistingUsername_Fail() {
    String newUsername = "newUsername";
    String newEmail = "newEmail@codeit.com";
    String newPassword = "newPassword";
    // given
    UserUpdateRequest request = new UserUpdateRequest(newUsername, newEmail, newPassword);

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(userRepository.existsByEmail(newEmail)).willReturn(false);
    given(userRepository.existsByUsername(newUsername)).willReturn(true);

    // when
    // then
    assertThatThrownBy(() -> userService.update(userId, request, Optional.empty()))
        .isInstanceOf(UserAlreadyExistsException.class);
  }

  @Test
  @DisplayName("사용자 삭제 성공")
  void deleteUser_Success() {
    // given
    given(userRepository.existsById(userId)).willReturn(true);

    // when
    userService.delete(userId);

    // then
    verify(userRepository).deleteById(userId);
  }

  @Test
  @DisplayName("사용자 삭제 실패 - 존재하지 않는 사용자")
  void delete_UserWithExistingUsername_Fail() {
    // given
    given(userRepository.existsById(userId)).willReturn(false);

    // when
    // then
    assertThatThrownBy(() -> userService.delete(userId))
        .isInstanceOf(UserNotFoundException.class);
  }

}
