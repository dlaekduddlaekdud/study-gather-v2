# OpenAPI 계약 변경 감지 실험

## 목적

백엔드 DTO의 API 제약 조건이 변경됐지만 OpenAPI JSON과 프론트 TypeScript 타입을 함께
갱신하지 않은 경우, CI의 `API Contract` 작업이 변경 누락을 감지하는지 확인한다.

## 실험 환경

- 실험일: 2026-08-28
- 브랜치: `test/openapi-contract-failure`
- 데이터베이스: Docker Compose의 MySQL 8.4.11
- 검사 스크립트: `scripts/check-openapi-contract.sh`
- 재현 스크립트: `scripts/test-openapi-contract-detection.sh`

## 실험 방법

재현 스크립트가 다음 작업을 자동으로 수행한다.

1. `CreateStudyRequest.title`의 최대 길이를 100자에서 101자로 임시 변경한다.
2. 백엔드를 실행하고 `/v3/api-docs`에서 OpenAPI JSON을 다시 생성한다.
3. 생성된 JSON을 기준으로 프론트 TypeScript 타입을 다시 생성한다.
4. 커밋된 계약 산출물과 새 산출물의 Git 차이를 검사한다.
5. 실험에 사용한 DTO와 생성 산출물을 원래 상태로 복구한다.

## 실행 명령

```bash
docker compose up -d mysql
set -a
source .env
set +a
./scripts/test-openapi-contract-detection.sh
git status --short
```

## 관찰 결과

계약 검사기는 DTO 제약 조건 변경으로 발생한 `docs/openapi.json`의 차이를 감지하고 실패했다.

```text
백엔드 API와 커밋된 OpenAPI 산출물이 일치하지 않습니다.
아래 변경 파일을 확인하고 함께 커밋해 주세요.
 docs/openapi.json | 2 +-
 1 file changed, 1 insertion(+), 1 deletion(-)

DTO 변경에 따른 OpenAPI 계약 차이를 예상대로 감지했습니다.
실험에 사용한 DTO와 생성 산출물은 원래 상태로 복구됩니다.
```

실험 종료 후 `CreateStudyRequest.java`, `docs/openapi.json`,
`frontend/src/api/generated/schema.ts`에는 실험으로 인한 변경이 남지 않았다.

## 결론

백엔드 DTO의 OpenAPI 계약이 변경됐는데 생성 산출물을 함께 반영하지 않으면 CI가 실패한다.
따라서 백엔드 계약과 프론트 타입이 서로 다른 상태로 `main` 브랜치에 병합되는 것을 방지할 수 있다.
