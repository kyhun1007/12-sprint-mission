package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Getter
public class UserStatus {
    private UUID id;
    private UUID userId;

    private Instant createdAt;
    private Instant updatedAt;

    public boolean isUserOnline() {
        if (this.updatedAt == null) {
            return false;
        }

        Instant threshold = Instant.now().minus(5, ChronoUnit.MINUTES);
        return updatedAt.isAfter(threshold);
    }
}
