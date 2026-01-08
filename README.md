# remittance-service

## API 명세

- API 명세 문서: `docs/api.md`

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

## HTTP 테스트 스크립트

IntelliJ HTTP Client로 아래 파일을 실행하면 계좌 API를 빠르게 호출해볼 수 있습니다.

- `scripts/http/account-api.http`

## 입/출/이체 HTTP 테스트 스크립트

- `scripts/http/transaction-api.http`

## 정책 HTTP 테스트 스크립트

- `scripts/http/policy-api.http`

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
