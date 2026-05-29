package com.sprint.mission.discodeit.dto.request;

public record UserUpdateRequest(
    String newUsername,
    String newEmail,
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
