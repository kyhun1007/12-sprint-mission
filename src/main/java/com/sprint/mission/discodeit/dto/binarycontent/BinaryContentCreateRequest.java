package com.sprint.mission.discodeit.dto.binarycontent;

import lombok.NonNull;

public record BinaryContentCreateRequest(
        byte[] bytes
) {
}
