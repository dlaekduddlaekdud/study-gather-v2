import { useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import { useAuth } from '../auth/AuthContext'

export function LoginPage() {
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  if (user) return <Navigate to="/me" replace />

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    setIsSubmitting(true)
    try {
      await login({ email, password })
      const state = location.state as { from?: string } | null
      navigate(state?.from ?? '/me', { replace: true })
    } catch (submitError) {
      setError(submitError instanceof ApiError
        ? submitError.message
        : '서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.')
    } finally {
      setIsSubmitting(false)
    }
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
        <p className="auth-switch">아직 계정이 없나요? <Link to="/signup">회원가입</Link></p>
      </div>
    </section>
  )
}
