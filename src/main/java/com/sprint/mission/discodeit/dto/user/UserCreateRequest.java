package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.entity.User;
import lombok.NonNull;

import java.util.UUID;

public record UserCreateRequest(
        @NonNull String username,
        @NonNull String email,
        @NonNull String password,
        UUID profileImageId
) {
    public User toUser() {
        return new User(username, email, password, profileImageId);
    }
}
