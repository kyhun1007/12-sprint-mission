package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.Instant;

public record UserStatusUpdateRequest(
    @NotNull(message = "마지막 접속 시간은 비어있을 수 없습니다.")
    @PastOrPresent(message = "마지막 접속 시간은 현재 이전이어야 합니다.")
    Instant newLastActiveAt
) {

}
