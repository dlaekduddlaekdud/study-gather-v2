# Scripts

반복 실행이 필요한 개발 및 검증 스크립트를 보관하는 디렉터리입니다.

## OpenAPI 계약 변경 검사

`check-openapi-contract.sh`는 현재 백엔드를 실행해 OpenAPI JSON과 프론트 TypeScript 타입을
재생성한 뒤, 커밋된 파일과 차이가 있는지 검사합니다.

프로젝트 루트에서 MySQL과 환경변수를 준비한 다음 실행합니다.

```bash
docker compose up -d mysql
set -a
source .env
set +a
./scripts/check-openapi-contract.sh
```

정상 상태이면 다음 메시지를 출력합니다.

```text
OpenAPI 계약과 생성 타입이 최신 상태입니다.
```

검사가 실패하면서 `docs/openapi.json` 또는
`frontend/src/api/generated/schema.ts`가 변경되었다면 백엔드 API 변경이 생성 산출물에 반영되지
않은 상태입니다. 변경 내용을 검토한 뒤 두 파일을 현재 기능과 함께 커밋합니다.

스크립트는 자체적으로 백엔드를 실행하므로 8080 포트에서 실행 중인 백엔드가 없어야 합니다.
GitHub Actions의 `API Contract` 작업도 같은 스크립트를 사용합니다.
