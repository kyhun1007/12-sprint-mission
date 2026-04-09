package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
//    User create(String username, String email, String password);
    User create(UserCreateRequest request);
    UserResponse find(UUID userId);
    List<UserResponse> findAll();
    UserResponse update(UserUpdateRequest request);
    void delete(UUID userId);
    // 이거로 전부 수정 예정
}
