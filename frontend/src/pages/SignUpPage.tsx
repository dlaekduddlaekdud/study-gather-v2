import { useState, type FormEvent } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { signUp } from '../api/auth'
import { ApiError } from '../api/client'
import { useAuth } from '../auth/AuthContext'

export function SignUpPage() {
  const { user } = useAuth()
  const [email, setEmail] = useState('')
  const [nickname, setNickname] = useState('')
  const [password, setPassword] = useState('')
  const [passwordConfirm, setPasswordConfirm] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isComplete, setIsComplete] = useState(false)

  if (user) return <Navigate to="/me" replace />

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    if (password !== passwordConfirm) {
      setError('비밀번호가 일치하지 않습니다.')
      return
    }

    setIsSubmitting(true)
    try {
      await signUp({ email, nickname, password })
      setIsComplete(true)
    } catch (submitError) {
      setError(submitError instanceof ApiError
        ? submitError.message
        : '서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.')
    } finally {
      setIsSubmitting(false)
    }
  }

  if (isComplete) {
    return (
      <section className="completion-card">
        <span className="completion-icon">✓</span>
        <h1>가입이 완료되었습니다</h1>
        <p>{nickname}님, 이제 로그인하고 Study Gather를 시작해 보세요.</p>
        <Link className="primary-button" to="/login">로그인하러 가기</Link>
      </section>
    )
  }

  return (
    <section className="auth-page">
      <div className="auth-intro">
        <span className="eyebrow">START TOGETHER</span>
        <h1>새로운 배움의<br />시작</h1>
        <p>간단한 정보만 입력하면 바로 시작할 수 있어요.</p>
      </div>
      <div className="auth-card">
        <h2>회원가입</h2>
        <p className="card-description">Study Gather에서 함께 성장해 보세요.</p>
        <form onSubmit={handleSubmit}>
          <label htmlFor="signup-email">이메일</label>
          <input id="signup-email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} autoComplete="email" maxLength={255} placeholder="name@example.com" required />
          <label htmlFor="signup-nickname">닉네임</label>
          <input id="signup-nickname" value={nickname} onChange={(event) => setNickname(event.target.value)} autoComplete="nickname" minLength={2} maxLength={50} placeholder="2~50자로 입력해 주세요" required />
          <label htmlFor="signup-password">비밀번호</label>
          <input id="signup-password" type="password" value={password} onChange={(event) => setPassword(event.target.value)} autoComplete="new-password" minLength={8} maxLength={64} placeholder="8자 이상 입력해 주세요" required />
          <label htmlFor="signup-password-confirm">비밀번호 확인</label>
          <input id="signup-password-confirm" type="password" value={passwordConfirm} onChange={(event) => setPasswordConfirm(event.target.value)} autoComplete="new-password" minLength={8} maxLength={64} placeholder="비밀번호를 다시 입력해 주세요" required />
          {error ? <p className="form-error" role="alert">{error}</p> : null}
          <button className="submit-button" type="submit" disabled={isSubmitting}>
            {isSubmitting ? '가입 중...' : '회원가입'}
          </button>
        </form>
        <p className="auth-switch">이미 계정이 있나요? <Link to="/login">로그인</Link></p>
      </div>
    </section>
  )
}
