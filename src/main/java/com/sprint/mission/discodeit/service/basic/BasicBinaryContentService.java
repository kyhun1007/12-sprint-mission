package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentMapper binaryContentMapper;
  private final BinaryContentStorage binaryContentStorage;

  @Transactional
  @Override
  public BinaryContentDto create(BinaryContentCreateRequest request) {
    String fileName = request.fileName();
    byte[] bytes = request.bytes();
    String contentType = request.contentType();

    log.debug("바이너리 콘텐츠 스토리지 업로드 시도 - 파일명: {}, 크기: {} bytes, 타입: {}",
        fileName, bytes != null ? bytes.length : 0, contentType);

    BinaryContent binaryContent = new BinaryContent(
        fileName,
        (long) bytes.length,
        contentType
    );
    binaryContentRepository.save(binaryContent);
    binaryContentStorage.put(binaryContent.getId(), bytes);

    log.info("바이너리 콘텐츠 생성 및 저장 완료 - ID: {}, 파일명: {}", binaryContent.getId(), fileName);

    return binaryContentMapper.toDto(binaryContent);
  }

  @Override
  public BinaryContentDto find(UUID binaryContentId) {
    return binaryContentRepository.findById(binaryContentId)
        .map(binaryContentMapper::toDto)
        .orElseThrow(() -> {
          log.warn("바이너리 콘텐츠 조회 실패: 존재하지 않는 ID - ID: {}", binaryContentId);
          return new NoSuchElementException(
              "BinaryContent with id " + binaryContentId + " not found");
        });
  }

  @Override
  public List<BinaryContentDto> findAllByIdIn(List<UUID> binaryContentIds) {
    if (binaryContentIds == null || binaryContentIds.isEmpty()) {
      log.debug("바이너리 콘텐츠 다건 조회 요청 - 요청 개수: 0 (인자가 null이거나 비어있음)");
      return new ArrayList<>();
    }

    log.debug("바이너리 콘텐츠 다건 조회 요청 - 요청 개수: {}", binaryContentIds.size());

    return binaryContentRepository.findAllById(binaryContentIds).stream()
        .map(binaryContentMapper::toDto)
        .toList();
  }

  @Transactional
  @Override
  public void delete(UUID binaryContentId) {
    if (!binaryContentRepository.existsById(binaryContentId)) {
      log.warn("바이너리 콘텐츠 삭제 실패: 존재하지 않는 ID - ID: {}", binaryContentId);
      throw new NoSuchElementException("BinaryContent with id " + binaryContentId + " not found");
    }
    binaryContentRepository.deleteById(binaryContentId);
    log.info("바이너리 콘텐츠 삭제 완료 - ID: {}", binaryContentId);
  }
}
