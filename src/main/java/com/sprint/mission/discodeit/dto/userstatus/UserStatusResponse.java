package com.sprint.mission.discodeit.dto.userstatus;

import java.time.Instant;
import java.util.UUID;

// Dto 대신 status 그냥 인자 몇개없어서 사용중인데 DTO로 통일하는게 좋을까요
public record UserStatusResponse(
        UUID id,
        UUID userId,
        Instant createdAt,
        Instant updatedAt
) {
}
