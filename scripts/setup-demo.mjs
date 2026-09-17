import { readFile, writeFile } from 'node:fs/promises'
import { randomBytes } from 'node:crypto'

const baseUrl = (process.env.DEMO_API_BASE_URL || 'http://localhost:8080').replace(/\/+$/, '')
const configPath = new URL('../frontend/.env.local', import.meta.url)
const fixturePath = new URL('../.demo-fixtures.json', import.meta.url)

async function request(path, { token, body } = {}) {
  const response = await fetch(`${baseUrl}${path}`, {
    method: body === undefined ? 'GET' : 'POST',
    headers: {
      ...(body === undefined ? {} : { 'Content-Type': 'application/json' }),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body === undefined ? undefined : JSON.stringify(body),
    // 배포 서버가 대기 상태에서 깨어나는 시간까지 기다린다.
    signal: AbortSignal.timeout(120000),
  })
  const payload = await response.json().catch(() => null)
  if (!response.ok || !payload?.success) {
    const error = new Error(`${path}: ${payload?.message || `HTTP ${response.status}`}`)
    error.status = response.status
    throw error
  }
  return payload.data
}

async function readOptional(path) {
  try {
    return await readFile(path, 'utf8')
  } catch (error) {
    if (error.code !== 'ENOENT') throw error
    return ''
  }
}

async function signIn(account) {
  try {
    return await request('/api/auth/login', { body: account })
  } catch (error) {
    if (error.status !== 401) throw error
    await request('/api/auth/signup', { body: account })
    return request('/api/auth/login', { body: account })
  }
}

async function ensureStudy(token, ownerId, title, description) {
  const studies = await request('/api/studies')
  const existing = studies.find((study) => study.ownerId === ownerId && study.title === title)
  if (existing) return existing
  return request('/api/studies', {
    token,
    body: { title, description, capacity: 10, recruitmentDeadline: '2099-12-31T23:59:00' },
  })
}

async function ensureApplication(token, studyId, message) {
  const applications = await request('/api/applications/me', { token })
  const existing = applications.find((application) => application.studyId === studyId)
  if (existing) return { application: existing, created: false }
  const application = await request(`/api/studies/${studyId}/applications`, {
    token, body: { message },
  })
  return { application, created: true }
}

function setEnv(source, key, value) {
  const line = `${key}=${JSON.stringify(value)}`
  const pattern = new RegExp(`^${key}=.*$`, 'm')
  return pattern.test(source) ? source.replace(pattern, () => line) : `${source.trimEnd()}\n${line}\n`
}

async function main() {
  console.log(`데모 준비 서버: ${baseUrl} (첫 응답은 최대 2분 기다립니다.)`)
  // 중간 실패 후 재실행해도 같은 계정으로 이어서 준비한다.
  const saved = await readOptional(fixturePath)
  const fixture = saved ? JSON.parse(saved) : {
    accounts: ['demo', 'owner', 'applicant', 'member'].map((role) => ({
      email: `portfolio-${role}-${randomBytes(6).toString('hex')}@example.com`,
      password: randomBytes(24).toString('hex'),
      nickname: { demo: '데모체험자', owner: '예시운영자', applicant: '예시신청자', member: '예시멤버' }[role],
    })),
  }
  if (!saved) await writeFile(fixturePath, JSON.stringify(fixture, null, 2), { mode: 0o600, flag: 'wx' })
  const sessions = []
  for (const account of fixture.accounts) {
    const { accessToken } = await signIn(account)
    const user = await request('/api/users/me', { token: accessToken })
    sessions.push({ token: accessToken, user })
  }
  const [demo, owner, applicant, member] = sessions
  const managed = await ensureStudy(demo.token, demo.user.id,
    '[데모] 운영자 체험 · Spring Boot 스터디',
    '데모 계정이 운영하는 체험용 스터디입니다. 아래에서 받은 신청 관리, 멤버 확인, 스터디 수정 및 모집 마감을 체험하세요. 변경 내용은 다른 방문자에게도 반영됩니다.')
  const participant = await ensureStudy(owner.token, owner.user.id,
    '[데모] 참여자 체험 · 코딩테스트 스터디',
    '데모 계정으로 참여 신청할 수 있는 예시 스터디입니다. 신청 후 상단 내 신청에서 확인할 수 있습니다.')
  const applied = await ensureStudy(owner.token, owner.user.id,
    '[데모] 내 신청 확인 · React 스터디',
    '데모 계정의 신청 내역을 미리 준비한 스터디입니다. 내 신청 화면에서 상태 확인과 취소를 체험하세요.')
  await ensureApplication(applicant.token, managed.id, '매주 학습 내용을 정리하고 함께 코드 리뷰하고 싶습니다.')
  const memberApplication = await ensureApplication(member.token, managed.id, 'Spring Boot 프로젝트 경험을 공유하고 싶습니다.')
  if (memberApplication.application.status === 'PENDING') {
    await request(`/api/applications/${memberApplication.application.id}/approve`, { token: demo.token, body: {} })
  }
  await ensureApplication(demo.token, applied.id, 'React 학습에 함께 참여하고 싶습니다.')

  let config = await readOptional(configPath)
  config = setEnv(config, 'VITE_DEMO_EMAIL', fixture.accounts[0].email)
  config = setEnv(config, 'VITE_DEMO_PASSWORD', fixture.accounts[0].password)
  await writeFile(configPath, config, { mode: 0o600 })
  console.log('데모 준비 완료. 프론트 개발 서버를 재시작하세요.')
  console.log(`운영자 체험: /studies/${managed.id}`)
  console.log(`참여 신청 체험: /studies/${participant.id}`)
  console.log('내 신청: /applications/me')
  console.log('공개 데모 로그인 정보는 frontend/.env.local에 저장했습니다.')
}

main().catch((error) => {
  console.error(error.name === 'TimeoutError'
    ? '데모 준비 실패: 서버가 2분 안에 응답하지 않았습니다. Render 서버 상태와 로그를 확인한 뒤 같은 명령을 다시 실행하세요.'
    : `데모 준비 실패: ${error.message}`)
  process.exitCode = 1
})
