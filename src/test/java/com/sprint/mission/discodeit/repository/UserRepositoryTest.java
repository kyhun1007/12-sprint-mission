package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.config.RepositoryTestConfig;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

public class UserRepositoryTest extends RepositoryTestConfig {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TestEntityManager em;

  private User createTestUser(String username, String email) {
    BinaryContent profile = new BinaryContent("profile.jpg", 100L, "image/jpeg");
    User user = new User(username, email, "password", profile);
    UserStatus status = new UserStatus(user, Instant.now());
    return user;
  }

  @Test
  @DisplayName("사용자 이름으로 검색 성공")
  void findByUsername_Success() {
    // given
    String username = "testUser";
    User user = createTestUser(username, "test@codeit.com");
    userRepository.save(user);

    em.flush();
    em.clear();

    // when
    Optional<User> foundUser = userRepository.findByUsername(username);

    // then
    assertThat(foundUser).isPresent();
    assertThat(foundUser.get().getUsername()).isEqualTo(username);
  }

  @Test
  @DisplayName("사용자 이름으로 검색 실패 - 존재하지 않는 사용자명")
  void findByNonExistentUsername_Success() {
    // given
    String username = "NonExistentUsername";

    // when
    Optional<User> foundUser = userRepository.findByUsername(username);

    // then
    assertThat(foundUser).isEmpty();
  }

  @Test
  @DisplayName("이메일로 사용자 존재여부 확인 성공")
  void findByEmail_Success() {
    // given
    String email = "test@codeit.com";
    User user = createTestUser("testUser", email);
    userRepository.save(user);

    em.flush();
    em.clear();

    // when
    boolean userExist = userRepository.existsByEmail(email);

    // then
    assertThat(userExist).isTrue();
  }

  @Test
  @DisplayName("이메일로 사용자 존재여부 확인 - 존재하지 않는 이메일 ")
  void findByNonExistentEmail_Success() {
    // given
    String email = "NonExistentEmail";

    // when
    boolean userExist = userRepository.existsByEmail(email);

    // then
    assertThat(userExist).isFalse();
  }

  @Test
  @DisplayName("모든 사용자를 프로필과 상태정보를 함께 조회")
  void findAllWithProfileAndStatus_Success() {
    User user1 = createTestUser("test1", "test1@codeit.com");
    User user2 = createTestUser("test2", "test2@codeit.com");

    userRepository.saveAll(List.of(user1, user2));

    em.flush();
    em.clear();

    List<User> users = userRepository.findAllWithProfileAndStatus();

    assertThat(users).hasSize(2);
    assertThat(users.get(0).getUsername()).isEqualTo("test1");
    assertThat(users.get(1).getUsername()).isEqualTo("test2");

    User foundUser1 = users.get(0);
    User foundUser2 = users.get(1);

    assertThat(Hibernate.isInitialized(foundUser1.getProfile())).isTrue();
    assertThat(Hibernate.isInitialized(foundUser1.getStatus())).isTrue();

    assertThat(Hibernate.isInitialized(foundUser2.getProfile())).isTrue();
    assertThat(Hibernate.isInitialized(foundUser2.getStatus())).isTrue();
  }
}
