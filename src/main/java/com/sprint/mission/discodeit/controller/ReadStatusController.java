package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusDto;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/readStatuses")
public class ReadStatusController {

  private final ReadStatusService readStatusService;

  // GET /api/readStatuses?userId=...
  @GetMapping(params = "userId")
  public ResponseEntity<List<ReadStatusDto>> findAllByUserId(@RequestParam UUID userId) {
    List<ReadStatusDto> statuses = readStatusService.findAllByUserId(userId);
    return ResponseEntity.ok(statuses);
  }

  // POST /api/readStatuses
  @PostMapping
  public ResponseEntity<ReadStatusDto> create(@RequestBody ReadStatusCreateRequest request) {
    ReadStatusDto created = readStatusService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  // PATCH /api/readStatuses
  @PatchMapping
  public ResponseEntity<ReadStatusDto> update(@RequestBody ReadStatusUpdateRequest request) {
    ReadStatusDto updated = readStatusService.update(request);
    return ResponseEntity.ok(updated);
  }

  // "/api/readStatuses/{readStatusId}": {
  //      "patch" 위에랑 똑같은데 호환용으로 위 update 일단 남겨둠

  @PatchMapping("/{readStatusId}")
  public ResponseEntity<ReadStatusDto> update(
      @PathVariable UUID readStatusId,
      @RequestBody ReadStatusUpdateRequest request
  ) {
    ReadStatusUpdateRequest serviceRequest = new ReadStatusUpdateRequest(
        readStatusId,
        request.userId(),
        request.channelId()
    );
    ReadStatusDto updated = readStatusService.update(serviceRequest);
    return ResponseEntity.ok(updated);
  }
}
