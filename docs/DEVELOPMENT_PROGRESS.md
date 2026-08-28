# Study Gather v2 개발 진행 현황

마지막 확인일: 2026-08-28

이 문서는 실제 코드, 테스트, CI 설정을 기준으로 Section 1~7의 진행 상태를 추적한다.
완료하지 않은 항목은 완료로 표시하지 않는다.

## 상태 표기

- `[x]`: 구현과 현재 단계에 필요한 검증 완료
- `[ ]`: 미구현 또는 완료 기준 미충족
- `부분 완료`: 일부 기반은 있으나 해당 Section의 완료 기준은 충족하지 못함

## 현재 위치 요약

| Section | 상태 | 현재 판단 |
| --- | --- | --- |
| Section 1. 프로젝트 초기화 | 완료 | 로컬 실행 기반과 첫 CI 구성 완료 |
| Section 2. 인증 관통 기능 | 부분 완료 | 백엔드·프론트 관통 흐름과 CI 완료, 프론트 인증 자동화 테스트 미완료 |
| Section 3. 스터디 생성과 신청 | 완료 | OpenAPI 기반 프론트와 브라우저 관통 흐름 및 CI 완료 |
| Section 4. 승인·거절·취소·동시성 | 완료 | 프론트 관리 흐름·동시성·강제 rollback 및 CI 완료 |
| Section 5. API 계약과 테스트 자동화 | 완료 | 계약 검사·Testcontainers·보안·계층·커버리지·프론트 오류 테스트 완료 |
| Section 6. Docker·배포·운영 | 부분 완료 | 환경변수 검증과 백엔드 Docker 이미지 빌드 완료, Compose 통합 대기 |
| Section 7. 측정·문서·포트폴리오 | 미착수 | 최종 산출물과 측정 작업 미진행 |

현재 개발 위치는 **Section 5의 API 계약과 테스트 자동화를 완료하고, Section 6의 필수 환경변수
시작 검증과 백엔드 Docker 이미지 빌드를 완료한 뒤 Compose 통합을 준비하는 단계**이다.

---

## Section 1. 프로젝트 초기화

상태: **완료**

- [x] 현재 폴더를 Git 저장소로 초기화
- [x] 개인 GitHub에 `study-gather-v2` 저장소 생성
- [x] `backend/`, `frontend/`, `docs/`, `scripts/` 디렉터리 생성
- [x] Spring Boot 백엔드 생성
- [x] React + TypeScript + Vite 프론트엔드 생성
- [x] MySQL Docker Compose 구성
- [x] Flyway 최초 마이그레이션 추가
- [x] Actuator health endpoint 구성
- [x] `.env.example` 작성
- [x] 최초 GitHub Actions 구성
- [x] README에 v1과 v2의 관계 작성
- [x] `docs/V1_INTEGRATION_FAILURE.md` 작성

완료 기준 확인:

- [x] 백엔드가 MySQL에 연결됨
- [x] 프론트엔드 개발 서버 실행 확인
- [x] `/actuator/health`의 `UP` 응답 확인
- [x] 백엔드와 프론트엔드 빌드 성공
- [x] GitHub Actions CI 통과

---

## Section 2. 첫 번째 관통 기능 - 회원가입부터 내 정보까지

상태: **백엔드·프론트 관통 흐름 및 CI 완료 / 프론트 인증 자동화 테스트 미완료**

- [x] `users` Flyway 마이그레이션
- [x] User Entity
- [x] UserRepository
- [x] 회원가입 DTO와 validation
- [x] BCrypt 비밀번호 암호화
- [x] 회원가입 Service·Controller
- [x] 로그인 API
- [x] JWT 발급·검증
- [x] Spring Security 설정
- [x] 현재 사용자 조회 API
- [x] 공통 예외 응답과 `@RestControllerAdvice`
- [x] 프론트 회원가입·로그인 화면
- [x] 프론트 API 모듈
- [x] 로그인 → JWT 저장 → 내 정보 조회 실제 연동
- [ ] 프론트를 포함한 인증·보안 자동화 테스트

백엔드 오류 검증:

- [x] 잘못된 입력 `400`
- [x] 인증 정보 없음 `401`
- [x] 변조된 JWT 거부
- [x] 만료된 JWT 거부
- [x] 중복 이메일 `409`

로컬에서 확인한 브라우저 흐름:

```text
회원가입
→ 로그인
→ JWT 저장
→ 내 정보 조회
→ 화면에 사용자 정보 표시
→ 새로고침 후 인증 상태 복원
→ 로그아웃
```

API 경로 주의:

