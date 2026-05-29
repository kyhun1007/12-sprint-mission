package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @NotBlank(message = "사용자 이름은 필수 입력 항목입니다.")
    @Size(min = 1, max = 50, message = "사용자 이름 은 {min}자 이상 {max}자 이하로 입력해주세요")
    String newUsername,

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(max = 100, message = "이메일은 {max}자를 초과할 수 없습니다.")
    String newEmail,

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Pattern(
        regexp = "^[A-Za-z0-9_!@#$%^&*()]{8,20}$",
        message = "패스워드는 영문/숫자/언더스코어+_!@#$%^&*() 조합 8~20자로 입력해 주시기 바랍니다."
    )
    String newPassword
) {

  @Override
  public String toString() {
    return "UserCreateRequest{" +
        "email='" + newEmail + '\'' +
        "username='" + newUsername + '\'' +
        ", password='[PROTECTED]'" +
        '}';
  }
}
