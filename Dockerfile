FROM amazoncorretto:17 AS builder
WORKDIR /app
COPY gradle ./gradle
COPY gradlew ./gradlew
RUN chmod +x ./gradlew
COPY build.gradle settings.gradle ./
RUN ./gradlew dependencies --no-daemon
COPY src ./src
RUN ./gradlew build -x test --no-daemon

FROM amazoncorretto:17-alpine3.21
WORKDIR /app
# builder 스테이지에서 생성된 jar 파일을 범용성 있게 복사
COPY --from=builder /app/build/libs/*.jar /app/app.jar
EXPOSE 80
ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar /app/app.jar"]