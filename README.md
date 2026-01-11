# 송금 서비스 (Remittance Service)

계좌 간 송금, 입금, 출금 기능을 제공하는 RESTful API 서비스입니다.

## 주요 기능

- 계좌 생성/삭제/조회
- 입금/출금/이체 (일 한도 제어)
- 거래내역 조회 (커서 기반 페이지네이션)
- 수수료 정책 관리

## 아키텍처(레이어 구조)

```
presentation   (Controller, DTO)
     ↓
application    (Facade, Service, Command)
     ↓
domain         (Entity, Repository Interface)
     ↓
infrastructure (JPA Repository Implementation)
```

---

## 실행 방법

### Docker Compose 실행

```bash
docker compose up --build
```

**접속 정보:**
- 앱: `http://localhost:8080`
- DB(MySQL): `localhost:3306`
  - db: `remittance`
  - user: `remittance`
  - password: `remittance`

### 테스트 실행

```bash
./gradlew test
```

---

## 문서

- **API 명세**: [docs/api.md](docs/api.md)
- **시스템 플로우 차트**: [docs/flow.md](docs/flow.md)
- **테스트 가이드**: [docs/test.md](docs/test.md)
- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 기술 스택

- **Language**: Java 17
- **Framework**: Spring Boot 3.5.9
- **Database**: MySQL 8.0
- **ORM**: JPA (Hibernate)
- **Build Tool**: Gradle
- **Container**: Docker & Docker Compose

---

## 핵심 구현 사항

### 1. 동시성 제어

비관적 락(Pessimistic Write Lock)과 트랜잭션 타임아웃을 사용하여 계좌 잔액의 동시성 문제를 해결합니다.

### 2. 성능 최적화 (DB 인덱스)

자주 사용되는 쿼리의 성능을 최적화하기 위해 적절한 인덱스를 설정했습니다.

### 3. 테스트

자세한 테스트 전략, 동시성 테스트, HTTP 테스트 스크립트는 [docs/test.md](docs/test.md)를 참고 부탁드립니다.

### 4. 문서화

API 명세, 시스템 플로우 차트, 테스트 가이드를 작성하여 프로젝트 이해도를 높였습니다.
