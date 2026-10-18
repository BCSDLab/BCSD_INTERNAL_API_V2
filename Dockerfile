FROM gradle:8-jdk21 AS builder
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
RUN gradle dependencies --no-daemon || true
COPY . .
RUN gradle bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=builder /app/build/libs/*.jar app.jar
USER app
EXPOSE 8081
# 컨테이너 한도(300m) 안에서 힙+비힙 전체를 예측 가능하게 묶는다.
# 300m 한도·NMT로 측정(부하 중): Metaspace 사용 약 120MiB라 상한 160m은 줄이지 않는다.
# 비힙은 주로 스레드 수(application.yml의 tomcat.threads.max)와 스택 512k·코드 캐시 32m로 줄인다.
# C1 전용 JIT(TieredStopAtLevel=1)는 로그인(BCrypt) 지연을 약 23% 늘려 쓰지 않는다.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=40.0", "-Xss512k", "-XX:ReservedCodeCacheSize=32m", "-XX:MaxMetaspaceSize=160m", "-XX:MaxDirectMemorySize=32m", "-XX:+ExitOnOutOfMemoryError", "-jar", "app.jar"]
