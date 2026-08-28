#!/usr/bin/env bash

set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
project_dir="$(cd "$script_dir/.." && pwd)"
target_dto="backend/src/main/java/com/studygather/study/dto/request/CreateStudyRequest.java"
contract_files=(
  docs/openapi.json
  frontend/src/api/generated/schema.ts
)
backup_dir="$(mktemp -d "${TMPDIR:-/tmp}/study-gather-contract-test.XXXXXX")"
check_log="$backup_dir/check.log"

restore_files() {
  if [[ -f "$backup_dir/CreateStudyRequest.java" ]]; then
    cp "$backup_dir/CreateStudyRequest.java" "$project_dir/$target_dto"
  fi
  if [[ -f "$backup_dir/openapi.json" ]]; then
    cp "$backup_dir/openapi.json" "$project_dir/${contract_files[0]}"
  fi
  if [[ -f "$backup_dir/schema.ts" ]]; then
    cp "$backup_dir/schema.ts" "$project_dir/${contract_files[1]}"
  fi
  rm -rf "$backup_dir"
}

trap restore_files EXIT

files_to_protect=(
  "$target_dto"
  "${contract_files[@]}"
)

if ! git -C "$project_dir" diff --quiet -- "${files_to_protect[@]}"; then
  echo "실험 대상 파일에 기존 변경이 있습니다. 변경을 정리한 뒤 다시 실행해 주세요." >&2
  git -C "$project_dir" status --short -- "${files_to_protect[@]}" >&2
  exit 1
fi

cp "$project_dir/$target_dto" "$backup_dir/CreateStudyRequest.java"
cp "$project_dir/${contract_files[0]}" "$backup_dir/openapi.json"
cp "$project_dir/${contract_files[1]}" "$backup_dir/schema.ts"

before='@Size(max = Study.MAX_TITLE_LENGTH, message = "제목은 100자 이하여야 합니다.")'
after='@Size(max = 101, message = "제목은 101자 이하여야 합니다.")'

if [[ "$(grep -F -c "$before" "$project_dir/$target_dto")" -ne 1 ]]; then
  echo "DTO 실험 지점을 정확히 찾지 못했습니다." >&2
  exit 1
fi

sed "s/$before/$after/" "$project_dir/$target_dto" >"$backup_dir/modified-dto.java"
cp "$backup_dir/modified-dto.java" "$project_dir/$target_dto"

set +e
"$script_dir/check-openapi-contract.sh" >"$check_log" 2>&1
check_status=$?
set -e

if [[ "$check_status" -eq 0 ]]; then
  cat "$check_log"
  echo "계약 변경을 검사하지 못했습니다: 검사가 성공으로 종료되었습니다." >&2
  exit 1
fi

if ! grep -q "백엔드 API와 커밋된 OpenAPI 산출물이 일치하지 않습니다." "$check_log"; then
  cat "$check_log"
  echo "계약 차이가 아닌 다른 원인으로 검사가 실패했습니다." >&2
  exit 1
fi

if git -C "$project_dir" diff --quiet -- "${contract_files[@]}"; then
  cat "$check_log"
  echo "계약 검사 실패 메시지는 확인했지만 생성 산출물의 차이가 없습니다." >&2
  exit 1
fi

cat "$check_log"
echo
echo "DTO 변경에 따른 OpenAPI 계약 차이를 예상대로 감지했습니다."
echo "실험에 사용한 DTO와 생성 산출물은 원래 상태로 복구됩니다."
