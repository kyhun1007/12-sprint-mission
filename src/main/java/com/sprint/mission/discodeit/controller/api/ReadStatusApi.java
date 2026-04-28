package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.readstatus.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@Tag(name = "ReadStatus", description = "메시지 읽음 상태 관리 API")
public interface ReadStatusApi {

  @Operation(summary = "User의 Message 읽음 상태 목록 조회")
  @GetMapping(params = "userId")
  ResponseEntity<List<ReadStatusDto>> findAllByUserId(@RequestParam UUID userId);

  @Operation(summary = "Message 읽음 상태 생성")
  @PostMapping
  ResponseEntity<ReadStatusDto> create(@RequestBody ReadStatusCreateRequest request);

  @Operation(summary = "Message 읽음 상태 업데이트")
  @PatchMapping("/{readStatusId}")
  ResponseEntity<ReadStatusDto> update(
      @PathVariable UUID readStatusId,
      @RequestBody ReadStatusUpdateRequest request
  );
}