- 최초 계획: `GET /api/v1/users/me`
- 현재 구현: `GET /api/users/me`
- 현재 백엔드 전체가 `/api` prefix를 사용하며 현재 OpenAPI 계약도 이 경로를 기준으로 관리한다.
- `/api/v1` 도입은 호환성 정책이 필요한 별도 변경으로 진행한다.

### Section 2 프론트 완료 계획

첫 프론트 기능 단위에서 다음을 함께 구현한다.

- React Router와 공통 Layout
- 회원가입·로그인 UI
- `src/api` 공통 API client
- JWT 저장과 `Authorization: Bearer <token>` 처리
- AuthContext 또는 동등한 인증 상태 관리
- `/api/users/me`를 이용한 로그인 상태 복원
- 로그아웃 시 클라이언트 토큰 제거
- `400`, `401`, `409` 오류 메시지 표시

완료 게이트:

- [x] 브라우저에서 회원가입부터 내 정보 표시까지 실제 HTTP로 동작
- [x] `npm run lint` 성공
- [x] `npm run build` 성공
- [x] 프론트·백엔드 GitHub Actions 통과

---

## Section 3. 두 번째 관통 기능 - 스터디 생성과 신청

상태: **완료**

- [x] `studies` 테이블
- [x] `study_members` 테이블
- [x] `study_applications` 테이블
- [x] Study Entity와 상태 규칙
- [x] 스터디 생성 API
- [x] 생성 시 개설자를 OWNER 멤버로 동일 트랜잭션에 저장
- [x] 스터디 목록·상세 API
- [x] 참여 신청 API
- [x] DTO validation
- [x] 중복 신청 DB UNIQUE 제약
- [x] 프론트 목록·상세·생성·신청 화면
- [x] 프론트 스터디 수정·모집 마감 화면
- [x] OpenAPI 명세 조회와 JSON 생성 기반
- [x] OpenAPI 기반 프론트 타입 생성

계획보다 추가로 완료한 백엔드 기능:

- [x] 스터디 수정
- [x] 모집 마감
- [x] 스터디 멤버 목록 조회

브라우저에서 확인한 실제 HTTP 흐름:

```text
사용자 A 로그인
→ 스터디 생성
→ 사용자 B 로그인
→ 스터디 목록·상세 조회
→ 참여 신청
```

로컬 완료 기준:

- [x] 위 흐름이 브라우저 화면에서 실제 HTTP로 동작
- [x] 프론트가 OpenAPI 생성 타입을 사용

### Section 3 프론트 완료 계획

- 모집 중 스터디 목록
- 스터디 상세
- 로그인 사용자의 스터디 생성
- 스터디 수정·모집 마감
- 다른 사용자의 참여 신청
- 로딩, 빈 목록, 인증 오류, 중복 신청 상태 구분

프론트 화면은 v1의 배치와 사용자 흐름만 참고한다. 현재 v2에 없는 주제, 장소, 시작일, 종료일,
오픈채팅 링크 필드를 임의로 추가하지 않는다.

완료 게이트:

- [x] 사용자 A 생성 → 사용자 B 신청 흐름을 브라우저에서 검증
- [x] OWNER 멤버와 신청 정보가 API 조회 결과에 일관되게 반영됨을 확인
- [x] 프론트 lint·build와 CI 통과

---

## Section 4. 핵심 기능 - 승인·거절·취소·동시성

상태: **완료**

- [x] 개설자의 신청 목록 조회
- [x] 신청 승인
- [x] 신청 거절
- [x] 신청자 본인의 PENDING 신청 취소
- [x] Entity 상태 전이 메서드
- [x] Service에서 개설자 소유권 검증
- [x] Study 조회에 비관적 쓰기 잠금 적용
- [x] 승인 시 MEMBER 생성과 `approvedCount` 증가
- [x] 승인 중간 실패를 강제로 발생시키는 전체 rollback 테스트
- [x] 권한별 `401`·`403` 테스트
- [x] 20개 동시 승인 테스트
- [x] 개설자 신청 관리 화면 연결
- [x] 내 신청 목록과 PENDING 신청 취소 화면
- [x] 스터디 멤버 목록 화면

현재 승인 트랜잭션:

```text
PENDING 신청 잠금·확인
→ Study 행 비관적 쓰기 잠금
→ 호출자 개설자 권한 확인
→ 잔여 정원 확인
→ 신청 APPROVED 전환
→ StudyMember 생성
→ approvedCount 증가
→ StudyMember 저장
→ commit
```

동시성 검증 결과:

- [x] 마지막 한 자리에 서로 다른 PENDING 신청 20개 준비
- [x] barrier 이후 승인 요청 20개 동시 실행
- [x] 정확히 1건만 APPROVED
- [x] 나머지 19건은 정원 초과로 거부
- [x] 저장된 초과 승인 0건
- [x] `StudyMember` 수와 `approvedCount` 일치
- [x] 위 테스트를 20회 반복
- [x] 비개설자 승인 요청 `403`

