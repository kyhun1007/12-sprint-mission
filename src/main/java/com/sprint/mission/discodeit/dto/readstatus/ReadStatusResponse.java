package com.sprint.mission.discodeit.dto.readstatus;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.time.Instant;
import java.util.UUID;

public record ReadStatusResponse(
        UUID id,
        UUID userId,
        UUID channelId,
        Instant createdAt,
        Instant updatedAt
) {
    public static ReadStatusResponse from(ReadStatus readStatus) {
        return new ReadStatusResponse(
                readStatus.getId(),
                readStatus.getUserId(),
                readStatus.getChannelId(),
                readStatus.getCreatedAt(),
                readStatus.getUpdatedAt()
        );
    }
}
