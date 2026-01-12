# 시스템 플로우 차트

## 목차

1. 전체 시스템 아키텍처
2. 이체 처리 플로우
3. 에러 처리 플로우
4. 주요 설계 포인트

## 1. 전체 시스템 아키텍처

```mermaid
graph TB
    subgraph presentation [Presentation Layer]
        AC[AccountController]
        TC[TransactionController]
        THC[TransactionHistoryController]
        TPC[TransactionPolicyController]
    end
    
    subgraph application [Application Layer]
        AF[AccountFacade]
        TF[TransactionFacade]
        THF[TransactionHistoryFacade]
        
        AS[AccountService]
        TS[TransactionService]
        THS[TransactionHistoryService]
        TPS[TransactionPolicyService]
        ITH[InternalTransferHandler]
    end
    
    subgraph domain [Domain Layer]
        Account[Account Entity]
        Transaction[Transaction Entity]
        Policy[TransactionPolicy Entity]
    end
    
    subgraph infrastructure [Infrastructure Layer]
        AR[AccountRepository]
        TR[TransactionRepository]
        TPR[TransactionPolicyRepository]
        DB[(MySQL Database)]
    end
    
    AC --> AF
    TC --> TF
    THC --> THF
    TPC --> TPS
    
    AF --> AS
    TF --> AS
    TF --> TS
    TF --> TPS
    TF --> ITH
    THF --> THS
    
    AS --> Account
    TS --> Transaction
    TPS --> Policy
    THS --> Transaction
    
    Account --> AR
    Transaction --> TR
    Policy --> TPR
    
    AR --> DB
    TR --> DB
    TPR --> DB
```

---

## 2. 이체 처리 플로우

```mermaid
sequenceDiagram
    participant Client
    participant Controller as TransactionController
    participant Facade as TransactionFacade
    participant Handler as InternalTransferHandler
    participant AccService as AccountService
    participant TxService as TransactionService
    participant PolicyService as TransactionPolicyService
    participant DB as Database
    
    Client->>Controller: POST /v1/transfers (X-Idempotency-Key)
    Note over Controller: 요청 검증 + command 생성
    Controller->>Facade: transfer(command, idempotencyKey)
    
    Note over Facade: @Transactional(SERIALIZABLE, timeout=10)
    
    Facade->>Facade: 계좌번호 오름차순 정렬
    Note over Facade: 데드락 방지
    
    Facade->>AccService: findByAccountNumberForUpdate(작은번호)
    AccService->>DB: SELECT ... FOR UPDATE
    DB-->>AccService: 계좌1 (락 획득)
    
    Facade->>AccService: findByAccountNumberForUpdate(큰번호)
    AccService->>DB: SELECT ... FOR UPDATE
    DB-->>AccService: 계좌2 (락 획득)
    
    Note over Facade: 멱등성 체크 (transferRoute=INTERNAL_CORE, idempotencyKey)
    Facade->>TxService: findByTransferRouteAndIdempotencyKey(...)
    TxService->>DB: SELECT transactions (unique key)
    DB-->>TxService: existing? (optional)
    TxService-->>Facade: existing
    alt alreadyProcessed
        Note over Facade: 기존 요청과 동일하면 즉시 종료 (중복 송금 방지)
        Facade-->>Controller: 완료
        Controller-->>Client: 200 OK
    else firstTime
    end

    Facade->>AccService: validateAccountActive(송금계좌)
    Facade->>AccService: validateAccountActive(수취계좌)

    Facade->>Handler: handle(transferCommand)
    Note over Handler: 현재는 INTERNAL_CORE만 사용 (추후 타행/해외 확장 지점)

    Facade->>PolicyService: getTransferDailyLimit()
    PolicyService-->>Facade: 3,000,000원
    
    Facade->>PolicyService: calculateTransferFee(amount)
    PolicyService-->>Facade: 수수료 (1%)
    
    Facade->>TxService: getTodayTransferTotal(송금계좌)
    TxService->>DB: SUM(오늘 이체액)
    DB-->>TxService: 오늘 총액
    TxService-->>Facade: 오늘 총액
    
    Note over Facade: 한도 체크
    
    Facade->>AccService: transferMoney(송금, 수취, 금액, 수수료)
    AccService->>DB: UPDATE accounts (잔액 변경)
    
    Facade->>TxService: recordTransfer(송금, 수취, 금액, 수수료)
    Note over TxService: TRX-YYYYMMDD-UUID transactionId 생성/저장
    TxService->>DB: INSERT transactions (2건, transferRoute=INTERNAL_CORE)
    
    Note over Facade: 트랜잭션 커밋
    
    Facade-->>Controller: 완료
    Controller-->>Client: 200 OK
```

---

## 3. 에러 처리 플로우

```mermaid
graph TB
    Start[요청 시작]
    Validation{입력 검증}
    Idempotency{멱등성 키 중복?}
    AccountCheck{계좌 존재?}
    StatusCheck{계좌 활성?}
    LimitCheck{한도 초과?}
    BalanceCheck{잔액 충분?}
    Process[처리 성공]
    
    E400_1[400 VALIDATION_ERROR]
    E404[404 NOT_FOUND]
    E400_2[400 ACCOUNT_CLOSED]
    E400_3[400 DAILY_LIMIT_EXCEEDED]
    E400_4[400 INSUFFICIENT_BALANCE]
    E409[409 IDEMPOTENCY_CONFLICT]
    E200[200 OK]
    
    Start --> Validation
    Validation -->|실패| E400_1
    Validation -->|성공| Idempotency
    Idempotency -->|중복/충돌| E409
    Idempotency -->|통과| AccountCheck
    AccountCheck -->|없음| E404
    AccountCheck -->|있음| StatusCheck
    StatusCheck -->|폐쇄| E400_2
    StatusCheck -->|활성| LimitCheck
    LimitCheck -->|초과| E400_3
    LimitCheck -->|통과| BalanceCheck
    BalanceCheck -->|부족| E400_4
    BalanceCheck -->|충분| Process
    Process --> E200
```

---

## 4. 주요 설계 포인트

### 1. Facade 패턴
- **목적**: 여러 서비스를 조합한 복잡한 비즈니스 로직 처리
- **장점**: Service는 단일 책임만 가지고, Facade가 오케스트레이션 담당

### 2. 비관적 락 (Pessimistic Lock)
- **방식**: `@Lock(LockModeType.PESSIMISTIC_WRITE)`
- **이유**: 동시성 환경에서 데이터 일관성 보장

### 3. 데드락 방지
- **전략**: 계좌번호 오름차순 정렬 후 락 획득
- **효과**: 순환 대기 조건 제거

### 4. 트랜잭션 타임아웃
- **설정**: `@Transactional(timeout = 10)`
- **목적**: 무한 대기 방지, 장애 전파 차단

### 5. 격리 수준
- **설정**: `Isolation.SERIALIZABLE`
- **이유**: 최고 수준의 데이터 일관성 보장
