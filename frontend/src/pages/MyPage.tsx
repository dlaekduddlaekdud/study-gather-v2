import { useAuth } from '../auth/AuthContext'

export function MyPage() {
  const { user } = useAuth()
  if (!user) return null

  return (
    <section className="profile-page">
      <div>
        <span className="eyebrow">MY PROFILE</span>
        <h1>내 정보</h1>
        <p>서버에서 조회한 현재 로그인 사용자 정보입니다.</p>
      </div>
      <article className="profile-card">
        <div className="avatar" aria-hidden="true">{user.nickname.slice(0, 1).toUpperCase()}</div>
        <div className="profile-summary">
          <h2>{user.nickname}</h2>
          <span>{user.role === 'ADMIN' ? '관리자' : '일반 회원'}</span>
        </div>
        <dl>
          <div><dt>회원 번호</dt><dd>{user.id}</dd></div>
          <div><dt>이메일</dt><dd>{user.email}</dd></div>
          <div><dt>닉네임</dt><dd>{user.nickname}</dd></div>
          <div><dt>권한</dt><dd>{user.role}</dd></div>
        </dl>
      </article>
    </section>
  )
}
