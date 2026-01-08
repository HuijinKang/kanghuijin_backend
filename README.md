# remittance-service

## API 명세(Swagger / OpenAPI)

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 실행 방법 (Docker Compose)

```bash
docker compose up --build
```

- 앱: `http://localhost:8080`
- DB(MySQL): `localhost:3306`
  - db: `remittance`
  - user: `remittance`
  - password: `remittance`

## 계좌 API

### 공통
- **Base URL**: `/api/v1`
- **Content-Type**: `application/json`
- **계좌번호 규칙**: 숫자만 10~14자리 (`^\d{10,14}$`)

### 에러 응답 포맷
에러 발생 시 아래 포맷으로 응답합니다.

```json
{
  "code": "DUPLICATE_ACCOUNT",
  "message": "이미 존재하는 계좌입니다.",
  "timestamp": "2026-01-01T00:00:00Z",
  "details": null
}
```

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
- **멱등성(Idempotent)**: 이미 해지된 계좌에 대해 동일한 DELETE를 재시도해도 **204**를 반환합니다.

#### Error Responses
- **404 Not Found**: 존재하지 않는 `accountId` (`NOT_FOUND`)

## HTTP 테스트 스크립트

IntelliJ HTTP Client로 아래 파일을 실행하면 계좌 API를 빠르게 호출해볼 수 있습니다.

- `scripts/http/account-api.http`

## 테스트

### 단위/슬라이스 테스트
- `@ExtendWith(MockitoExtension.class)` 기반 서비스 단위 테스트
- `@WebMvcTest` 기반 컨트롤러 슬라이스 테스트

### 통합 테스트
- `@SpringBootTest` + Testcontainers(MySQL) 기반 통합 테스트
- 실행 시 **Docker Desktop(또는 Docker Engine)** 이 필요합니다.

### 실행

```bash
./gradlew test
```
