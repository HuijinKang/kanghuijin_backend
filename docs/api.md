# API 명세

## 공통
- **Base URL**: `/api/v1`
- **Content-Type**: `application/json`
- **계좌번호 규칙**: 숫자만 10~14자리 (`^\d{10,14}$`)

## 에러 응답 포맷
에러 발생 시 아래 포맷으로 응답합니다.

```json
{
  "code": "DUPLICATE_ACCOUNT",
  "message": "이미 존재하는 계좌입니다.",
  "timestamp": "2026-01-01T00:00:00Z",
  "details": null
}
```

## 계좌 API

### 1) 계좌 생성
- **POST** `/api/v1/accounts`

#### Request Body
```json
{
  "accountNumber": "123456789012",
  "ownerName": "홍길동"
}
```

#### Response (201 Created)
```json
{
  "accountId": 1,
  "accountNumber": "123456789012",
  "ownerName": "홍길동"
}
```

#### Error Responses
- **400 Bad Request**: 요청 값 검증 실패(예: 계좌번호 형식 오류, 빈 값)
- **409 Conflict**: `accountNumber` 중복 (`DUPLICATE_ACCOUNT`)

### 2) 계좌 삭제(해지)
- **DELETE** `/api/v1/accounts/{accountId}`

#### Response
- **204 No Content**

#### 동작 정책
- **소프트 삭제(해지)**: 계좌는 물리 삭제하지 않고 상태를 `CLOSED`로 변경하고 `deletedAt`을 기록합니다.

#### Error Responses
- **404 Not Found**: 존재하지 않는 `accountId` (`NOT_FOUND`)

## 입금/출금/이체 API

### 1) 입금
- **POST** `/api/v1/accounts/{accountId}/deposits`

#### Request Body
```json
{
  "amount": 2000
}
```

#### Response
- **204 No Content**

### 2) 출금
- **POST** `/api/v1/accounts/{accountId}/withdrawals`

#### 정책
- **일 한도**: DB의 `policy_configs.withdraw_daily_limit` 값 (기본값: 1,000,000)
- **집계 기준**: UTC 날짜(00:00:00 ~ 23:59:59 UTC)

#### Request Body
```json
{
  "amount": 500
}
```

#### Response
- **204 No Content**

#### Error Responses
- **400 Bad Request**: 일 한도 초과 (`DAILY_LIMIT_EXCEEDED`), 잔액 부족 (`INSUFFICIENT_BALANCE`), 해지 계좌 (`ACCOUNT_CLOSED`)

### 3) 이체
- **POST** `/api/v1/transfers`

#### 정책
- **수수료**: DB의 `policy_configs.transfer_fee_bps` (기본값: 100 bps = 1.00%)
- **일 한도**: DB의 `policy_configs.transfer_daily_limit` 값 (기본값: 3,000,000)
- **집계 기준**: UTC 날짜(00:00:00 ~ 23:59:59 UTC)

#### Request Body
```json
{
  "fromAccountNumber": "123456789012",
  "toAccountNumber": "999999999999",
  "amount": 1000
}
```

#### Response
- **204 No Content**

#### Error Responses
- **400 Bad Request**: 일 한도 초과 (`DAILY_LIMIT_EXCEEDED`), 잔액 부족 (`INSUFFICIENT_BALANCE`), 해지 계좌 (`ACCOUNT_CLOSED`)
- **404 Not Found**: 계좌를 찾을 수 없음 (`NOT_FOUND`)

## 정책(한도/수수료) 관리 API (선택)

> 정책 API는 **필수 요구사항이 아닌 옵션 기능**입니다.  
> **기본 정책(DEFAULT)은 애플리케이션 시작 시 `data.sql`로 자동 생성/업데이트**되므로, 정책 API를 호출하지 않아도 입금/출금/이체 기능을 실행할 수 있습니다.

### 정책 조회
- **GET** `/api/v1/policies/{policyType}`

### 정책 Upsert
- **PUT** `/api/v1/policies/{policyType}`

