# 시스템 플로우 차트

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
    participant AccService as AccountService
    participant TxService as TransactionService
    participant PolicyService as TransactionPolicyService
    participant DB as Database
    
    Client->>Controller: POST /v1/transfers
    Note over Controller: 요청 검증
    Controller->>Facade: transfer(command)
    
    Note over Facade: @Transactional(SERIALIZABLE, timeout=10)
    
    Facade->>Facade: 계좌번호 오름차순 정렬
    Note over Facade: 데드락 방지
    
    Facade->>AccService: findByAccountNumberForUpdate(작은번호)
    AccService->>DB: SELECT ... FOR UPDATE
    DB-->>AccService: 계좌1 (락 획득)
    
    Facade->>AccService: findByAccountNumberForUpdate(큰번호)
    AccService->>DB: SELECT ... FOR UPDATE
    DB-->>AccService: 계좌2 (락 획득)
    
    Facade->>AccService: validateAccountActive(송금계좌)
    Facade->>AccService: validateAccountActive(수취계좌)
    
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
    TxService->>DB: INSERT transactions (2건)
    
    Note over Facade: 트랜잭션 커밋
    
    Facade-->>Controller: 완료
    Controller-->>Client: 200 OK
```

---

## 3. 동시성 제어 플로우

### 데드락 방지 메커니즘

```mermaid
graph TB
    subgraph scenario1 [시나리오: A→B 이체]
        T1Start[트랜잭션 1 시작]
        T1Compare[계좌번호 비교<br/>A vs B]
        T1LockA[A 계좌 락 획득]
        T1LockB[B 계좌 락 획득]
        T1Process[이체 처리]
        T1Commit[커밋]
        
        T1Start --> T1Compare
        T1Compare -->|A < B| T1LockA
        T1LockA --> T1LockB
        T1LockB --> T1Process
        T1Process --> T1Commit
    end
    
    subgraph scenario2 [시나리오: B→A 이체]
        T2Start[트랜잭션 2 시작]
        T2Compare[계좌번호 비교<br/>B vs A]
        T2LockA[A 계좌 락 획득]
        T2LockB[B 계좌 락 획득]
        T2Process[이체 처리]
        T2Commit[커밋]
        
        T2Start --> T2Compare
        T2Compare -->|A < B| T2LockA
        T2LockA --> T2LockB
        T2LockB --> T2Process
        T2Process --> T2Commit
    end
    
    style T1LockA fill:#e1f5e1
    style T2LockA fill:#e1f5e1
    style T1LockB fill:#fff3cd
    style T2LockB fill:#fff3cd
```

**핵심 원리**: 모든 트랜잭션이 **동일한 순서**(계좌번호 오름차순)로 락을 획득하므로 순환 대기가 발생하지 않음

### 락 타임아웃 처리

```mermaid
sequenceDiagram
    participant T1 as 트랜잭션 1
    participant T2 as 트랜잭션 2
    participant DB as Database
    
    T1->>DB: 계좌 A 락 획득
    Note over DB: A 락 보유
    
    T2->>DB: 계좌 A 락 시도
    Note over T2,DB: 대기 중...
    
    Note over T1: 처리 중 (최대 10초)
    
    alt 10초 이내 완료
        T1->>DB: 트랜잭션 커밋
        Note over DB: A 락 해제
        DB->>T2: 락 획득 성공
        T2->>DB: 처리 및 커밋
    else 10초 초과
        Note over T2: TransactionTimedOutException
        T2->>T2: 롤백 및 예외 반환
    end
```

---

## 4. 거래 타입별 처리 플로우 비교

```mermaid
graph LR
    subgraph deposit [입금]
        D1[요청 수신]
        D2[계좌 락 획득]
        D3[잔액 증가]
        D4[거래 기록]
        D5[커밋]
        
        D1 --> D2 --> D3 --> D4 --> D5
    end
    
    subgraph withdraw [출금]
        W1[요청 수신]
        W2[일 한도 체크]
        W3[계좌 락 획득]
        W4[잔액 감소]
        W5[거래 기록]
        W6[커밋]
        
        W1 --> W2 --> W3 --> W4 --> W5 --> W6
    end
    
    subgraph transfer [이체]
        T1[요청 수신]
        T2[계좌번호 정렬]
        T3[두 계좌 락 획득]
        T4[일 한도 체크]
        T5[수수료 계산]
        T6[잔액 변경]
        T7[거래 기록 2건]
        T8[커밋]
        
        T1 --> T2 --> T3 --> T4 --> T5 --> T6 --> T7 --> T8
    end
```

### 특징 비교

| 구분 | 입금 | 출금 | 이체 |
|-----|------|------|------|
| 락 대상 | 1개 계좌 | 1개 계좌 | 2개 계좌 (순서 보장) |
| 한도 체크 | 없음 | 일 1,000,000원 | 일 3,000,000원 |
| 수수료 | 없음 | 없음 | 1% (BPS 100) |
| 거래 기록 | 1건 | 1건 | 2건 (송금/수취) |
| 격리 수준 | SERIALIZABLE | SERIALIZABLE | SERIALIZABLE |
| 타임아웃 | 10초 | 10초 | 10초 |

---

## 5. 데이터 흐름도

### 이체 데이터 흐름

```mermaid
graph LR
    Request[이체 요청]
    Validate[입력 검증]
    Sort[계좌번호 정렬]
    Lock1[계좌1 락]
    Lock2[계좌2 락]
    CheckStatus[상태 검증]
    GetPolicy[정책 조회]
    CheckLimit[한도 체크]
    CalcFee[수수료 계산]
    UpdateBalance[잔액 변경]
    RecordTx[거래 기록]
    Commit[커밋]
    Response[응답]
    
    Request --> Validate
    Validate --> Sort
    Sort --> Lock1
    Lock1 --> Lock2
    Lock2 --> CheckStatus
    CheckStatus --> GetPolicy
    GetPolicy --> CheckLimit
    CheckLimit --> CalcFee
    CalcFee --> UpdateBalance
    UpdateBalance --> RecordTx
    RecordTx --> Commit
    Commit --> Response
    
    style Request fill:#e3f2fd
    style Response fill:#e8f5e9
    style Lock1 fill:#fff3cd
    style Lock2 fill:#fff3cd
    style Commit fill:#f3e5f5
```

---

## 6. 에러 처리 플로우

```mermaid
graph TB
    Start[요청 시작]
    Validation{입력 검증}
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
    E200[200 OK]
    
    Start --> Validation
    Validation -->|실패| E400_1
    Validation -->|성공| AccountCheck
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

## 주요 설계 포인트

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
