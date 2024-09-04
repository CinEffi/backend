# Base image 지정 : 주로 OS나 런타임 이미지를 지정
FROM openjdk:17-jdk-slim-buster as builder

# 커맨드를 실행하는 디렉토리를 지정, -w:오버라이딩
WORKDIR /app

COPY ./gradlew ./build.gradle ./settings.gradle ./
COPY ./gradle ./gradle
COPY ./src/main ./src/main

RUN mkdir -p /root/.gradle
RUN ./gradlew bootJar

# app
FROM openjdk:17-slim-buster
WORKDIR /app

# 필요한 JAR 파일 복사
COPY --from=builder /app/build/libs/cineffi-0.0.1-SNAPSHOT.jar ./cineffi.jar
# 통신에 사용할 포트 지정
EXPOSE 4000

# 도커 이미지가 실행될 때 실행됨
ENTRYPOINT ["java", "-jar", "cineffi.jar", "--spring.profiles.active=dev"]
