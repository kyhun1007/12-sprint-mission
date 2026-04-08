package com.sprint.mission.discodeit.dto.data;

import com.sprint.mission.discodeit.entity.User;

import java.time.Instant;
import java.util.UUID;

public record UserDto(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        String username,
        String email,
        String password
) {
    // User랑 DTO 호환용 from 메소드
    public static UserDto from(User user){
        return new UserDto(user.getId(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword());
    }
}
