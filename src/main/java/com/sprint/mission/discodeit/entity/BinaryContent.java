package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class BinaryContent {
    private UUID id;
    private UUID messageId;
    private UUID userId;

    private Instant createdAt;
}
