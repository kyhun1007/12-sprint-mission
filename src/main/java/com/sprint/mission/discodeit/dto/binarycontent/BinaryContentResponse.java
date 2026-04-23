package com.sprint.mission.discodeit.dto.binarycontent;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.Base64;

public record BinaryContentResponse(
        String id,
        String contentType,
        String bytes
) {
    public static BinaryContentResponse from(BinaryContent entity) {
        return new BinaryContentResponse(
                entity.getId().toString(),
                entity.getContentType(),
                Base64.getEncoder().encodeToString(entity.getBytes())
        );
    }
}