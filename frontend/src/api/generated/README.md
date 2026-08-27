# OpenAPI 생성 타입

이 디렉터리의 `schema.ts`는 백엔드 OpenAPI 명세에서 자동 생성한다.
생성된 타입을 직접 수정하지 않는다.

백엔드가 `localhost:8080`에서 실행 중일 때 프론트엔드 디렉터리에서 다음 명령으로 갱신한다.

```bash
npm run openapi:generate
```

명령은 다음 작업을 순서대로 수행한다.

1. `/v3/api-docs` 응답을 `docs/openapi.json`에 저장한다.
2. 저장한 명세를 `src/api/generated/schema.ts`로 변환한다.
