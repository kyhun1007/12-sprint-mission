package com.sprint.mission.discodeit.dto.readstatus;

import lombok.NonNull;

import java.util.UUID;

public record ReadStatusCreateRequest(
        @NonNull UUID userId,
        @NonNull UUID channelId
) {
}
