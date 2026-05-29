package com.sprint.mission.discodeit.dto.request;

public record UserCreateRequest(
    String username,
    String email,
    String password
) {

  @Override
  public String toString() {
    return "UserCreateRequest{" +
        "email='" + email + '\'' +
        "username='" + username + '\'' +
        ", password='[PROTECTED]'" +
        '}';
  }
}
