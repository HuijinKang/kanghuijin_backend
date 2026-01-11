# 송금 서비스 (Remittance Service)

계좌 간 송금, 입금, 출금 기능을 제공하는 RESTful API 서비스입니다.

## 주요 기능

- 계좌 생성/삭제/조회
- 입금/출금/이체 (일 한도 제어)
- 거래내역 조회 (커서 기반 페이지네이션)
- 수수료 정책 관리

## 아키텍처(레이어 구조)

```
presentation  (Controller, DTO)
     ↓
application   (Facade, Service, Command)
     ↓
domain        (Entity, Repository Interface)
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

- **API 명세 문서**: [docs/api.md](docs/api.md)
- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **테스트**: [docs/test.md](docs/test.md)

---

## 기술 스택

- **Language**: Java 17
- **Framework**: Spring Boot 3.x
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

---


---

## 비즈니스 규칙

### 계좌

- 계좌번호는 10~14자리 숫자
- 계좌는 ACTIVE 또는 CLOSED 상태
- 계좌 폐쇄 후에는 거래 불가

### 입금

- 최소 금액: 1원
- 최대 금액: 1억원
- 일 한도: 제한 없음

### 출금

- 최소 금액: 1원
- 최대 금액: 1억원
- **일 한도**: 1,000,000원 (정책으로 변경 가능)
- 잔액 부족 시 출금 불가

### 이체

- 최소 금액: 1원
- 최대 금액: 1억원
- **일 한도**: 3,000,000원 (정책으로 변경 가능)
- **수수료**: 이체 금액의 1% (정책으로 변경 가능)
- 잔액 부족 시 이체 불가 (이체 금액 + 수수료)

---

## 테스트

자세한 테스트 전략, 동시성 테스트, HTTP 테스트 스크립트는 [TEST.md](TEST.md)를 참고하세요.

### 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 동시성 테스트만 실행
./gradlew test --tests ConcurrencyTest
```

### HTTP 테스트 스크립트

### 테스트 환경 요구사항

- **통합 테스트**: Docker Desktop (또는 Docker Engine) 필요