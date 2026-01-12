# API 명세


## Swagger UI

- [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## 공통 사항

- **Base URL**: `/api`
- **Content-Type**: `application/json`
- **계좌번호**: 서버가 발급하는 **12자리 숫자**
- **멱등성**: 거래 API는 `X-Idempotency-Key` 헤더를 사용해 중복 요청을 방지합니다.

### 응답 형식

모든 API는 다음 형식으로 응답합니다:

```json
{
  "success": true/false,
  "data": {...} or null,
  "message": "메시지" or null,
  "timestamp": "2026-01-01T09:00:00"
}
```

---

## 계좌 API

### 계좌 생성
- **POST** `/v1/accounts`
- **Request**: `ownerName` (2-50자), `phoneNumber` (숫자 10-15자리, 국가번호는 + 포함 가능)
- **Response (201)**: `accountId`, `accountNumber` (서버 발급), `ownerName`
- **Error**: 400 (검증 실패)

### 계좌 조회
- **GET** `/v1/accounts/{accountId}`
- **Response (200)**: `accountId`, `accountNumber`, `ownerName`, `balance`, `status`
- **Error**: 404 (계좌 없음)

### 계좌 삭제
- **DELETE** `/v1/accounts/{accountId}`
- **Response (200)**: 성공 메시지
- **Error**: 404 (계좌 없음)
- **동작**: 소프트 삭제 (상태 `CLOSED`, `deletedAt` 기록)

---

## 거래 API

### 입금
- **POST** `/v1/accounts/{accountId}/deposits`
- **Headers**: `X-Idempotency-Key`(필수)
- **Request**: `amount` (양수)
- **Response (200)**: 성공 메시지
- **Error**: 400 (검증 실패, 계좌 폐쇄), 404 (계좌 없음), 409 (멱등성 키 충돌)

### 출금
- **POST** `/v1/accounts/{accountId}/withdrawals`
- **Headers**: `X-Idempotency-Key`(필수)
- **Request**: `amount` (양수)
- **Response (200)**: 성공 메시지
- **정책**: 일 한도 1,000,000원
- **Error**: 400 (검증 실패, 잔액 부족, 한도 초과, 계좌 폐쇄), 404 (계좌 없음), 409 (멱등성 키 충돌)

### 이체
- **POST** `/v1/transfers`
- **Headers**: `X-Idempotency-Key`(필수)
- **Request**: `fromAccountNumber`, `toAccountNumber`, `amount` (양수)
- **Response (200)**: 성공 메시지
- **정책**: 수수료 1%, 일 한도 3,000,000원
- **Error**: 400 (검증 실패, 잔액 부족, 한도 초과, 계좌 폐쇄), 404 (계좌 없음), 409 (멱등성 키 충돌)

---

## 거래내역 조회 API

### 공통 사항
- **Pagination**: Cursor 기반 (최신순)
- **Query**: `cursor` (optional, 이전 페이지 마지막 `transactionId`(TRX 문자열)), `limit` (optional, 기본 50, 최대 200)
- **Response**: `items` (거래 목록), `nextCursor` (다음 페이지 커서, 없으면 null)

### 입금 내역
- **GET** `/v1/accounts/{accountId}/deposits`
- **Response**: `transactionId`, `amount`, `createdAt`

### 출금 내역
- **GET** `/v1/accounts/{accountId}/withdrawals`
- **Response**: `transactionId`, `amount`, `createdAt`

### 보낸 이체 내역
- **GET** `/v1/accounts/{accountId}/sent-transfers`
- **Response**: `transactionId`, `amount`, `fee`, `counterpartyAccountNumber`, `createdAt`

### 받은 이체 내역
- **GET** `/v1/accounts/{accountId}/received-transfers`
- **Response**: `transactionId`, `amount`, `counterpartyAccountNumber`, `createdAt`

---

## 거래 정책 관리 API

> 선택 기능. 기본 정책(DEFAULT)은 애플리케이션 시작 시 자동 생성됩니다.

### 정책 조회
- **GET** `/v1/transaction-policies/{policyType}`
- **Response (200)**: `policyType`, `withdrawDailyLimit`, `transferDailyLimit`, `transferFeeBps`
- **필드 설명**:
  - `withdrawDailyLimit`: 출금 일 한도 (원)
  - `transferDailyLimit`: 이체 일 한도 (원)
  - `transferFeeBps`: 이체 수수료 (BPS 단위, 100 = 1%)

### 정책 수정
- **PUT** `/v1/transaction-policies/{policyType}`
- **Request**: `withdrawDailyLimit` (양수), `transferDailyLimit` (양수), `transferFeeBps` (양수, 최대 10000)
- **Response (200)**: 수정된 정책 정보
- **Error**: 400 (검증 실패)

---

## HTTP 상태 코드

| 코드 | 설명 |
|-----|------|
| 200 | 성공 |
| 201 | 생성 성공 |
| 400 | 입력값 검증 실패, 비즈니스 규칙 위반 |
| 404 | 리소스 없음 |
| 409 | 중복 (계좌번호) |
| 500 | 서버 오류 |
