# PookyBlog Docker Compose 실행

## 준비

1. Docker Engine 또는 Docker Desktop과 Compose v2를 설치합니다.
2. `.env.example`을 `.env`로 복사합니다.
3. `.env`의 비밀번호와 `JWT_SECRET`을 안전한 값으로 변경합니다.

JWT secret은 최소 32바이트의 임의 값을 Base64로 인코딩해서 사용합니다. `.env`는 Git에 포함되지 않습니다.

## 실행

```bash
docker compose up -d --build
```

브라우저에서 `http://localhost:8080`으로 접속합니다. `WEB_PORT` 환경변수로 호스트 포트를 바꿀 수 있습니다. web/BFF 이외의 애플리케이션과 MySQL, Redis, Kafka는 호스트에 포트를 공개하지 않습니다.

상태와 로그는 다음 명령으로 확인합니다.

```bash
docker compose ps
docker compose logs -f web
```

## 종료와 데이터 보존

```bash
docker compose down
```

컨테이너와 네트워크만 제거합니다. `mysql-data`, `redis-data`, `kafka-data` named volume은 유지되어 다음 실행에서 데이터와 Kafka consumer offset을 재사용합니다.

```bash
docker compose down -v
```

컨테이너, 네트워크와 함께 named volume도 제거합니다. 데이터베이스 데이터, Redis 데이터, Kafka log와 consumer offset이 모두 삭제되므로 초기화가 필요할 때만 사용합니다.

## 환경 구분

- 기본 `application.yml`: IntelliJ 등에서 직접 실행하는 local 설정
- `application-docker.yml`: Compose가 `SPRING_PROFILES_ACTIVE=docker`로 활성화
- `application-production.yml`: 실제 서버에서 별도 운영 환경변수와 함께 사용

Docker profile에서는 Compose service name인 `mysql`, `redis`, `kafka`, `user-service`, `post-service` 등을 hostname으로 사용합니다.

## 데이터베이스 스키마

현재 서비스들은 하나의 MySQL schema를 공유하며 Docker에서도 이 구조를 유지합니다. MySQL은 `utf8mb4`, `utf8mb4_0900_ai_ci`, Asia/Seoul timezone으로 실행됩니다. 현재 개발 설정과 동일하게 Hibernate `ddl-auto=update`가 스키마를 생성·보정하며, `Outbox.payload`의 JPA 정의는 `TEXT`입니다.

포트폴리오 배포를 넘어 운영 환경으로 전환할 때는 Flyway 또는 Liquibase migration으로 `ddl-auto=validate` 전환을 권장합니다.
