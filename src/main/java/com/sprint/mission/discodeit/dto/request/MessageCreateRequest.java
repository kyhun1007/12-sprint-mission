package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record MessageCreateRequest(
    @NotBlank(message = "메시지 내용은 비어있을 수 없습니다.")
    @Size(max = 2000, message = "메시지는 최대 {max}자까지 입력할 수 있습니다.")
    String content,

    @NotNull(message = "채널 ID는 필수 항목입니다.")
    UUID channelId,

    @NotNull(message = "작성자 ID는 필수 항목입니다.")
    UUID authorId
) {

}
