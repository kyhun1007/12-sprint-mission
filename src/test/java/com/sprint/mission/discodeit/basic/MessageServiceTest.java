package com.sprint.mission.discodeit.basic;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import com.sprint.mission.discodeit.storage.local.LocalBinaryContentStorage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

  @Mock
  private MessageRepository messageRepository;

  @Mock
  private ChannelRepository channelRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private MessageMapper messageMapper;

  @Mock
  private LocalBinaryContentStorage binaryContentStorage;

  @Mock
  private BinaryContentRepository binaryContentRepository;

  @Mock
  private PageResponseMapper pageResponseMapper;

  @InjectMocks
  private BasicMessageService messageService;

  private UUID messageId;
  private UUID channelId;
  private UUID authorId;
  private String content;
  private Message message;
  private MessageDto messageDto;
  private Channel channel;
  private User author;
  private BinaryContent binaryContent;
  private BinaryContentDto attachmentDto;

  @BeforeEach
  public void setup() {
    messageId = UUID.randomUUID();
    channelId = UUID.randomUUID();
    authorId = UUID.randomUUID();
    content = "안녕하세요, 반갑습니다!";

    channel = new Channel(ChannelType.PUBLIC, "testChannel", "testDescription");
    ReflectionTestUtils.setField(channel, "id", channelId);

    author = new User("testUser", "test@codeit.com", "pass1234", null);
    ReflectionTestUtils.setField(author, "id", authorId);

    binaryContent = new BinaryContent("test.png", 100L, "image/png");
    ReflectionTestUtils.setField(binaryContent, "id", UUID.randomUUID());
    attachmentDto = new BinaryContentDto(binaryContent.getId(), "test.png", 100L, "image/png");

    message = new Message(content, channel, author, List.of(binaryContent));
    ReflectionTestUtils.setField(message, "id", messageId);

    messageDto = new MessageDto(
        messageId,
        message.getCreatedAt(),
        message.getUpdatedAt(),
        content,
        channelId,
        new UserDto(authorId, "testUser", "test@codeit.com", null, true),
        List.of(attachmentDto)
    );
  }

  @Test
  @DisplayName("메세지 생성 성공")
  public void createMessage_success() {
    // given
    MessageCreateRequest request = new MessageCreateRequest(content, channelId, authorId);
    BinaryContentCreateRequest binaryRequest = new BinaryContentCreateRequest("test.png",
        "image/png", "hi".getBytes());
    List<BinaryContentCreateRequest> binaryRequests = List.of(binaryRequest);

    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
    given(userRepository.findById(authorId)).willReturn(Optional.of(author));
    given(binaryContentRepository.save(any(BinaryContent.class))).will(invocation -> {
      BinaryContent binaryContent = invocation.getArgument(0);
      ReflectionTestUtils.setField(binaryContent, "id", this.binaryContent.getId());
      return binaryContent;
    });
    given(messageMapper.toDto(any(Message.class))).willReturn(messageDto);

    // when
    MessageDto result = messageService.create(request, binaryRequests);

    // then
    assertThat(result).isEqualTo(messageDto);
    verify(binaryContentRepository).save(any(BinaryContent.class));
    verify(binaryContentStorage).put(eq(binaryContent.getId()), any(byte[].class));
    verify(messageRepository).save(any(Message.class));
  }

  @Test
  @DisplayName("메세지 생성 실패 - 존재하지 않는 작성자")
  public void createMessageWithNonExistAuthor_Fail() {
    // given
    MessageCreateRequest request = new MessageCreateRequest(content, channelId, authorId);

    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
    given(userRepository.findById(authorId)).willReturn(Optional.empty());

    // when
    // then
    assertThatThrownBy(() -> messageService.create(request, List.of()))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  @DisplayName("메세지 업데이트 성공")
  public void updateMessage_success() {
    // given
    MessageUpdateRequest request = new MessageUpdateRequest("newContent");
    given(messageRepository.findById(messageId)).willReturn(Optional.of(message));
    given(messageMapper.toDto(any(Message.class))).willReturn(messageDto);

    // when
    MessageDto result = messageService.update(messageId, request);

    // then
    assertThat(result).isEqualTo(messageDto);
  }

  @Test
  @DisplayName("메세지 업데이트 실패 - 존재하지 않는 메세지")
  public void update_WithNonExistId_fail() {
    // given
    MessageUpdateRequest request = new MessageUpdateRequest("newContent");
    given(messageRepository.findById(messageId)).willReturn(Optional.empty());

    // when
    // then
    assertThatThrownBy(() -> messageService.update(messageId, request))
        .isInstanceOf(NoSuchElementException.class);
  }

  @Test
  @DisplayName("메세지 삭제 성공")
  public void deleteMessage_success() {
    // given
    given(messageRepository.existsById(messageId)).willReturn(true);

    // when
    messageService.delete(messageId);

    // then
    verify(messageRepository).deleteById(messageId);
  }

  @Test
  @DisplayName("메세지 삭제 실패 - 존재하지 않는 메세지")
  public void delete_WithNonExistMessageId_fail() {
    // given
    given(messageRepository.existsById(messageId)).willReturn(false);

    // when
    // then
    assertThatThrownBy(() -> messageService.delete(messageId))
        .isInstanceOf(NoSuchElementException.class);
  }

  @Test
  @DisplayName("채널 이름으로 메세지 찾기 성공")
  public void findWithChannelId_success() {
    // given
    int pageSize = 2;
    Instant createdAt = Instant.now();
    Pageable pageable = PageRequest.of(0, pageSize);

    Message message1 = new Message(content + 1, channel, author, List.of(binaryContent));
    Message message2 = new Message(content + 2, channel, author, List.of(binaryContent));
    Message message3 = new Message(content + 3, channel, author, List.of(binaryContent));

    ReflectionTestUtils.setField(message1, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(message2, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(message3, "id", UUID.randomUUID());

    Instant createdAt1 = Instant.now().minusSeconds(10);
    Instant createdAt2 = Instant.now().minusSeconds(20);
    Instant createdAt3 = Instant.now().minusSeconds(30);

    ReflectionTestUtils.setField(message1, "createdAt", createdAt1);
    ReflectionTestUtils.setField(message2, "createdAt", createdAt2);
    ReflectionTestUtils.setField(message3, "createdAt", createdAt3);

    MessageDto dto1 = new MessageDto(
        message1.getId(),
        createdAt1,
        createdAt1,
        content + 1,
        channelId,
        new UserDto(authorId, "testUser", "test@codeit.com", null, null),
        List.of(attachmentDto)
    );

    MessageDto dto2 = new MessageDto(
        message2.getId(),
        createdAt2,
        createdAt2,
        content + 2,
        channelId,
        new UserDto(authorId, "testUser", "test@codeit.com", null, null),
        List.of(attachmentDto)
    );

    List<Message> firstPageMessages = List.of(message1, message2);
    List<MessageDto> firstPageDtos = List.of(dto1, dto2);

    SliceImpl<Message> firstPageSlice = new SliceImpl<>(firstPageMessages, pageable, true);
    PageResponse<MessageDto> firstPageResponse = new PageResponse<>(
        firstPageDtos,
        createdAt2,
        pageSize,
        true,
        null
    );

    given(messageRepository.findAllByChannelIdWithAuthor(eq(channelId), eq(createdAt),
        eq(pageable))).willReturn(firstPageSlice);
    given(messageMapper.toDto(eq(message1))).willReturn(dto1);
    given(messageMapper.toDto(eq(message2))).willReturn(dto2);
    given(pageResponseMapper.<MessageDto>fromSlice(any(), eq(createdAt2))).willReturn(
        firstPageResponse);

    PageResponse<MessageDto> result = messageService.findAllByChannelId(channelId, createdAt,
        pageable);

    assertThat(result).isEqualTo(firstPageResponse);
    assertThat(result.content()).hasSize(pageSize);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.nextCursor()).isEqualTo(createdAt2);
  }

}
