package com.sprint.mission.discodeit.dto.message;

import lombok.NonNull;

import java.util.List;
import java.util.UUID;

public record MessageCreateRequest(
        @NonNull UUID channelId,
        @NonNull UUID authorId,
        @NonNull String content,
        List<UUID> attachmentIds
) {
        public MessageCreateRequest {
                if (attachmentIds == null) {
                        attachmentIds = List.of();
                }
//                if (content.isBlank()) {
//                        throw new IllegalArgumentException("Content cannot be empty or blank");
//                }
        }
}
