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
- 승인 인원 동시성 문제를 DB 잠금과 트랜잭션으로 해결

v1에서 확인한 문제와 v2의 개선 방향은 [V1 통합 실패 분석](docs/V1_INTEGRATION_FAILURE.md)에 기록합니다.

## 현재 진행 상태

- Section 1: 백엔드, 프론트엔드, MySQL, Flyway, Actuator, CI 기반 환경 구성 완료
- Section 2: 회원가입 API, BCrypt 암호화, 공통 응답 및 예외 처리 구현 완료
- 다음 작업: 로그인 API 구현

완료하지 않은 기능은 완료된 것으로 표시하지 않습니다.

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| Backend | Java 21, Spring Boot 4.1, Gradle |
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
```

`.env`는 로컬 개발용이며 Git에 커밋하지 않습니다.

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
./gradlew test
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

