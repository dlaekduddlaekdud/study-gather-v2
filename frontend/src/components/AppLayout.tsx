import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export function AppLayout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  return (
    <div className="app-shell">
      <header className="site-header">
        <Link className="brand" to="/" aria-label="Study Gather 홈">
          <span className="brand-mark">S</span>
          <span>Study Gather</span>
        </Link>
        <nav className="main-nav" aria-label="주요 메뉴">
          <NavLink to="/">홈</NavLink>
          <NavLink to="/studies">스터디 찾기</NavLink>
          {user ? (
            <>
              <NavLink to="/me">내 정보</NavLink>
              <button className="nav-button" type="button" onClick={handleLogout}>로그아웃</button>
            </>
          ) : (
            <>
              <NavLink to="/login">로그인</NavLink>
              <Link className="nav-cta" to="/signup">회원가입</Link>
            </>
          )}
        </nav>
      </header>
      <main className="main-content"><Outlet /></main>
      <footer className="site-footer">함께 배우고 성장하는 스터디 커뮤니티</footer>
    </div>
  )
}
