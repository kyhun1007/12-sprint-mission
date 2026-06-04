package com.sprint.mission.discodeit.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class ChannelServiceTest {

  @Mock
  private ChannelRepository channelRepository;

  @Mock
  private ReadStatusRepository readStatusRepository;

  @Mock
  private MessageRepository messageRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ChannelMapper channelMapper;

  @InjectMocks
  private BasicChannelService channelService;

  private UUID channelId;
  private UUID userId;
  private String channelName;
  private String description;
  private Channel channel;
  private ChannelDto channelDto;
  private User user;

  @BeforeEach
  public void setup() {
    channelId = UUID.randomUUID();
    userId = UUID.randomUUID();
    channelName = "testChannel";
    description = "testDescription";

    channel = new Channel(ChannelType.PUBLIC, channelName, description);
    ReflectionTestUtils.setField(channel, "id", channelId);
    ChannelDto channelDto = new ChannelDto(channelId, ChannelType.PUBLIC, channelName, description,
        List.of(),
        Instant.now());
    user = new User("testUser", "test@codeit.com", "test1234", null);
//    ReflectionTestUtils.setField(user, "id", userId);
  }

  @Test
  @DisplayName("공개 채널 생성 성공")
  public void createPublicChannel_success() {
    // given
    PublicChannelCreateRequest request = new PublicChannelCreateRequest(
        channelName, description
    );
    given(channelRepository.save(any())).willReturn(channelDto);

    // when
    ChannelDto result = channelService.create(request);

    // then
    assertThat(result).isEqualTo(channelDto);
  }

  @Test
  @DisplayName("비공개 채널 생성 성공")
  public void createPrivateChannel_success() {
    // given
    List<UUID> participantIds = List.of(userId);
    PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(participantIds);
    given(userRepository.findAllById(participantIds)).willReturn(List.of(user));
    given(channelMapper.toDto(any())).willReturn(channelDto);

    // when
    ChannelDto result = channelService.create(request);

    // then
    assertThat(result).isEqualTo(channelDto);
    verify(channelRepository).save(any(Channel.class));
    verify(readStatusRepository).saveAll(anyList());
  }

  @Test
  @DisplayName("채널 업데이트 성공")
  public void updateChannel_success() {
    // given
    String newName = "newName";
    String newDescription = "newDescription";
    PublicChannelUpdateRequest request = new PublicChannelUpdateRequest(
        newName, newDescription
    );

    given(channelRepository.findById(any())).willReturn(Optional.of(channel));
    given(channelMapper.toDto(any())).willReturn(channelDto);

    // when
    ChannelDto result = channelService.update(channelId, request);

    // then
    assertThat(result).isEqualTo(channelDto);
  }

  @Test
  @DisplayName("채널 업데이트 실패 - 존재하지 않는 채널id")
  public void updateWithNonExistId_fail() {
    // given
    String newName = "newName";
    String newDescription = "newDescription";
    PublicChannelUpdateRequest request = new PublicChannelUpdateRequest(
        newName, newDescription
    );

    given(channelRepository.findById(any())).willReturn(Optional.empty());

    // when
    // then
    assertThatThrownBy(() -> channelService.update(channelId, request))
        .isInstanceOf(ChannelNotFoundException.class);
  }

  @Test
  @DisplayName("유저 id 로 채널 조회 성공")
  public void findChannelByUserId_success() {
    // given
    List<ReadStatus> readStatusIds = List.of(new ReadStatus(user, channel, Instant.now()));
    given(readStatusRepository.findAllByUserId(userId)).willReturn(readStatusIds);
    given(channelRepository.findAllByTypeOrIdIn(eq(ChannelType.PUBLIC),
        eq(List.of(channel.getId())))).willReturn(List.of(channel));
    given(channelMapper.toDto(any(Channel.class))).willReturn(channelDto);

    // when
    List<ChannelDto> result = channelService.findAllByUserId(userId);

    // then
    assertThat(result).containsExactly(channelDto);
  }

  @Test
  @DisplayName("채널 삭제 성공")
  public void deleteChannel_success() {
    // given
    given(channelRepository.existsById(channelId)).willReturn(true);

    // when
    channelService.delete(channelId);

    // then
    verify(messageRepository).deleteAllByChannelId(channelId);
    verify(readStatusRepository).deleteAllByChannelId(channelId);
    verify(channelRepository).deleteById(channelId);
  }

  @Test
  @DisplayName("채널 삭제 실패 - 존재하지 않는 채널")
  public void deleteWithNonExistId_fail() {
    // given
    given(channelRepository.existsById(channelId)).willReturn(false);

    // when
    // then
    assertThatThrownBy(() -> channelService.delete(channelId))
        .isInstanceOf(ChannelNotFoundException.class);
  }

}
