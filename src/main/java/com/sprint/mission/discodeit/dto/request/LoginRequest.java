package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "사용자 이름은 필수 입력 항목입니다.")
    @Size(min = 1, max = 50, message = "사용자 이름 은 {min}자 이상 {max}자 이하로 입력해주세요")
    String username,

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Pattern(
        regexp = "^[A-Za-z0-9_!@#$%^&*()]{8,20}$",
        message = "패스워드는 영문/숫자/언더스코어+_!@#$%^&*() 조합 8~20자로 입력해 주시기 바랍니다."
    )
    @Size(min = 4, max = 60, message = "비밀번호는 {min}자 이상, {max}자 이하로 입력해주세요.")
    String password
) {

  @Override
  public String toString() {
    return "LoginRequest{" +
        "username='" + username + '\'' +
        ", password='[PROTECTED]'" +
        '}';
  }
}
