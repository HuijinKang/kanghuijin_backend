# 테스트 가이드

## 목차

- 기술 스택
- 테스트 실행
- 환경 요구사항
- 테스트 구조
- 동시성 테스트
- 테스트 성공 시나리오
- HTTP 테스트 스크립트


## 기술 스택

- JUnit 5
- Mockito
- Testcontainers (MySQL 8.0)
- AssertJ

## 테스트 실행

```bash
# 전체 테스트
./gradlew test
```

## 환경 요구사항

- Docker Desktop 필수 (Docker Engine)

---

## 테스트 구조

### 단위 테스트
- **서비스**: 비즈니스 로직 검증

### 통합 테스트
- **프레임워크**: `@SpringBootTest` + `@Testcontainers`
- **목적**: 실제 DB 연동하여 전체 플로우 검증

### 동시성 테스트
- **목적**: 멀티스레드 환경에서 데이터 일관성 검증

---

## 동시성 테스트

### 메커니즘

**비관적 락 + 트랜잭션 타임아웃**

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select a from Account a where a.id = :id")
Optional<Account> findByIdForUpdate(@Param("id") long id);

@Transactional(isolation = Isolation.SERIALIZABLE, timeout = 10)
public void deposit(DepositCommand command, String idempotencyKey) { }
```

### 전략

1. **입금/출금**: 계좌 ID 기반 락 획득
2. **이체**: 계좌번호 오름차순으로 락 획득 (데드락 방지)
3. **타임아웃**: 10초 설정 (스레드 고갈 방지, 장애 전파 차단)

---

## 테스트 성공 시나리오

### 1. 동시 입금
- **시나리오**: 100개 스레드가 동시에 1,000원씩 입금
- **검증**: 최종 잔액 = 100,000원 (손실 없음)

### 2. 동시 이체 (데드락 방지)
- **시나리오**: 계좌 A(초기 100,000원) ↔ B(초기 100,000원) 간 500원씩 50번 왕복 이체 (총 100건)
- **검증**: 
  - 데드락 없이 모든 이체 완료
  - 최종 총 잔액 = 199,500원 (초기 200,000원 - 수수료 500원)
  - 수수료 계산: 100건 × (500원 × 1%) = 500원
- **데드락 방지 원리**: 
  - 모든 트랜잭션이 **계좌번호 오름차순**으로 락 획득
  - A→B 이체: A(작은번호) 락 → B(큰번호) 락
  - B→A 이체: A(작은번호) 락 → B(큰번호) 락
  - 두 트랜잭션 모두 동일한 순서로 락을 획득하므로 순환 대기 불가능

---

## HTTP 테스트 스크립트

IntelliJ HTTP Client로 API를 빠르게 테스트할 수 있습니다.

### 계좌 API
- `scripts/http/account-api.http`

### 거래 API
- `scripts/http/transaction-api.http`

### 거래 내역 API
- `scripts/http/transaction-history-api.http`

### 수수료/한도 정책 API
- `scripts/http/transaction-policy-api.http`
