package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  //
  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;
  private final MessageMapper messageMapper;
  private final BinaryContentStorage binaryContentStorage;
  private final BinaryContentRepository binaryContentRepository;
  private final PageResponseMapper pageResponseMapper;

  @Transactional
  @Override
  public MessageDto create(MessageCreateRequest messageCreateRequest,
      List<BinaryContentCreateRequest> binaryContentCreateRequests) {
    UUID channelId = messageCreateRequest.channelId();
    UUID authorId = messageCreateRequest.authorId();

    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(
            () -> {
              log.warn("메시지 생성 실패: 존재하지 않는 채널 ID - ChannelId: {}", channelId);
              return new NoSuchElementException("Channel with id " + channelId + " does not exist");
            });
    User author = userRepository.findById(authorId)
        .orElseThrow(
            () -> {
              log.warn("메시지 생성 실패: 존재하지 않는 작성자 ID - AuthorId: {}", authorId);
              return new NoSuchElementException("Author with id " + authorId + " does not exist");
            }
        );

    log.debug("메시지 첨부파일 저장 시작 - 채널 ID: {}, 파일 개수: {}", channelId,
        binaryContentCreateRequests.size());

    List<BinaryContent> attachments = binaryContentCreateRequests.stream()
        .map(attachmentRequest -> {
          String fileName = attachmentRequest.fileName();
          String contentType = attachmentRequest.contentType();
          byte[] bytes = attachmentRequest.bytes();

          BinaryContent binaryContent = new BinaryContent(fileName, (long) bytes.length,
              contentType);
          binaryContentRepository.save(binaryContent);
          binaryContentStorage.put(binaryContent.getId(), bytes);
          return binaryContent;
        })
        .toList();

    String content = messageCreateRequest.content();
    Message message = new Message(
        content,
        channel,
        author,
        attachments
    );

    messageRepository.save(message);

    log.info("메시지 발행 완료 - MessageID: {}, 채널ID: {}, 작성자ID: {}, 첨부파일 수: {}",
        message.getId(), channelId, authorId, attachments.size());

    return messageMapper.toDto(message);
  }

  @Transactional(readOnly = true)
  @Override
  public MessageDto find(UUID messageId) {
    return messageRepository.findById(messageId)
        .map(messageMapper::toDto)
        .orElseThrow(
            () -> {
              log.warn("메시지 조회 실패: 존재하지 않는 메시지 ID - MessageId: {}", messageId);
              return new NoSuchElementException("Message with id " + messageId + " not found");
            });
  }

  @Transactional(readOnly = true)
  @Override
  public PageResponse<MessageDto> findAllByChannelId(UUID channelId, Instant createAt,
      Pageable pageable) {

    log.debug("채널 메시지 목록 조회 쿼리 실행 - 채널ID: {}, 기준 커서(createdAt): {}, 페이지 크기: {}",
        channelId, createAt != null ? createAt : "NOW", pageable.getPageSize());

    Slice<MessageDto> slice = messageRepository.findAllByChannelIdWithAuthor(channelId,
            Optional.ofNullable(createAt).orElse(Instant.now()),
            pageable)
        .map(messageMapper::toDto);

    Instant nextCursor = null;
    if (!slice.getContent().isEmpty()) {
      nextCursor = slice.getContent().get(slice.getContent().size() - 1)
          .createdAt();
    }

    log.debug("채널 메시지 목록 조회 완료 - 조회된 개수: {}, 다음 페이지 존재 여부: {}, 다음 커서: {}",
        slice.getContent().size(), slice.hasNext(), nextCursor);

    return pageResponseMapper.fromSlice(slice, nextCursor);
  }

  @Transactional
  @Override
  public MessageDto update(UUID messageId, MessageUpdateRequest request) {
    String newContent = request.newContent();
    Message message = messageRepository.findById(messageId)
        .orElseThrow(
            () -> {
              log.warn("메시지 수정 실패: 존재하지 않는 메시지 ID - MessageId: {}", messageId);
              return new NoSuchElementException("Message with id " + messageId + " not found");
            });
    message.update(newContent);
    log.info("메시지 수정 완료 - MessageId: {}, 채널ID: {}", message.getId(), message.getChannel().getId());
    return messageMapper.toDto(message);
  }

  @Transactional
  @Override
  public void delete(UUID messageId) {
    if (!messageRepository.existsById(messageId)) {
      log.warn("메시지 삭제 실패: 존재하지 않는 메시지 ID - MessageId: {}", messageId);
      throw new NoSuchElementException("Message with id " + messageId + " not found");
    }

    messageRepository.deleteById(messageId);
    log.info("메시지 삭제 완료 - MessageId: {}", messageId);
  }
}