`정원 초과 0건`은 정원을 넘어 DB에 저장된 승인 결과가 0건이라는 뜻이다. 마지막 한 자리를 제외한
19개 요청은 정상적으로 정원 초과 예외를 반환한다.

rollback 검증 결과:

- [x] 승인 상태와 `approvedCount` 변경 후 멤버 저장을 강제로 실패시킴
- [x] 트랜잭션 종료 후 application이 PENDING인지 확인
- [x] MEMBER가 생성되지 않았는지 확인
- [x] `approvedCount`가 1로 롤백됐는지 확인

### Section 4 프론트 완료 계획

- 내 참여 신청 목록
- PENDING 신청 취소
- 개설자의 스터디별 신청 목록
- 신청 승인·거절
- 처리 완료 후 서버 목록 재조회
- 스터디 멤버 목록
- `401`, `403`, `409` 상태별 화면 처리

완료 게이트:

- [x] 신청자 화면과 개설자 화면 사이의 전체 승인 흐름 검증
- [x] 승인 후 멤버 목록과 승인 인원 화면 갱신
- [x] 거절·취소 후 상태 화면 갱신
- [x] 프론트 lint·build와 CI 통과
- [x] 강제 중간 실패 rollback 테스트 통과

---

## Section 2~4 프론트 마이그레이션 원칙

참고 프로젝트: `LEEDONGQUE/study-gather-project`

확인 결과 기존 프론트는 React JavaScript로 작성되어 있고, 일부 API는 v1 백엔드 또는 정적 JSON을
사용한다. 현재 계정의 GitHub 기여 기록은 주로 백엔드에 있으므로 팀 프론트 소스를 그대로 복사하지
않는다.

재사용하는 것:

- 화면 배치와 사용자 흐름 아이디어
- 파란색 계열 시각 방향
- 로그인·회원가입 모달 구성 아이디어
- 목록·상세·신청 관리 화면 구조

새로 작성하는 것:

- TypeScript 컴포넌트와 타입
- API client와 endpoint 모듈
- JWT 저장과 인증 상태
- v2 DTO 요청·응답 매핑
- 오류·로딩·빈 상태 처리
- 스타일 코드와 UI 자산

금지 사항:

- v1 `node_modules`, 빌드 결과, `.env` 복사
- v1의 localhost 주소와 요청 필드를 그대로 사용
- 컴포넌트 내부에 API URL 반복 작성
- `any` 사용
- 정적 JSON을 실제 API 결과처럼 사용
- 팀원이 작성한 프론트 코드를 출처 구분 없이 개인 구현으로 표시

---

## Section 2~4 완료한 작업 순서

### 1. `feature/frontend-auth` - 완료

Section 2의 프론트 인증 관통 흐름을 완성한다.

### 2. `feature/api-contract-baseline` - 완료

Section 3에서 미뤄 둔 OpenAPI 생성을 시작하고 프론트 타입 생성 기반을 만든다. 인증 화면에서 먼저
작성한 최소 수동 타입을 생성 타입으로 교체한다.

### 3. `feature/frontend-study` - 완료

생성 타입을 사용해 Section 3의 목록·상세·생성·신청 화면을 완성한다.

### 4. `feature/frontend-application` - 완료

Section 4의 내 신청 목록과 개설자 승인·거절·멤버 화면을 완성한다.

### 5. `test/approval-rollback` - 완료

승인 중간 실패 시 application·member·count 전체 rollback을 검증한다.

### Section 5 진입 조건

다음 항목을 모두 만족하기 전에는 Section 5의 나머지 자동화 작업으로 넘어가지 않는다.

- [x] Section 2 브라우저 인증 관통 흐름 완료
- [x] Section 3 브라우저 스터디 생성·신청 흐름 완료
- [x] Section 4 브라우저 신청 관리 흐름 완료
- [x] OpenAPI 생성 타입을 프론트 API에서 사용
- [x] 승인 강제 실패 rollback 테스트 완료
- [x] 백엔드·프론트 전체 테스트와 CI 통과

---

## Section 5. API 계약과 테스트 자동화

상태: **완료**

- [x] OpenAPI JSON 자동 생성
- [x] `docs/openapi.json` 버전 관리
- [x] `openapi-typescript` 타입 생성
- [x] 프론트 API에서 생성 타입 사용
- [x] OpenAPI 변경 여부를 CI에서 검사
- [x] Testcontainers MySQL 통합 테스트
- [x] Security 테스트 보강
- [x] Repository·Service 테스트 보강
- [x] JaCoCo 적용
- [x] 프론트의 `400`·`401`·`403`·`409` 처리
- [x] DTO 변경 시 CI 실패 실험 기록

