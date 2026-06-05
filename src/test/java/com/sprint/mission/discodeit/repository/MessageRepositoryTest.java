package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.config.RepositoryTestConfig;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;


public class MessageRepositoryTest extends RepositoryTestConfig {

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private ChannelRepository channelRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TestEntityManager em;

  private User createTestUser(String username, String email) {
    BinaryContent profile = new BinaryContent("profile.jpg", 100L, "image/jpeg");
    User user = new User(username, email, "password", profile);
    UserStatus status = new UserStatus(user, Instant.now());
    return userRepository.save(user);
  }

  private Channel createTestChannel(ChannelType type, String name) {
    Channel channel = new Channel(type, name, "description" + name);
    return channelRepository.save(channel);
  }

  private Message createTestMessage(String content, Channel channel, User author,
      Instant createdAt) {
    Message message = new Message(content, channel, author, new ArrayList<>());

    if (createdAt != null) {
      ReflectionTestUtils.setField(message, "createdAt", createdAt);
    }

    Message savedMessage = messageRepository.save(message);
    em.flush();

    return savedMessage;
  }

  @Test
  @DisplayName("")
  void findAllByChannelIdWithAuthor_Success() {
    User user = createTestUser("testUser", "test@codeit.com");
    Channel channel = createTestChannel(ChannelType.PRIVATE, "테스트채널");

    Instant now = Instant.now();
    Instant fiveMinuteAgo = now.minusSeconds(300);
    Instant tenMinutesAgo = now.minusSeconds(600);

    Message message1 = createTestMessage("message1", channel, user, now);
    Message message2 = createTestMessage("message2", channel, user, fiveMinuteAgo);
    Message message3 = createTestMessage("message3", channel, user, tenMinutesAgo);

    em.flush();
    em.clear();

    Slice<Message> messages = messageRepository.findAllByChannelIdWithAuthor(
        channel.getId(),
        now.plusSeconds(1),
        PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"))
    );

    assertThat(messages).isNotNull();
    assertThat(messages.hasContent()).isTrue();
    assertThat(messages.getContent()).hasSize(2);
    assertThat(messages.hasNext()).isTrue();

    List<Message> content = messages.getContent();
    assertThat(content.get(0).getCreatedAt()).isAfterOrEqualTo(content.get(1).getCreatedAt());

    Message firstMessage = content.get(0);
    assertThat(Hibernate.isInitialized(firstMessage.getAuthor())).isTrue();
    assertThat(Hibernate.isInitialized(firstMessage.getAuthor().getStatus())).isTrue();
    assertThat(Hibernate.isInitialized(firstMessage.getAuthor().getProfile())).isTrue();
  }

  @Test
  @DisplayName("")
  void findLatestMessageAtChannel_Success() {
    User user = createTestUser("testUser", "test@codeit.com");
    Channel channel = createTestChannel(ChannelType.PRIVATE, "테스트채널");

    Instant now = Instant.now();
    Instant fiveMinuteAgo = now.minusSeconds(300);
    Instant tenMinutesAgo = now.minusSeconds(600);

    Message message1 = createTestMessage("message1", channel, user, now);
    Message message2 = createTestMessage("message2", channel, user, fiveMinuteAgo);
    Message message3 = createTestMessage("message3", channel, user, tenMinutesAgo);

    em.flush();
    em.clear();

    Optional<Instant> lastMessageTime = messageRepository.findLastMessageAtByChannelId(
        channel.getId());

    assertThat(lastMessageTime.isPresent()).isTrue();
    assertThat(lastMessageTime.get()).isEqualTo(now);
  }

}
