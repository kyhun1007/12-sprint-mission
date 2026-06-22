package com.sprint.mission.discodeit.storage.s3;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Properties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AWSS3Test {

  private static S3Client s3Client;
  private static S3Presigner sPresigner;
  private static String bucketName;
  private static final String KEY = "test-folder/s3-test-file.txt";

  @BeforeAll
  static void init() throws IOException {
    Properties envProperties = new Properties();
    try (FileInputStream fis = new FileInputStream(".env")) {
      envProperties.load(fis);
    }

    String accessKey = envProperties.getProperty("AWS_S3_ACCESS_KEY");
    String secretKey = envProperties.getProperty("AWS_S3_SECRET_KEY");
    String regionStr = envProperties.getProperty("AWS_S3_REGION", "ap-northeast-2");
    bucketName = envProperties.getProperty("AWS_S3_BUCKET");

    if (accessKey == null || secretKey == null || bucketName == null) {
      throw new IllegalArgumentException(".env 파일에 AWS 자격증명 및 버킷 이름 설정이 누락되었습니다.");
    }

    Region region = Region.of(regionStr);
    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
    StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

    s3Client = S3Client.builder()
        .region(region)
        .credentialsProvider(credentialsProvider)
        .build();

    sPresigner = S3Presigner.builder()
        .region(region)
        .credentialsProvider(credentialsProvider)
        .build();
  }

  @Test
  @Order(1)
  void uploadTest() {
    System.out.println("=== [S3 업로드 테스트 시작] ===");
    String content = "Hello, S3! This is a test file for Discodeit storage system.";

    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(KEY)
        .contentType("text/plain")
        .build();

    s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content.getBytes()));
    System.out.println("성공: S3 버킷 [" + bucketName + "]에 '" + KEY + "' 파일 업로드 완료");
  }

  @Test
  @Order(2)
  void downloadTest() throws IOException {
    System.out.println("=== [S3 다운로드 테스트 시작] ===");

    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucketName)
        .key(KEY)
        .build();

    // S3로부터 파일 바이트 읽기
    ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
    String downloadedContent = objectBytes.asUtf8String();

    System.out.println("성공: S3로부터 다운로드한 내용 -> " + downloadedContent);

    Path tempFile = Files.createTempFile("s3-downloaded-", ".txt");
    Files.write(tempFile, objectBytes.asByteArray());
    System.out.println("임시 파일 저장 위치: " + tempFile.toAbsolutePath());
  }

  @Test
  @Order(3)
  void generatePresignedUrlTest() {
    System.out.println("=== [S3 PresignedUrl 생성 테스트 시작] ===");

    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucketName)
        .key(KEY)
        .build();

    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(Duration.ofMinutes(10))
        .getObjectRequest(getObjectRequest)
        .build();

    PresignedGetObjectRequest presignedGetObjectRequest = sPresigner.presignGetObject(
        presignRequest);
    String presignedUrl = presignedGetObjectRequest.url().toString();

    System.out.println("성공: 10분간 유효한 다운로드용 Presigned URL이 생성되었습니다.");
    System.out.println("생성된 URL: " + presignedUrl);
  }
}