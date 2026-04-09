package com.sprint.mission.discodeit.dto.readstatus;

import lombok.NonNull;

import java.util.UUID;

public record ReadStatusUpdateRequest(
        @NonNull
        UUID id,
        UUID userId,
        UUID channelId
) {
}
