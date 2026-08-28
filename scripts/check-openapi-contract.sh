#!/usr/bin/env bash

set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
project_dir="$(cd "$script_dir/.." && pwd)"
server_log="$(mktemp "${TMPDIR:-/tmp}/study-gather-openapi.XXXXXX.log")"
backend_pid=""
readiness_timeout_seconds=180

cleanup() {
  if [[ -n "$backend_pid" ]] && kill -0 "$backend_pid" 2>/dev/null; then
    kill "$backend_pid" 2>/dev/null || true
    wait "$backend_pid" 2>/dev/null || true
  fi
  rm -f "$server_log"
}

trap cleanup EXIT

required_variables=(
  DB_URL
  DB_USERNAME
  DB_PASSWORD
  JWT_SECRET
  JWT_ACCESS_TOKEN_EXPIRATION
)

for variable_name in "${required_variables[@]}"; do
  if [[ -z "${!variable_name:-}" ]]; then
    echo "필수 환경변수가 없습니다: $variable_name" >&2
    exit 1
  fi
done

if curl --fail --silent --output /dev/null --max-time 2 \
  http://localhost:8080/actuator/health; then
  echo "8080 포트에서 이미 백엔드가 실행 중입니다. 종료한 뒤 다시 실행해 주세요." >&2
  exit 1
fi

(
  cd "$project_dir/backend"
  exec ./gradlew bootRun --no-daemon
) >"$server_log" 2>&1 &
backend_pid=$!

for ((attempt = 1; attempt <= readiness_timeout_seconds; attempt++)); do
  if curl --fail --silent --output /dev/null \
    http://localhost:8080/actuator/health/readiness; then
    break
  fi

  if ! kill -0 "$backend_pid" 2>/dev/null; then
    echo "OpenAPI 문서를 생성할 백엔드가 실행 중 종료되었습니다." >&2
    tail -n 100 "$server_log" >&2
    exit 1
  fi

  if [[ "$attempt" -eq "$readiness_timeout_seconds" ]]; then
    echo "${readiness_timeout_seconds}초 안에 백엔드가 준비되지 않았습니다." >&2
    tail -n 100 "$server_log" >&2
    exit 1
  fi

  sleep 1
done

(
  cd "$project_dir/frontend"
  npm run openapi:generate
)

contract_files=(
  docs/openapi.json
  frontend/src/api/generated/schema.ts
)

if ! git -C "$project_dir" diff --quiet -- "${contract_files[@]}"; then
  echo "백엔드 API와 커밋된 OpenAPI 산출물이 일치하지 않습니다." >&2
  echo "아래 변경 파일을 확인하고 함께 커밋해 주세요." >&2
  git -C "$project_dir" diff --stat -- "${contract_files[@]}" >&2
  exit 1
fi

echo "OpenAPI 계약과 생성 타입이 최신 상태입니다."