자동화 검증 결과:

- [x] 로컬에서 백엔드 실행부터 명세·타입 재생성 및 변경 검사까지 통과
- [x] GitHub Actions의 Backend·Frontend·API Contract 작업 통과
- [x] 계약 자동화 PR이 `main`에 병합됨
- [x] Repository 테스트가 Testcontainers MySQL에서 실행됨
- [x] JWT 보안 경계와 Repository·Service 경계 테스트 통과
- [x] JaCoCo 라인 90%, 브랜치 70% 최소 기준 적용
- [x] DTO 계약 변경 시 CI 실패 감지 실험과 자동 복구 확인
- [x] 프론트 API 오류 처리 테스트 8개와 lint·build 통과

완료한 작업 순서:

1. `feature/openapi-contract-ci` - OpenAPI 계약 변경 CI
2. `test/testcontainers-mysql` - Repository Testcontainers MySQL
3. `test/security-hardening` - JWT 보안 경계 테스트
4. `test/repository-service-hardening` - Repository·Service 경계 테스트
5. `test/jacoco-coverage` - JaCoCo 커버리지 검증
6. `test/openapi-contract-failure` - DTO 계약 변경 실패 실험
7. `test/frontend-api-errors` - 프론트 API 오류 처리 테스트

---

## Section 6. Docker·배포·운영

상태: **운영 기반 확장 진행 중**

- [ ] Docker Compose에서 MySQL과 백엔드 함께 실행 - 현재 MySQL만 실행
- [ ] healthcheck와 DB 준비 순서 설정 - MySQL healthcheck만 존재
- [ ] Flyway clean 실행 검증
- [x] 환경변수 시작 시 검증
- [ ] CORS 설정
- [ ] correlation ID 적용
- [ ] 오류 응답의 traceId와 로그 연결
- [x] Actuator health·liveness·readiness 구성
- [ ] 배포
- [ ] clean clone 재현
- [ ] README 실행 절차 최종 완성

장애 검증:

- [ ] DB 중단
- [x] 잘못된 JWT
- [x] 중복 신청
- [ ] 백엔드 재시작 후 데이터 유지 증거 기록

완료한 운영 기반:

- [x] DB URL·사용자명·비밀번호의 빈 값 검증 구현
- [x] JWT 비밀키의 Base64 형식과 최소 32바이트 검증 구현
- [x] JWT 만료시간의 누락과 최소 1초 검증 구현
- [x] 설정 바인딩 테스트 9개, JWT 테스트 3개, 전체 백엔드 빌드 통과
- [x] PR #13의 Backend·Frontend·API Contract CI 통과 및 `main` 병합

현재 진행 중:

- [x] Java 21 멀티 스테이지 백엔드 Dockerfile 작성
- [x] 비루트 사용자로 백엔드 프로세스 실행 구성
- [x] 로컬 Docker 이미지 빌드 검증

다음 작업 순서:

1. Docker Compose에서 MySQL과 백엔드를 함께 실행한다.
2. MySQL healthcheck를 기준으로 백엔드 시작 순서를 구성한다.
3. Flyway clean 실행과 백엔드 재시작 후 데이터 유지를 검증한다.
4. CORS, correlation ID, 오류 응답 traceId를 각각 기능 단위로 적용한다.

---

## Section 7. 측정·문서·포트폴리오

상태: **미착수**

- [ ] ERD
- [ ] 아키텍처 다이어그램
- [ ] 승인 트랜잭션 시퀀스
- [ ] ADR
- [ ] k6 성능 측정
- [ ] 동시성 테스트 결과 문서
- [ ] GitHub Actions 통과 화면
- [ ] Docker 실행 화면
- [ ] 배포 화면
- [ ] 포트폴리오 문구
- [ ] v1·v2 기여 범위 정리

`docs/V1_INTEGRATION_FAILURE.md`와 CI 자체는 존재하지만, Section 7의 최종 증거물로 정리한 상태는 아니다.
측정하지 않은 수치와 구현하지 않은 기능은 완료형으로 작성하지 않는다.

---

## 문서 갱신 규칙

- 기능 PR이 `main`에 merge된 뒤 관련 체크박스를 갱신한다.
- 테스트를 실행하지 않았다면 완료로 표시하지 않는다.
- 브라우저 완료 기준이 있는 Section은 백엔드 curl 성공만으로 완료 처리하지 않는다.
- README의 진행 상태는 기능 단위마다 수정하지 않고, 주요 Section 완료 시 묶어서 갱신한다.
- 계획과 실제 endpoint가 다르면 둘 중 하나를 숨기지 않고 차이를 기록한다.
