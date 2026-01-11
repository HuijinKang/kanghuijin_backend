# remittance-service

## API 명세

- API 명세 문서: [docs/api.md](docs/api.md)

## API 명세(Swagger)

- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## 실행 방법 (Docker Compose)

```bash
docker compose up --build
```

- 앱: `http://localhost:8080`
- DB(MySQL): `localhost:3306`
  - db: `remittance`
  - user: `remittance`
  - password: `remittance`

## 테스트

### 단위 테스트
- `@ExtendWith(MockitoExtension.class)` 기반 서비스 단위 테스트
- `@WebMvcTest` 기반 컨트롤러 테스트

### 통합 테스트
- `@SpringBootTest` + Testcontainers(MySQL) 기반 통합 테스트
- 실행 시 **Docker Desktop(또는 Docker Engine)** 이 필요합니다.

### HTTP 테스트 스크립트

- IntelliJ HTTP Client로 아래 파일을 실행하면 계좌 API를 빠르게 호출해볼 수 있습니다.

#### 계좌 생성/삭제 HTTP 테스트 스크립트

- `scripts/http/account-api.http`

#### 입금/출금/이체 HTTP 테스트 스크립트

- `scripts/http/transaction-api.http`

#### 정책(수수료/한도) HTTP 테스트 스크립트

- `scripts/http/policy-api.http`

### 테스트 실행

```bash
./gradlew test
```

## 동시성 제어 및 테스트

### 동시성 제어 메커니즘

본 프로젝트는 **비관적 락(Pessimistic Write Lock)** 을 사용하여 계좌 잔액의 동시성 문제를 해결합니다.

#### 구현 방식

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select a from Account a where a.id = :id")
Optional<Account> findByIdForUpdate(@Param("id") long id);
```

- **입금/출금**: 계좌 ID 기반 락 획득
- **이체**: 데드락 방지를 위해 계좌번호 오름차순으로 락 획득

### 동시성 테스트

`ConcurrencyTest` 클래스에서 3가지 시나리오를 검증합니다:

#### 1. 동시 입금 테스트
- **시나리오**: 100개 스레드가 동시에 같은 계좌에 1,000원씩 입금
- **검증**: 최종 잔액 = 100,000원 (손실 없음)
- **목적**: PESSIMISTIC_WRITE 락이 입금 시 잔액 일관성을 보장하는지 확인

#### 2. 동시 출금 테스트
- **시나리오**: 초기 잔액 200,000원에서 100개 스레드가 1,000원씩 동시 출금
- **검증**: 최종 잔액 = 100,000원 (손실 없음)
- **목적**: 동시 출금 시 잔액이 정확하게 차감되는지 확인

#### 3. 동시 이체 데드락 방지 테스트
- **시나리오**: 계좌 A ↔ B 간 50번씩 왕복 이체 (총 100건)
- **검증**: 
  - 데드락 없이 완료
  - 총 잔액 = 200,000원 - 500원 (100건 × 5원 수수료)
- **목적**: 계좌번호 정렬 기반 락 순서 제어로 데드락이 방지되는지 확인

### 락 없이 테스트하기 (실패 케이스 확인)

동시성 제어의 중요성을 확인하려면 락을 제거하고 테스트할 수 있습니다:

**락 제거 방법**: `AccountJpaRepository`에서 `@Lock` 어노테이션 주석 처리

```java
// @Lock(LockModeType.PESSIMISTIC_WRITE)  // 이 줄을 주석 처리
@Query("select a from Account a where a.id = :id")
Optional<Account> findByIdForUpdate(@Param("id") long id);
```

**예상 결과**:
- **입금 테스트 실패**: 최종 잔액 < 100,000원 (예: 87,000원)
  - Lost Update 발생: 여러 스레드가 같은 잔액을 읽고 덮어씀
- **출금 테스트 실패**: 최종 잔액 > 100,000원 (예: 113,000원)
  - Race Condition: 출금 계산이 중복으로 누락됨
- **이체 테스트 실패**: 데드락 발생 가능 또는 잔액 불일치

**테스트 실행**:

```bash
# 1. AccountJpaRepository에서 @Lock 주석 처리
# 2. 동시성 테스트만 실행
./gradlew test --tests ConcurrencyTest

# 결과: 테스트 실패 확인
```

이를 통해 락이 없을 때 발생하는 동시성 문제를 직접 확인할 수 있습니다.

