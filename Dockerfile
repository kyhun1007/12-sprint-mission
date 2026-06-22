FROM amazoncorretto:17 AS builder
WORKDIR /app

COPY gradle ./gradle
COPY gradlew ./gradlew
COPY build.gradle settings.gradle ./

RUN chmod +x ./gradlew && ./gradlew buildEnvironment --no-daemon

COPY src ./src

RUN ./gradlew bootJar --no-daemon

FROM amazoncorretto:17-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*-SNAPSHOT.jar /app/app.jar

EXPOSE 80

ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar /app/app.jar"]