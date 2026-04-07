package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.file.FileChannelRepository;
import com.sprint.mission.discodeit.repository.file.FileMessageRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class DiscodeitApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

		UserService userService = context.getBean(UserService.class);
		ChannelService channelService = context.getBean(ChannelService.class);
		MessageService messageService = context.getBean(MessageService.class);

//		clearDataFiles();

		runTest(userService, channelService, messageService);
	}

	private static void clearDataFiles() {
		Path dataMapPath = Paths.get(System.getProperty("user.dir"), "file-data-map");
		
		try {
			if (Files.exists(dataMapPath)) {
				// file-data-map 하위의 모든 파일과 디렉토리를 삭제
				Files.walk(dataMapPath)
					.sorted((a, b) -> b.compareTo(a)) // 역순 정렬 (하위부터 삭제)
					.forEach(path -> {
						try {
							if (path.toString().endsWith(".ser") && !Files.isDirectory(path)) {
								Files.delete(path);
							}
							System.out.println("삭제됨: " + path);
						} catch (IOException e) {
							System.err.println("삭제 실패: " + path + " - " + e.getMessage());
						}
					});
			}
			System.out.println("기존 데이터 초기화 완료");
		} catch (IOException e) {
			System.err.println("데이터 초기화 중 오류 발생: " + e.getMessage());
		}
	}

	private static void runTest(UserService userService, ChannelService channelService, MessageService messageService) {
		try {
			System.out.println("========== [1. 데이터 일괄 생성] ==========");
			// User 엔티티에 nickname이 없으므로 username, email, password만 사용
			User u1 = userService.create("lee", "lee@test.com", "1234");
			User u2 = userService.create("song", "song@test.com", "2345");
			User u3 = userService.create("kim", "kim@test.com", "3456");

			// Channel 생성 (ChannelType 포함)
			Channel c1 = channelService.create(ChannelType.PUBLIC, "자바기초", "자바 기본 문법 공부방");
			Channel c2 = channelService.create(ChannelType.PUBLIC, "프로젝트", "최종 프로젝트 협업 전용");

			System.out.println("유저/채널 생성 완료 (유저: " + userService.findAll().size() + ", 채널: " + channelService.findAll().size() + ")");

			// Message 생성 (인자 순서: content, channelId, authorId)
			messageService.create("안녕하세요, 자바 공부 시작합니다!", c1.getId(), u1.getId());
			messageService.create("반가워요 경훈님!", c1.getId(), u2.getId());
			messageService.create("저도 같이 공부해요.", c1.getId(), u3.getId());
			messageService.create("제네릭이 너무 어렵네요ㅠㅠ", c1.getId(), u1.getId());
			messageService.create("그거 스트림이랑 같이 보면 편해요.", c1.getId(), u2.getId());
			messageService.create("맞아요, 람다도 중요하죠!", c1.getId(), u3.getId());

			messageService.create("프로젝트 주제 정해졌나요?", c2.getId(), u1.getId());
			messageService.create("채팅 서비스로 하기로 했어요.", c2.getId(), u2.getId());
			messageService.create("저는 백엔드 맡을게요.", c2.getId(), u3.getId());
			Message m1 = messageService.create("그럼 전 프론트엔드 할게요!", c2.getId(), u1.getId());

			System.out.println("전체 메시지 등록 완료: " + messageService.findAll().size() + "개\n");

			System.out.println("========== [2-1. 특정 메시지 조회] ==========");
			// Optional 처리 및 닉네임 대신 Username 사용
			String channelName = channelService.find(m1.getChannelId()).getName();
			String userName = userService.find(m1.getAuthorId()).getUsername();

			System.out.println("채널 : " + channelName + " | 작성자 : " + userName);
			System.out.println("내용 : " + m1.getContent());
			System.out.println("----------------------------------------------------");

			System.out.println("\n========== [2-2. 유저 전체 조회] ==========");
			for (User u : userService.findAll()) {
				System.out.println("ID : " + u.getId() + " | 이름 : " + u.getUsername() + " | 이메일 : " + u.getEmail());
			}

			System.out.println("\n========== [3. 수정 및 재조회 검증] ==========");
			System.out.println("유저2 수정 전 이름: " + u2.getUsername());
			// BasicUserService.update(id, username, email, password)
			userService.update(u2.getId(), "민형마스터", null, null);

			User updatedU2 = userService.find(u2.getId());
			System.out.println("수정 후 이름: " + updatedU2.getUsername());

			// 메시지 수정 (content만 수정)
			messageService.update(m1.getId(), "전 풀스택 할래요!");
			// 엔티티가 새로 반환되거나 Optional로 다시 조회해야 업데이트된 내용 확인 가능
			Message updatedM1 = messageService.find(m1.getId());
			System.out.println("수정된 메시지 내용: " + updatedM1.getContent());

			System.out.println("\n========== [4. 특정 데이터 삭제 검증] ==========");
			System.out.println("삭제 전 메시지 수: " + messageService.findAll().size());
			messageService.delete(m1.getId());
			System.out.println("삭제 후 메시지 수: " + messageService.findAll().size());
//            System.out.println("삭제 확인 (Present 여부): " + messageService.find(m1.getId()));

			System.out.println("\n========== [5. 채널 삭제] ==========");
			channelService.delete(c1.getId());
			System.out.println("남은 채널 수: " + channelService.findAll().size());

			System.out.println("\n========= 모든 테스트 시나리오 종료 =========");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
