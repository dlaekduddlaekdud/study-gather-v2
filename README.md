# Study Gather v2

스터디 모집, 참여 신청, 승인 과정을 구현하며 API 계약, 트랜잭션, 동시성 제어와 운영 기반을
검증하는 개인 리빌드 프로젝트입니다.

## v1과 v2의 관계

v1은 팀 프로젝트에서 스터디 모집과 사용자 기능을 구현한 첫 번째 버전입니다. 기능별 코드는
존재했지만 프론트엔드와 백엔드의 요청 필드 및 API 주소를 수동으로 맞추는 과정에서 통합 안정성과
재현성이 부족했습니다.

v2는 v1 코드를 복사하지 않고 확인된 문제를 다시 설계하고 검증합니다.

- Flyway 기반 DB 스키마 이력 관리
- DTO validation과 일관된 API 응답
- Controller → Service → Repository 책임 분리
- OpenAPI 기반 프론트 TypeScript 타입 생성
- Testcontainers MySQL 통합 테스트
- 비관적 잠금과 트랜잭션을 이용한 동시 승인 제어
- Docker Compose, healthcheck, CORS, correlation ID 운영 기반

상세 분석은 [V1 통합 실패 분석](docs/V1_INTEGRATION_FAILURE.md), 실제 진행 상태는
[개발 진행 현황](docs/DEVELOPMENT_PROGRESS.md)에서 확인할 수 있습니다.

## 현재 진행 상태

- Section 1: 프로젝트 초기화 완료
- Section 2: 백엔드·프론트 인증 관통 흐름과 CI 완료, 프론트 인증 자동화 테스트 미완료
- Section 3: 스터디 생성·조회·수정·마감·참여 신청 완료
- Section 4: 승인·거절·취소·동시성·rollback 검증 완료
- Section 5: OpenAPI 계약과 테스트 자동화 완료
- Section 6: 로컬 Docker·운영 기반 완료, clean clone과 배포 검증 진행 중

완료하지 않은 기능은 완료된 것으로 표시하지 않습니다.

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| Backend | Java 21, Spring Boot 4.1, Gradle |
| Security | Spring Security, JWT, BCrypt |
| Database | MySQL 8.4, JPA, Flyway |
| Frontend | React, TypeScript, Vite |
| Contract | springdoc-openapi, openapi-typescript |
| Test | JUnit 5, MockMvc, Testcontainers, Vitest, JaCoCo |
| Infrastructure | Docker Compose, GitHub Actions |

## 디렉터리 구조

```text
study-gather-v2/
├── backend/              Spring Boot 백엔드와 Dockerfile
├── frontend/             React·TypeScript 프론트엔드
├── docs/                 진행 기록과 OpenAPI 계약
├── scripts/              OpenAPI 계약 자동화 스크립트
├── docker-compose.yml    MySQL·백엔드 로컬 실행 구성
└── .env.example          필수 환경변수 예시
```

## 사전 준비

- Docker Desktop과 Docker Compose
- 프론트 개발·검증 시 Node.js 24와 npm
- 백엔드를 컨테이너 밖에서 실행하거나 테스트할 때 Java 21

버전을 확인합니다.

```bash
docker --version
docker compose version
node --version
java -version
```

## 빠른 시작

### 1. 저장소와 환경변수 준비

```bash
git clone https://github.com/dlaekduddlaekdud/study-gather-v2.git
cd study-gather-v2
cp .env.example .env
openssl rand -base64 32
```

생성된 Base64 문자열로 `.env`의 `JWT_SECRET` 값을 교체합니다. `.env.example`의 secret은 형식
확인용 가짜 값입니다. `.env`와 실제 secret은 Git에 커밋하지 않습니다.

주요 환경변수:

| 변수 | 용도 |
| --- | --- |
| `MYSQL_*` | Compose MySQL 초기 데이터베이스와 계정 |
| `DB_*` | 컨테이너 밖에서 실행하는 백엔드의 MySQL 연결 정보 |
| `JWT_SECRET` | Base64 형식의 32바이트 이상 JWT 서명 키 |
| `JWT_ACCESS_TOKEN_EXPIRATION` | `1h` 같은 access token 만료 시간 |
| `CORS_ALLOWED_ORIGINS` | 쉼표로 구분한 허용 프론트 Origin |

### 2. MySQL과 백엔드 실행

```bash
docker compose up -d --build
docker compose ps
```

정상이면 다음 두 컨테이너가 `healthy`가 됩니다.

```text
study-gather-mysql     ... (healthy)
study-gather-backend   ... (healthy)
```

Compose 내부에서 백엔드는 `mysql:3306`으로 DB에 연결합니다. 호스트에서 MySQL에 직접 연결할 때는
`127.0.0.1:3307`을 사용합니다.

서버 상태를 확인합니다.

```bash
curl localhost:8080/actuator/health
curl localhost:8080/actuator/health/liveness
curl localhost:8080/actuator/health/readiness
```

정상 응답은 `{"status":"UP"}`입니다.

### 3. 프론트엔드 실행

프론트엔드는 현재 Compose에 포함하지 않으며 별도 Vite 개발 서버로 실행합니다.

```bash
cd frontend
npm ci
npm run dev
```

브라우저에서 `http://localhost:5173`으로 접속합니다. Vite 개발 서버는 `/api` 요청을
`http://localhost:8080`으로 프록시합니다.

## 컨테이너 밖에서 백엔드 실행

IntelliJ 또는 Gradle로 백엔드를 실행하려면 MySQL만 컨테이너로 실행하고 `.env`를 현재 셸에
주입합니다. Compose 백엔드와 동시에 실행하면 `8080` 포트가 충돌합니다.

