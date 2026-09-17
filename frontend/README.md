# Study Gather 프론트엔드

## 포트폴리오 공개 진입과 데모 로그인

- `/`는 `/studies`로 이동하며 기존 스터디 목록을 표시합니다.
- 목록과 상세는 토큰 없이 조회합니다. 생성·신청·신청 관리 등 기존 인증과 운영자 검사는 유지합니다.
- 데모 버튼은 일반 로그인과 동일한 `AuthProvider.login()` → `POST /api/auth/login` → JWT 저장 → `GET /api/users/me` 흐름을 사용합니다.
- 데모 로그인 후에는 원래 요청한 보호 화면으로 돌아가며, 그런 화면이 없으면 스터디 목록으로 이동합니다.

### 계정 준비

운영자·참여자 화면을 한 계정으로 체험하려면 프로젝트 루트에서
`node scripts/setup-demo.mjs`를 실행하세요. 로컬 백엔드에 계정과 예시 데이터를 생성하고
이 폴더의 `.env.local`을 설정합니다. 개발 서버 재시작 후 데모 버튼으로 로그인하면
`[데모] 운영자 체험` 스터디에서 신청 관리·멤버·수정 화면을,
`[데모] 참여자 체험` 스터디에서 참여 신청을 볼 수 있습니다.
자세한 실행·재실행·배포 방법은 `../scripts/README.md`를 참고하세요.

직접 계정을 준비할 경우에는 아래 순서를 사용합니다.

1. 배포 사이트의 기존 회원가입 화면에서 공개 체험 전용 일반 계정을 생성합니다. 이메일 인증이나 별도 데모 API는 필요하지 않습니다.
2. 로컬에서는 이 폴더의 `.env.example`을 `.env.local`로 복사하고 `VITE_DEMO_EMAIL`, `VITE_DEMO_PASSWORD`에 해당 계정 정보를 넣습니다.
3. 배포 환경에서도 프론트 빌드 환경변수에 두 값을 설정하고 다시 빌드·배포합니다. 개발 서버는 환경변수 변경 후 재시작합니다.
4. 두 값이 없으면 버튼은 비활성화됩니다. 계정은 자동 생성되지 않으므로 실제 DB에 가입된 계정이어야 합니다.

`VITE_` 값은 브라우저에 공개됩니다. 환경변수로 설정해도 비밀번호가 비밀로 보관되는 것은 아닙니다. 개인 계정·관리자 계정·다른 서비스에서 사용하는 비밀번호를 넣지 않습니다.

### 데이터 변경 범위

현재 데모 전용 쓰기 제한은 없습니다. 공유 계정으로 스터디 생성, 참여 신청·취소가 가능하며, 해당 계정이 운영자인 스터디는 수정·모집 마감·신청 승인/거절도 가능합니다. 계정을 공유하는 방문자는 같은 신청 내역을 봅니다. 다른 운영자의 스터디 관리는 기존 서버 권한 검사로 차단됩니다. 현재 탈퇴·비밀번호 변경·스터디 삭제 API는 없습니다.

실제 운영 데이터를 보존해야 한다면 활성화 전에 데모 계정의 위 쓰기 작업을 서버에서 제한해야 합니다. 화면에서 버튼만 숨겨서는 API 호출을 막을 수 없습니다. 현재 변경에는 이런 제한이나 자동 초기화 시스템을 포함하지 않습니다. 대표 예시 스터디는 공유 데모 계정과 다른 운영자가 소유하도록 준비하면 공유 계정에서 수정·마감할 수 없습니다.

목록에는 `OPEN` 상태이면서 모집 마감일이 미래인 스터디만 표시됩니다. 예시 데이터가 있어도 모집 기간이 지나면 목록에서 사라집니다.

### 실행 확인

프론트엔드 폴더에서 실행합니다.

```bash
npm run build
npm run lint
npm run test
npm run dev
```

- 시크릿 창에서 `/` 접속 → `/studies` 목록 표시, 브라우저 제목 `Study Gather`.
- 스터디 선택 → 비로그인 상세 조회 성공, 참여 영역에는 로그인 안내.
- 비로그인 `/studies/new` 접속 → 로그인 화면으로 이동.
- 일반 로그인 → 기존 로그인 및 원래 화면 복귀 동작 유지.
- 데모 버튼 → 입력란을 비워도 일반 로그인 API 호출, 성공 시 JWT로 내 정보 조회 후 목록 또는 원래 화면으로 이동.
- 잘못된 데모 비밀번호 → 오류 안내, 버튼 재활성화.
- 환경변수 미설정 → 데모 버튼 비활성화, 비로그인 목록 링크 사용 가능.

실행 및 배포 검증 결과는 별도로 확인해야 합니다.

---

## Vite 템플릿 참고

This template provides a minimal setup to get React working in Vite with HMR and some Oxlint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the Oxlint configuration

If you are developing a production application, we recommend enabling type-aware lint rules by installing `oxlint-tsgolint` and editing `.oxlintrc.json`:

```json
{
  "$schema": "./node_modules/oxlint/configuration_schema.json",
  "plugins": ["react", "typescript", "oxc"],
  "options": {
    "typeAware": true
  },
  "rules": {
    "react/rules-of-hooks": "error",
    "react/only-export-components": ["warn", { "allowConstantExport": true }]
  }
}
```

See the [Oxlint rules documentation](https://oxc.rs/docs/guide/usage/linter/rules) for the full list of rules and categories.
