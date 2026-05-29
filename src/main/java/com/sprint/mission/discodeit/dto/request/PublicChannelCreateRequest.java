package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PublicChannelCreateRequest(
    @NotBlank(message = "채널 이름은 필수 입력 항목입니다.")
    @Size(min = 2, max = 100, message = "채널 이름은 {min}자 이상, {max}자 이하로 입력해주세요.")
    String name,

    @Size(max = 500, message = "채널 설명은 {max}자를 초과할 수 없습니다.")
    String description
) {

}
