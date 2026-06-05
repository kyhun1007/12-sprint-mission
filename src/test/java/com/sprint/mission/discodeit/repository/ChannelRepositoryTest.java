package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.config.RepositoryTestConfig;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

public class ChannelRepositoryTest extends RepositoryTestConfig {

  @Autowired
  private ChannelRepository channelRepository;

  @Autowired
  private TestEntityManager em;

  private Channel createTestChannel(ChannelType type, String name) {
    Channel channel = new Channel(type, name, "description" + name);
    return channelRepository.save(channel);
  }

  @Test
  @DisplayName("공개 채널과 ID 목록에 포함된 채널을 모두 조회 성공")
  void findAllByTypeOrIdIn_success() {
    // given
    Channel publicChannel1 = createTestChannel(ChannelType.PUBLIC, "public1");
    Channel publicChannel2 = createTestChannel(ChannelType.PUBLIC, "public2");
    Channel privateChannel1 = createTestChannel(ChannelType.PRIVATE, "private1");
    Channel privateChannel2 = createTestChannel(ChannelType.PRIVATE, "private2");

    channelRepository.saveAll(List.of(
        publicChannel1, publicChannel2, publicChannel1, privateChannel2));

    em.flush();
    em.clear();

    List<UUID> private1Id = List.of(privateChannel1.getId());

    // when
    List<Channel> foundChannels = channelRepository.findAllByTypeOrIdIn(ChannelType.PUBLIC,
        private1Id);

    // then
    assertThat(foundChannels).hasSize(3);

    assertThat(
        foundChannels.stream().filter(c -> c.getType() == ChannelType.PUBLIC).count()).isEqualTo(2);

    List<Channel> privateChannels = foundChannels.stream()
        .filter(c -> c.getType() == ChannelType.PRIVATE)
        .toList();
    assertThat(privateChannels).hasSize(1);
    assertThat(privateChannels.get(0).getId()).isEqualTo(privateChannel1.getId());
  }

  @Test
  @DisplayName("공개 채널이 없고 ID를 입력하지 않으면 빈 리스트를 반환")
  void findAllByTypeAndIdIn_ReturnEmptyList() {
    // given
    Channel privateChannel = createTestChannel(ChannelType.PRIVATE, "public1");

    channelRepository.saveAll(List.of(privateChannel));
    em.flush();
    em.clear();

    // when
    List<Channel> foundChannel = channelRepository.findAllByTypeOrIdIn(ChannelType.PUBLIC,
        List.of());

    // then
    assertThat(foundChannel).isEmpty();
  }
}
