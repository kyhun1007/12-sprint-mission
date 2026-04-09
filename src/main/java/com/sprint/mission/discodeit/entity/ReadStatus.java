package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class ReadStatus implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private UUID userId;
    private UUID channelId;

    private Instant createdAt;
    private Instant updatedAt;

    public ReadStatus(UUID userId, UUID channelId) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.channelId = channelId;

        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void update(UUID userId, UUID channelId) {
        boolean anyValueUpdated = false;

        if (userId != null && !userId.equals(this.userId)) {
            this.userId = userId;
            anyValueUpdated = true;
        }

        if (channelId != null && !channelId.equals(this.channelId)) {
            this.channelId = channelId;
            anyValueUpdated = true;
        }

        if (!anyValueUpdated) {
            this.updatedAt = Instant.now();
        }
    }
}
