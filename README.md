# BCSD Internal API V2

BCSD 동아리 인터널(내부 관리) 서비스 백엔드. Spring Boot 4 / Java 21 / PostgreSQL 16.

## 로컬 실행

### 요구 사항
- JDK 21
- Docker (로컬 Postgres 컨테이너용)

### 절차

1. `.env` 생성
   ```
   cp .env.example .env
   ```
   `.env`를 열어 `JWT_SECRET`을 32바이트 이상 임의 문자열로 채운다.
   ```
   openssl rand -base64 32
   ```
   `.env`는 `.gitignore`에 포함되어 있다.

2. 로컬 Postgres 기동
   ```
   docker compose up -d
   ```
   이미 로컬에 5432 포트를 쓰는 Postgres가 있다면(Postgres.app, `brew services` 등) 충돌한다.
   `docker compose ps`로 컨테이너가 healthy인지 확인하고, 충돌 시:
   ```
   COMPOSE_POSTGRES_PORT=5433 docker compose up -d
   ```
   와 함께 `.env`에 `DB_PORT=5433`을 추가한다.

3. 애플리케이션 실행
   ```
   ./gradlew bootRun
   ```
   `developmentOnly` 의존성인 `springboot4-dotenv`가 `.env`를 자동으로 읽는다.
   별도 `--spring.profiles.active` 지정은 필요 없다 — `application.yml`의 프로퍼티 기본값이
   이미 로컬 개발을 기준으로 되어 있다(`DB_HOST:localhost`, `DB_PORT:5432` 등).

4. 확인
   ```
   curl http://localhost:8080/health
   ```
   `OK`(200)가 반환되면 정상이다. 부팅 로그에서 Flyway 마이그레이션이 전부 적용됐는지도 확인한다.

### 테스트 실행

```
./gradlew test
```

Testcontainers가 테스트 실행 시 Postgres 컨테이너를 자동으로 띄우므로 Docker가 실행 중이어야 한다.
로컬 Postgres(2번 단계)와는 별개다 — 테스트는 매번 격리된 컨테이너를 쓴다.

### 흔히 겪는 문제

- **`Cannot find a Java installation on your machine ... {languageVersion=21}`**
  이 프로젝트는 JDK 21 툴체인을 요구한다. JDK 21을 설치한 뒤 `~/.gradle/gradle.properties`
  (저장소가 아니라 사용자 홈, 커밋하지 않는다)에 다음을 추가한다.
  ```
  org.gradle.java.installations.paths=<JDK 21 설치 경로>
  ```

- **`FATAL: role "postgres" does not exist`로 Flyway 마이그레이션이 실패한다**
  로컬에 이미 떠 있는 다른 Postgres(홈브루 서비스 등)가 5432를 선점하고 있다는 신호다.
  위 2번 단계의 `COMPOSE_POSTGRES_PORT` 대안을 쓴다.

- **`JWT_SECRET`이 비어 있으면 부팅이 즉시 실패한다.**
  `.env`에 32바이트 이상 값을 채웠는지 확인한다(`app.jwt.secret: ${JWT_SECRET}`에 기본값이 없다).

## 배포

`main` push → GitHub Actions → ghcr.io 이미지 빌드 → 서버 SSH 접속 후 기존 컨테이너 종료·재시작(순차 재시작, 수초 다운타임). 서버가 KONECT 운영 서버를 공유하고 API 컨테이너 메모리 한도가 256MiB로 빠듯하므로, 배포 후 `docker stats`와 `free -h`로 확인한다.
