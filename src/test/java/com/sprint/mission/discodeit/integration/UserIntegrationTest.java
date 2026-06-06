package com.sprint.mission.discodeit.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserService userService;

  @Test
  @DisplayName("유저 생성 통합 테스트 성공")
  void createUser_success() throws Exception {
    UserCreateRequest request = new UserCreateRequest(
        "testUser",
        "test@codeit.com",
        "password"
    );

    MockMultipartFile userCreateRequest = new MockMultipartFile(
        "userCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    MockMultipartFile profile = new MockMultipartFile(
        "profile",
        "profile.jpg",
        MediaType.IMAGE_JPEG_VALUE,
        "test".getBytes()
    );

    mockMvc.perform(multipart("/api/users")
            .file(userCreateRequest)
            .file(profile)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.username", is("testUser")))
        .andExpect(jsonPath("$.email", is("test@codeit.com")))
        .andExpect(jsonPath("$.profile.fileName", is("profile.jpg")))
        .andExpect(jsonPath("$.online", is(true)));
  }

  @Test
  @DisplayName("유저 생성 통합 테스트 실패 - 유효하지 않은 요청")
  void createUserWithNonValidParameter_fail() throws Exception {
    UserCreateRequest request = new UserCreateRequest(
        "t",
        "tescom",
        "passwor"
    );

    MockMultipartFile userCreateRequest = new MockMultipartFile(
        "userCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    MockMultipartFile profile = new MockMultipartFile(
        "profile",
        "profile.jpg",
        MediaType.IMAGE_JPEG_VALUE,
        "test".getBytes()
    );

    mockMvc.perform(multipart("/api/users")
            .file(userCreateRequest)
            .file(profile)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("유저 업데이트 통합 테스트 성공")
  void updateUser_success() throws Exception {
    UserCreateRequest createRequest = new UserCreateRequest(
        "testUser",
        "test@codeit.com",
        "password"
    );

    UserDto createdUser = userService.create(createRequest, Optional.empty());
    UUID userId = createdUser.id();

    UserUpdateRequest request = new UserUpdateRequest(
        "newUsername",
        "newTest@codeit.com",
        "newPassword"
    );

    MockMultipartFile userUpdateRequest = new MockMultipartFile(
        "userUpdateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    MockMultipartFile newProfile = new MockMultipartFile(
        "profile",
        "new-profile.jpg",
        MediaType.IMAGE_JPEG_VALUE,
        "newProfile".getBytes()
    );

    mockMvc.perform(multipart("/api/users/{userId}", userId)
            .file(userUpdateRequest)
            .file(newProfile)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .with(updateRequest -> {
              updateRequest.setMethod("PATCH");
              return updateRequest;
            }))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(userId.toString())))
        .andExpect(jsonPath("$.username", is("newUsername")))
        .andExpect(jsonPath("$.email", is("newTest@codeit.com")))
        .andExpect(jsonPath("$.profile.fileName", is("new-profile.jpg")));

  }


}
