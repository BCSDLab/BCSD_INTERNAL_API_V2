FROM gradle:8-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8081
# 컨테이너 한도(300m) 안에서 힙+비힙 전체를 예측 가능하게 묶는다.
# -Xmx256m은 힙 혼자 한도를 다 먹을 수 있어 메타스페이스/코드캐시/스택이 넘칠 때
# OOMKill을 유발했다. nproc=2 + 한도 300m이므로 GC는 JVM이 SerialGC를 자동 선택한다.
# MaxMetaspaceSize는 원래 112m였는데, 이후 태스크(AWS SDK·jsoup·springdoc 등)가 늘면서
# 클래스 로딩이 그걸 넘겨 "Terminating due to java.lang.OutOfMemoryError: Metaspace"로
# 운영에서 크래시 루프가 났다(실측: 부팅 직후에도 이미 112m 턱밑). 160m로 올리고,
# 내부 관리 도구라 피크 JIT 성능보다 메모리 여유가 더 중요해 코드캐시를 48m로 줄여 상쇄한다.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=45.0", "-XX:MaxMetaspaceSize=160m", "-XX:ReservedCodeCacheSize=48m", "-XX:MaxDirectMemorySize=32m", "-XX:+ExitOnOutOfMemoryError", "-jar", "app.jar"]
