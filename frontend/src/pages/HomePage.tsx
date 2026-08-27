import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export function HomePage() {
  const { user, isRestoring } = useAuth()

  return (
    <section className="hero-section">
      <div className="eyebrow">LEARN · CONNECT · GROW</div>
      <h1>혼자보다 멀리,<br />함께라서 꾸준히</h1>
      <p className="hero-copy">관심사가 같은 사람들과 스터디를 만들고, 배움의 여정을 이어가세요.</p>
      <div className="hero-actions">
        {isRestoring ? (
          <span className="muted">로그인 정보 확인 중...</span>
        ) : user ? (
          <>
            <Link className="primary-button" to="/me">내 정보 보기</Link>
            <span className="welcome-message">{user.nickname}님, 반갑습니다.</span>
          </>
        ) : (
          <>
            <Link className="primary-button" to="/signup">무료로 시작하기</Link>
            <Link className="text-link" to="/login">이미 계정이 있어요</Link>
          </>
        )}
      </div>
      <div className="feature-grid" aria-label="서비스 특징">
        <article><strong>01</strong><h2>간편한 참여</h2><p>관심 있는 스터디를 찾아 바로 참여를 신청하세요.</p></article>
        <article><strong>02</strong><h2>함께하는 성장</h2><p>목표가 같은 사람들과 꾸준히 학습할 수 있어요.</p></article>
        <article><strong>03</strong><h2>안전한 관리</h2><p>명확한 승인 절차로 스터디 멤버를 관리합니다.</p></article>
      </div>
    </section>
  )
}
