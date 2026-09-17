import { useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import type { LoginRequest } from '../api/types'
import { useAuth } from '../auth/AuthContext'

const demoEmail = import.meta.env.VITE_DEMO_EMAIL?.trim()
const demoPassword = import.meta.env.VITE_DEMO_PASSWORD
const isDemoConfigured = Boolean(demoEmail && demoPassword)

export function LoginPage() {
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  if (user) return <Navigate to="/me" replace />

  const submitLogin = async (request: LoginRequest, fallbackPath: string) => {
    if (isSubmitting) return
    setError('')
    setIsSubmitting(true)
    try {
      await login(request)
      const state = location.state as { from?: string } | null
      navigate(state?.from ?? fallbackPath, { replace: true })
    } catch (submitError) {
      setError(submitError instanceof ApiError
        ? submitError.message
        : '서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.')
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    void submitLogin({ email, password }, '/me')
  }

  const handleDemoLogin = () => {
    if (!demoEmail || !demoPassword) return
    void submitLogin({ email: demoEmail, password: demoPassword }, '/studies')
  }

  return (
    <section className="auth-page">
      <div className="auth-intro">
        <span className="eyebrow">WELCOME BACK</span>
        <h1>다시 만나서<br />반가워요</h1>
        <p>로그인하고 진행 중인 스터디를 확인해 보세요.</p>
      </div>
      <div className="auth-card">
        <h2>로그인</h2>
        <p className="card-description">Study Gather 계정으로 계속합니다.</p>
        <form onSubmit={handleSubmit}>
          <label htmlFor="login-email">이메일</label>
          <input id="login-email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} autoComplete="email" placeholder="name@example.com" required />
          <label htmlFor="login-password">비밀번호</label>
          <input id="login-password" type="password" value={password} onChange={(event) => setPassword(event.target.value)} autoComplete="current-password" minLength={8} maxLength={64} placeholder="8자 이상 입력해 주세요" required />
          {error ? <p className="form-error" role="alert">{error}</p> : null}
          <button className="submit-button" type="submit" disabled={isSubmitting}>
            {isSubmitting ? '로그인 중...' : '로그인'}
          </button>
        </form>
        <button
          className="submit-button"
          type="button"
          onClick={handleDemoLogin}
          disabled={isSubmitting || !isDemoConfigured}
          aria-describedby="demo-login-description"
        >
          데모 계정으로 둘러보기
        </button>
        <p id="demo-login-description" className="card-description">
          {isDemoConfigured
            ? '계정 하나로 스터디 운영과 참여를 체험합니다. [데모] 스터디를 선택해 주세요. 변경 내용은 다른 방문자에게도 반영됩니다.'
            : '데모 계정을 준비 중입니다. 스터디 목록과 상세는 로그인 없이 볼 수 있습니다.'}
        </p>
        <Link className="text-link" to="/studies">로그인 없이 스터디 둘러보기</Link>
        <p className="auth-switch">아직 계정이 없나요? <Link to="/signup">회원가입</Link></p>
      </div>
    </section>
  )
}
