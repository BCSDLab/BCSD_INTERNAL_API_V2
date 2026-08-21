FROM gradle:8-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8081
# 컨테이너 한도(256m) 안에서 힙+비힙 전체를 예측 가능하게 묶는다.
# -Xmx256m은 힙 혼자 한도를 다 먹을 수 있어 메타스페이스/코드캐시/스택이 넘칠 때
# OOMKill을 유발했다. nproc=2 + 한도 256m이므로 GC는 JVM이 SerialGC를 자동 선택한다.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=45.0", "-XX:MaxMetaspaceSize=112m", "-XX:ReservedCodeCacheSize=64m", "-XX:MaxDirectMemorySize=32m", "-XX:+ExitOnOutOfMemoryError", "-jar", "app.jar"]
