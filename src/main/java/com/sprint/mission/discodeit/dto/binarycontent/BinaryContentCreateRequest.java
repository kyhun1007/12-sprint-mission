package com.sprint.mission.discodeit.dto.binarycontent;

import lombok.NonNull;

public record BinaryContentCreateRequest(
        String fileName,
        String contentType,
        Long fileSize,
        byte[] bytes
) {
}
