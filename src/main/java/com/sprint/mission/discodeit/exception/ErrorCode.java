package com.sprint.mission.discodeit.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
  USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
  DUPLICATE_USER("이미 존재하는 사용자입니다."),
  CHANNEL_NOT_FOUND("채널이 존재하지 않습니다."),
  PRIVATE_CHANNEL_UPDATE("비공개 채널은 업데이트 할 수 없습니다."),

  // Server 에러 코드
  INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다."),
  INVALID_REQUEST("잘못된 요청입니다.");

  private final String message;
}
