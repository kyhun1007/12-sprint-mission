package com.sprint.mission.discodeit.dto.userstatus;

import lombok.NonNull;

import java.util.UUID;

public record UserStatusUpdateRequest(
        UUID id,
        UUID userId
) {
}
