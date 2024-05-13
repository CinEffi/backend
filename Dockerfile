# Base image 지정 : 주로 OS나 런타임 이미지를 지정
FROM openjdk:17-alpine as builder

# 커맨드를 실행하는 디렉토리를 지정, -w:오버라이딩
WORKDIR /app

COPY cineffi/gradlew cineffi/build.gradle cineffi/settings.gradle ./
COPY cineffi/gradle ./gradle
COPY cineffi/src/main ./src/main

RUN mkdir -p /root/.gradle
RUN echo "systemProp.http.proxyHost=krmp-proxy.9rum.cc\nsystemProp.http.proxyPort=3128\nsystemProp.https.proxyHost=krmp-proxy.9rum.cc\nsystemProp.https.proxyPort=3128" > /root/.gradle/gradle.properties

RUN ./gradlew bootJar

# app
FROM openjdk:17-alpine
WORKDIR /app

# 필요한 JAR 파일 복사
COPY --from=builder /app/build/libs/cineffi-0.0.1-SNAPSHOT.jar ./cineffi.jar
# 통신에 사용할 포트 지정
EXPOSE 4000

# 도커 이미지가 실행될 때 실행됨
ENTRYPOINT ["java", "-jar", "cineffi.jar", "--spring.profiles.active=dev"]