```bash
docker compose stop backend
docker compose up -d mysql

cd backend
set -a
source ../.env
set +a
./gradlew bootRun
```

IntelliJ에서 실행할 때도 `.env`의 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`,
`JWT_ACCESS_TOKEN_EXPIRATION`, `CORS_ALLOWED_ORIGINS`를 Run Configuration에 설정해야 합니다.

## 빌드와 테스트

### 백엔드

Testcontainers 테스트가 있으므로 Docker가 실행 중이어야 합니다.

```bash
cd backend
set -a
source ../.env
set +a
./gradlew check
```

`check`는 단위·통합 테스트, JaCoCo 리포트와 최소 커버리지 기준을 함께 검증합니다.

### 프론트엔드

```bash
cd frontend
npm ci
npm test
npm run lint
npm run build
```

### OpenAPI 계약

계약 검사 스크립트가 자체 백엔드를 `8080` 포트에서 실행하므로 Compose 백엔드를 먼저 중지합니다.

```bash
docker compose stop backend
set -a
source .env
set +a
./scripts/check-openapi-contract.sh
```

정상 결과:

```text
OpenAPI 계약과 생성 타입이 최신 상태입니다.
```

검사가 끝난 뒤 Compose 백엔드를 다시 실행할 수 있습니다.

```bash
docker compose up -d backend
```

## 운영 확인

### Health endpoint

| 경로 | 역할 |
| --- | --- |
| `/actuator/health` | 전체 상태 |
| `/actuator/health/liveness` | 애플리케이션 프로세스 생존 여부 |
| `/actuator/health/readiness` | DB를 포함한 요청 처리 준비 여부 |

MySQL이 중단되면 readiness와 DB 의존 API가 실패하고, MySQL 복구 후 백엔드를 재시작하지 않아도
연결과 readiness가 자동으로 복구됩니다.

### Correlation ID와 오류 traceId

요청에 `X-Correlation-ID`가 있으면 그대로 사용하고, 없거나 형식이 잘못되면 UUID를 생성합니다.
같은 값이 응답 헤더, MDC 로그, 오류 응답 `traceId`에 연결됩니다.

```bash
curl -i localhost:8080/api/users/me \
  -H 'X-Correlation-ID: readme-check-123'
```

오류 응답 예시:

```json
{
  "success": false,
  "message": "인증이 필요합니다.",
  "data": null,
  "traceId": "readme-check-123"
}
```

### CORS

기본 허용 Origin은 `http://localhost:5173`입니다. 다른 프론트 주소를 사용할 때는
`CORS_ALLOWED_ORIGINS`를 변경한 뒤 백엔드를 다시 시작합니다.

## 데이터와 컨테이너 관리

컨테이너만 중지하며 MySQL 데이터는 유지합니다.

```bash
docker compose stop
```

컨테이너와 네트워크를 제거해도 named volume은 기본적으로 유지됩니다.

```bash
docker compose down
```

`docker compose down -v`는 MySQL 데이터를 삭제합니다. 스키마 초기화가 명확히 필요한 경우가 아니면
사용하지 않습니다. Flyway clean 검증은 실제 개발 DB가 아닌 Testcontainers의 격리된 MySQL에서
실행합니다.

## 주요 API

| 기능 | Method | Path | 인증 |
| --- | --- | --- | --- |
| 회원가입 | POST | `/api/auth/signup` | 불필요 |
| 로그인 | POST | `/api/auth/login` | 불필요 |
| 내 정보 | GET | `/api/users/me` | 필요 |
| 모집 중 스터디 목록 | GET | `/api/studies` | 불필요 |
| 스터디 상세 | GET | `/api/studies/{studyId}` | 불필요 |
| 스터디 생성 | POST | `/api/studies` | 필요 |
| 스터디 수정 | PATCH | `/api/studies/{studyId}` | 개설자 |
| 모집 마감 | POST | `/api/studies/{studyId}/close` | 개설자 |
| 참여 신청 | POST | `/api/studies/{studyId}/applications` | 필요 |
| 내 신청 목록 | GET | `/api/applications/me` | 필요 |
| 신청 취소 | POST | `/api/applications/{applicationId}/cancel` | 신청자 |
| 신청 승인·거절 | POST | `/api/applications/{applicationId}/approve`, `/reject` | 개설자 |
| 스터디 멤버 | GET | `/api/studies/{studyId}/members` | 필요 |

정확한 요청·응답 계약은 [OpenAPI JSON](docs/openapi.json)을 기준으로 관리합니다.

## 문제 해결

### `localhost:5173` 연결 거부

Compose는 프론트엔드를 실행하지 않습니다. `frontend`에서 `npm run dev`를 실행합니다.

### `JWT_ACCESS_TOKEN_EXPIRATION` 바인딩 실패

IntelliJ 또는 Gradle 실행 프로세스에 `.env`가 주입되지 않은 상태입니다. 필수 환경변수를 Run
Configuration에 추가하거나 셸에서 `source .env` 후 실행합니다.

### `8080` 포트 사용 중

Compose 백엔드와 IntelliJ 백엔드를 동시에 실행하지 않습니다. 사용할 실행 방식 하나만 남깁니다.

### 백엔드가 `health: starting`으로 표시됨

MySQL healthcheck와 Flyway·JPA 초기화가 끝날 때까지 기다린 뒤 `docker compose ps`를 다시 확인합니다.

## CI

GitHub Actions는 다음 작업을 독립적으로 검증합니다.

- Backend: MySQL 기반 테스트·빌드와 JaCoCo
- Frontend: 테스트·lint·TypeScript 빌드
- API Contract: OpenAPI JSON과 생성 TypeScript 타입 변경 검사
