package com.sprint.mission.discodeit.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    log.error("기본 오류 발생 : {}", e.getMessage());
    ErrorResponse errorResponse = new ErrorResponse(e, 500);
    return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
  }

  @ExceptionHandler(DiscodeitException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(DiscodeitException e) {
    log.error("커스텀 예외 발생 : code={}, message={}, detail={}", e.getErrorCode(), e.getMessage(),
        e.getDetails());
    HttpStatus httpStatus = parseHttpStatus(e);
    ErrorResponse errorResponse = new ErrorResponse(e, httpStatus.value());
    return ResponseEntity.status(httpStatus).body(errorResponse);
  }

  private HttpStatus parseHttpStatus(DiscodeitException e) {
    ErrorCode code = e.getErrorCode();
    return switch (code) {
      case USER_NOT_FOUND, CHANNEL_NOT_FOUND -> HttpStatus.NOT_FOUND;
      case DUPLICATE_USER -> HttpStatus.CONFLICT;
      case PRIVATE_CHANNEL_UPDATE -> HttpStatus.BAD_REQUEST;
      default -> HttpStatus.INTERNAL_SERVER_ERROR;
    };
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException e) {
    log.error("요청 유효성 검사 실패 : {}", e.getMessage());

    Map<String, Object> validationErrors = new LinkedHashMap<>();
    e.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMeString = error.getDefaultMessage();
      validationErrors.put(fieldName, errorMeString);
    });

    ErrorResponse response = new ErrorResponse(
        Instant.now(),
        "VALIDATION_ERROR",
        "요청 데이터 유효성 검사에 실패하였습니다.",
        validationErrors,
        e.getClass().getSimpleName(),
        HttpStatus.BAD_REQUEST.value()
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }
}
