package com.sprint.mission.discodeit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.data.UserStatusDto;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private UserStatusService userStatusService;

  @Test
  @DisplayName("사용자 생성 성공")
  void createUser_success() throws Exception {
    UserCreateRequest request = new UserCreateRequest("testUser", "test@codeit.com", "password");
    MockMultipartFile profile = new MockMultipartFile("profile", "profile.jpg",
        MediaType.IMAGE_JPEG_VALUE,
        "test".getBytes());

    MockMultipartFile requestPart = new MockMultipartFile(
        "userCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    UUID userId = UUID.randomUUID();
    BinaryContentDto profileDto = new BinaryContentDto(
        UUID.randomUUID(),
        "profile.jpg",
        6L,
        MediaType.IMAGE_JPEG_VALUE
    );

    UserDto createdUser = new UserDto(
        userId,
        "testUser",
        "test@codeit.com",
        profileDto,
        false
    );

    given(userService.create(any(UserCreateRequest.class), any(Optional.class)))
        .willReturn(createdUser);

    mockMvc.perform(multipart("/api/users")
            .file(requestPart)
            .file(profile)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(userId.toString()))
        .andExpect(jsonPath("$.username").value("testUser"))
        .andExpect(jsonPath("$.email").value("test@codeit.com"))
        .andExpect(jsonPath("$.profile.fileName").value("profile.jpg"))
        .andExpect(jsonPath("$.online").value(false));
  }

  @Test
  @DisplayName("사용자 생성 실패 - 유효하지 않은 요청")
  void createUserByInvalidRequest_fail() throws Exception {
    UserCreateRequest request = new UserCreateRequest("te", "tes.com", "passwo");

    MockMultipartFile requestPart = new MockMultipartFile(
        "userCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    mockMvc.perform(multipart("/api/users")
            .file(requestPart)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("모든 사용자 조회 성공")
  void findAllUsers_success() throws Exception {
    UUID userId1 = UUID.randomUUID();
    UUID userId2 = UUID.randomUUID();
    UserDto user1 = new UserDto(
        userId1,
        "user1",
        "test1@codeit.com",
        null,
        true
    );

    UserDto user2 = new UserDto(
        userId2,
        "user2",
        "test2@codeit.com",
        null,
        false
    );

    given(userService.findAll()).willReturn(List.of(user1, user2));

    mockMvc.perform(get("/api/users")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.[0].id").value(userId1.toString()))
        .andExpect(jsonPath("$.[1].id").value(userId2.toString()))
        .andExpect(jsonPath("$.[0].username").value("user1"))
        .andExpect(jsonPath("$.[1].username").value("user2"))
        .andExpect(jsonPath("$.[0].online").value(true))
        .andExpect(jsonPath("$.[1].online").value(false));
  }

  @Test
  @DisplayName("사용자 업데이트 성공")
  void updateUser_success() throws Exception {
    UUID userId = UUID.randomUUID();

    UserUpdateRequest request = new UserUpdateRequest("updatedUser", "newEmail@codeit.com", null);

    MockMultipartFile requestPart = new MockMultipartFile(
        "userUpdateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    MockMultipartFile profilePart = new MockMultipartFile(
        "profile",
        "updated-profile.jpg",
        MediaType.IMAGE_JPEG_VALUE,
        "updated-test".getBytes()
    );

    BinaryContentDto updatedProfileDto = new BinaryContentDto(
        UUID.randomUUID(),
        "updated-profile.jpg",
        12L,
        MediaType.IMAGE_JPEG_VALUE
    );

    UserDto updatedUser = new UserDto(
        userId,
        "updatedUser",
        "test@codeit.com",
        updatedProfileDto,
        true
    );

    given(userService.update(eq(userId), any(UserUpdateRequest.class), any(Optional.class)))
        .willReturn(updatedUser);

    mockMvc.perform(multipart("/api/users/{id}", userId)
            .file(requestPart)
            .file(profilePart)
            .with(requestProcessor -> {
              requestProcessor.setMethod("PATCH");
              return requestProcessor;
            })
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId.toString()))
        .andExpect(jsonPath("$.username").value("updatedUser"))
        .andExpect(jsonPath("$.profile.fileName").value("updated-profile.jpg"));
  }

  @Test
  @DisplayName("사용자 업데이트 실패 - 유효하지 않은 요청 데이터 (글자수 초과 및 이메일 형식 오류)")
  void updateUser_invalidRequest_fail() throws Exception {
    UUID userId = UUID.randomUUID();

    String invalidUsername = "a".repeat(51);
    String invalidEmail = "not-an-email-format";

    UserUpdateRequest invalidRequest = new UserUpdateRequest(invalidUsername, invalidEmail, null);

    MockMultipartFile requestPart = new MockMultipartFile(
        "userUpdateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(invalidRequest)
    );

    mockMvc.perform(multipart("/api/users/{id}", userId)
            .file(requestPart)
            .with(requestProcessor -> {
              requestProcessor.setMethod("PATCH");
              return requestProcessor;
            })
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("사용자 업데이트 실패 - 비밀번호 길이 부족")
  void updateUser_invalidPassword_fail() throws Exception {
    UUID userId = UUID.randomUUID();

    UserUpdateRequest invalidRequest = new UserUpdateRequest(null, null, "short");

    MockMultipartFile requestPart = new MockMultipartFile(
        "userUpdateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(invalidRequest)
    );

    mockMvc.perform(multipart("/api/users/{id}", userId)
            .file(requestPart)
            .with(requestProcessor -> {
              requestProcessor.setMethod("PATCH");
              return requestProcessor;
            })
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("사용자 삭제 성공")
  void deleteUser_success() throws Exception {
    UUID userId = UUID.randomUUID();
    willDoNothing().given(userService).delete(userId);

    mockMvc.perform(delete("/api/users/{userId}", userId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("사용자 삭제 실패 - 존재하지 않는 사용자")
  void deleteUserWithNonExistId_fail() throws Exception {
    UUID userId = UUID.randomUUID();
    willThrow(UserNotFoundException.withId(userId)).given(userService).delete(userId);

    mockMvc.perform(delete("/api/users/{userId}", userId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("사용자 상태 업데이트 성공 테스트")
  void updateUserStatus_success() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID userStatusId = UUID.randomUUID();
    Instant lastActiveAt = Instant.now();

    UserStatusUpdateRequest updateRequest = new UserStatusUpdateRequest(lastActiveAt);
    UserStatusDto updatedStatus = new UserStatusDto(userStatusId, userId, lastActiveAt);

    given(userStatusService.updateByUserId(eq(userId), any(UserStatusUpdateRequest.class)))
        .willReturn(updatedStatus);

    mockMvc.perform(patch("/api/users/{userId}/userStatus", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(userId.toString()))
        .andExpect(jsonPath("$.id").value(userStatusId.toString()))
        .andExpect(content().json(objectMapper.writeValueAsString(updatedStatus)));
  }

  @Test
  @DisplayName("사용자 상태 업데이트 실패 - 존재하지 않는 사용자")
  void updateUserStatus_fail() throws Exception {
    UUID userId = UUID.randomUUID();
    Instant lastActiveAt = Instant.now();

    UserStatusUpdateRequest updateRequest = new UserStatusUpdateRequest(lastActiveAt);

    given(userStatusService.updateByUserId(eq(userId), any(UserStatusUpdateRequest.class)))
        .willThrow(UserNotFoundException.withId(userId));

    mockMvc.perform(patch("/api/users/{userId}/userStatus", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(updateRequest)))
        .andExpect(status().isNotFound());
  }


}
