# Study Gather v2

스터디 모집, 참여 신청, 승인 과정을 구현하며 API 계약, 트랜잭션, 동시성 제어를 학습하는 프로젝트입니다.

## v1과 v2의 관계

v1은 팀 프로젝트에서 스터디 모집과 사용자 기능을 구현한 첫 번째 버전입니다. 기능별 코드는 존재했지만 프론트엔드와 백엔드의 요청 필드 및 API 주소를 수동으로 맞추는 과정에서 통합 안정성과 재현성이 부족했습니다.

v2는 v1 코드를 단순 복사하지 않고, 확인된 문제를 다시 설계하고 검증하기 위한 개인 리빌드 프로젝트입니다.

- Flyway로 DB 스키마 변경 이력 관리
- DTO 검증과 일관된 API 응답 적용
- Controller, Service, Repository 책임 분리
- React와 TypeScript로 API 계약 오류 조기 발견
- MySQL 기반 통합 테스트와 GitHub Actions 자동 검증
- 승인 인원 동시성 문제를 DB 잠금과 트랜잭션으로 검증할 계획

v1에서 확인한 문제와 v2의 개선 방향은 [V1 통합 실패 분석](docs/V1_INTEGRATION_FAILURE.md)에 기록합니다.

## 현재 진행 상태

- Section 1: 백엔드, 프론트엔드, MySQL, Flyway, Actuator, CI 기반 환경 구성 완료
- Section 2: 백엔드 인증과 프론트 회원가입·로그인·JWT 복원·내 정보 조회 로컬 관통 흐름 완료
- Section 3: 스터디 생성·조회·수정·모집 마감·참여 신청 백엔드 구현 완료
- Section 4: 승인·거절·취소와 비관적 잠금 기반 동시 승인 제어 백엔드 핵심 구현 완료
- 인증 통합 테스트와 GitHub Actions의 MySQL 기반 검증 완료
- 다음 작업: OpenAPI 타입 생성 기반과 스터디 프론트 화면 구현

완료하지 않은 기능은 완료된 것으로 표시하지 않습니다.

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| Backend | Java 21, Spring Boot 4.1, Gradle |
| Security | Spring Security, JWT, BCrypt |
| Database | MySQL 8.4, JPA, Flyway |
| Frontend | React, TypeScript, Vite |
| Infrastructure | Docker Compose, GitHub Actions |

## 디렉터리 구조

```text
study-gather-v2/
├── backend/              Spring Boot 백엔드
├── frontend/             React 프론트엔드
├── docs/                 설계 및 실험 기록
├── scripts/              반복 작업용 스크립트 안내
├── docker-compose.yml    로컬 MySQL 구성
└── .env.example          환경변수 예시
```

## 로컬 실행

### 1. 환경변수 준비

```bash
cp .env.example .env
openssl rand -base64 32
```

생성된 Base64 문자열로 `.env`의 `JWT_SECRET` 값을 교체합니다. `.env.example`의 값은 형식 확인용
가짜 값입니다. `.env`와 실제 secret은 Git에 커밋하지 않습니다.

### 2. MySQL 실행

```bash
docker compose up -d mysql
docker compose ps
```

`study-gather-mysql`의 상태가 `healthy`이면 정상입니다. 호스트에서는 `3307` 포트로 접속합니다.

### 3. 백엔드 실행

```bash
cd backend
set -a
source ../.env
set +a
./gradlew bootRun
```

서버 상태는 다음 요청으로 확인합니다.

```bash
curl http://localhost:8080/actuator/health
```

`status`가 `UP`이면 정상입니다.

### 4. 프론트엔드 실행

```bash
cd frontend
npm install
npm run dev
```

개발 서버는 기본적으로 `http://localhost:5173`에서 실행됩니다.

## 빌드와 테스트

백엔드:

```bash
cd backend
set -a
source ../.env
set +a
./gradlew clean test
```

프론트엔드:

```bash
cd frontend
npm run lint
npm run build
```

GitHub Actions에서도 MySQL 서비스 컨테이너를 실행한 뒤 백엔드와 프론트엔드를 각각 검증합니다.

## 현재 제공 API

### 회원가입

```http
POST /api/auth/signup
Content-Type: application/json
```

```json
{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "스터디원"
}
```

성공 시 `201 Created`, 중복 이메일이면 `409 Conflict`, 입력값이 잘못되면 `400 Bad Request`를 반환하도록 구성합니다.

### 로그인

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

성공 시 `200 OK`와 JWT access token을 반환합니다.

```json
{
  "success": true,
  "message": "로그인에 성공했습니다.",
  "data": {
    "accessToken": "eyJ..."
  }
}
```

이메일이 없거나 비밀번호가 일치하지 않으면 계정 존재 여부를 구분하지 않고 `401 Unauthorized`를
반환합니다.

### 내 정보 조회

```http
GET /api/users/me
Authorization: Bearer <access-token>
```

성공 시 `200 OK`와 현재 사용자의 정보를 반환합니다. Entity와 비밀번호 해시는 응답에 노출하지 않습니다.

```json
{
  "success": true,
  "message": "내 정보를 조회했습니다.",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "nickname": "스터디원",
    "role": "USER"
  }
}
```

토큰이 없거나 변조·만료된 경우 `401 Unauthorized`, 토큰의 사용자 ID가 DB에 없으면
`404 Not Found`를 반환합니다.

## 인증 방식과 현재 범위

로그인 성공 시 사용자 ID를 `sub`, 역할을 `role` claim으로 갖는 JWT access token을 발급합니다.
클라이언트는 보호 API 요청에 다음 헤더를 전달해야 합니다.

```http
Authorization: Bearer <access-token>
```

JWT 필터가 서명, 만료 시간, 사용자 ID와 역할 형식을 검증한 뒤 Spring Security의
`SecurityContext`에 인증 정보를 저장합니다. 서버 세션은 생성하지 않습니다.

현재는 access token만 구현했습니다. refresh token, 로그아웃을 통한 토큰 즉시 폐기, 회원 탈퇴 후
기존 토큰 차단은 아직 구현하지 않았습니다.

GitHub Actions는 Repository Secret의 `JWT_SECRET`을 사용하며, MySQL 서비스 컨테이너에서 백엔드
테스트와 빌드를 실행합니다.
