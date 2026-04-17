package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@SpringBootApplication
public class DiscodeitApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

		UserService userService = context.getBean(UserService.class);
		ChannelService channelService = context.getBean(ChannelService.class);
		MessageService messageService = context.getBean(MessageService.class);
		BinaryContentService binaryContentService = context.getBean(BinaryContentService.class);
		UserStatusService userStatusService = context.getBean(UserStatusService.class);
		ReadStatusService readStatusService = context.getBean(ReadStatusService.class);

//		clearDataFiles();

		runTest(userService, channelService, messageService,
				binaryContentService, userStatusService, readStatusService);

		System.out.println("http://localhost:8080/");
	}

	private static void clearDataFiles() {
		// 삭제는 수동 경로 설정 필요함
		Path dataMapPath = Paths.get(System.getProperty("user.dir"), ".discodeit");
		
		try {
			if (Files.exists(dataMapPath)) {
				Files.walk(dataMapPath)
					.sorted((a, b) -> b.compareTo(a))
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

	private static void runTest(UserService userService,
								ChannelService channelService,
								MessageService messageService,
								BinaryContentService binaryContentService,
								UserStatusService userStatusService,
								ReadStatusService readStatusService) {
		try {
			System.out.println("========== [1. 데이터 일괄 생성] ==========");
			BinaryContent b1 = binaryContentService.create(new BinaryContentCreateRequest("kdksadflaflflaslslfafssaasd".getBytes()));
			BinaryContent b2 = binaryContentService.create(new BinaryContentCreateRequest("kdkasjdkasfaskdasddasdkdkdkdkdasaddasdd".getBytes()));

			User u1 = userService.create(new UserCreateRequest("lee", "lee@test.com", "1234", b1.getId()));
			User u2 = userService.create(new UserCreateRequest("song", "song@test.com", "2345", null));
			User u3 = userService.create(new UserCreateRequest("kim", "kim@test.com", "3456", null));

			Channel c1 = channelService.createPublicChannel(new PublicChannelCreateRequest("자바기초", "자바 기본 문법 공부방"));
			Channel c2 = channelService.createPublicChannel(new PublicChannelCreateRequest("프로젝트", "최종 프로젝트 협업 전용"));

			System.out.println("유저/채널 생성 완료 (유저: " + userService.findAll().size() + ", 채널: " + channelService.findAllByUserId(u1.getId()).size() + ")");

			messageService.create(new MessageCreateRequest(c1.getId(), u1.getId(), "안녕하세요, 자바 공부 시작합니다!", List.of(b2.getId())));
			messageService.create(new MessageCreateRequest(c1.getId(), u2.getId(), "반가워요 경훈님!", null));
			messageService.create(new MessageCreateRequest(c1.getId(), u3.getId(), "저도 같이 공부해요.", null));
			messageService.create(new MessageCreateRequest(c1.getId(), u1.getId(), "제네릭이 너무 어렵네요ㅠㅠ", null));
			messageService.create(new MessageCreateRequest(c1.getId(), u2.getId(), "그거 스트림이랑 같이 보면 편해요.", null));
			messageService.create(new MessageCreateRequest(c1.getId(), u3.getId(), "맞아요, 람다도 중요하죠!", null));

			messageService.create(new MessageCreateRequest(c2.getId(), u1.getId(), "프로젝트 주제 정해졌나요?", null));
			messageService.create(new MessageCreateRequest(c2.getId(), u2.getId(), "채팅 서비스로 하기로 했어요.", null));
			messageService.create(new MessageCreateRequest(c2.getId(), u3.getId(), "저는 백엔드 맡을게요.", null));
			MessageDto m1 = messageService.create(new MessageCreateRequest(c2.getId(), u1.getId(), "그럼 전 프론트엔드 할게요!", null));

			System.out.println("전체 메시지 등록 완료 c1 : " + messageService.findAllByChannelId(c1.getId()).size() + "개");
			System.out.println("전체 메시지 등록 완료 c2 : " + messageService.findAllByChannelId(c2.getId()).size() + "개\n");

//			System.out.println("========== [2-1. 특정 메시지 조회] ==========");
//			String channelName = channelService.find(m1.channelId()).name();
//			String userName = userService.find(m1.authorId()).username();
//
//			System.out.println("채널 : " + channelName + " | 작성자 : " + userName);
//			System.out.println("내용 : " + m1.content());
//			System.out.println("----------------------------------------------------");
//
//			System.out.println("\n========== [2-2. 유저 전체 조회] ==========");
//			for (UserResponse u : userService.findAll()) {
//				System.out.println("ID : " + u.id() + " | 이름 : " + u.username() + " | 이메일 : " + u.email() + "| 프로필 :" + u.profileImageId());
//			}
//
//			System.out.println("\n========== [3. 수정 및 재조회 검증] ==========");
//			System.out.println("유저2 수정 전 이름: " + u2.getUsername());
//			System.out.println("유저2 수정 전 프로필 이미지 ID: " + u2.getProfileImageId());
//			userService.update(new UserUpdateRequest(u2.getId(), "민형마스터", null, null, b2.getId()));
//
//			UserResponse updatedU2 = userService.find(u2.getId());
//			System.out.println("수정 후 이름: " + updatedU2.username());
//			System.out.println("수정 후 프로필 이미지 ID: " + updatedU2.profileImageId());
//
//			messageService.update(new MessageUpdateRequest(m1.id(), "전 풀스택 할래요!", null));
//
//			Message updatedM1 = messageService.find(m1.id());
//			System.out.println("수정된 메시지 내용: " + updatedM1.getContent());
//
//			System.out.println("\n========== [4. 특정 데이터 삭제 검증] ==========");
//			System.out.println("삭제 전 메시지 수: " + messageService.totalMessageNumber());
//			messageService.delete(m1.id());
//			System.out.println("삭제 후 메시지 수: " + messageService.totalMessageNumber());
//
//			System.out.println("\n========== [5. 채널 삭제] ==========");
//			channelService.delete(c1.getId());
//			System.out.println("남은 채널 수: " + channelService.findAllByUserId(u1.getId()).size());
//
//			System.out.println("\n========= 모든 테스트 시나리오 종료 =========");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
