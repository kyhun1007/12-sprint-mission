package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.entity.User;

import java.util.UUID;

public record UserCreateRequest(
        String username,
        String email,
        String password,
        UUID profileImageId
) {
    public User toUser() {
        return new User(username, email, password, profileImageId);
    }
}
