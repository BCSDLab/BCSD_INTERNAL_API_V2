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
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=45.0", "-XX:MaxMetaspaceSize=160m", "-XX:ReservedCodeCacheSize=48m", "-XX:MaxDirectMemorySize=32m", "-XX:+ExitOnOutOfMemoryError", "-jar", "app.jar"]
