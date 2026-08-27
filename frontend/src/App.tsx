import { Navigate, Route, Routes } from 'react-router-dom'
import { AppLayout } from './components/AppLayout'
import { ProtectedRoute } from './components/ProtectedRoute'
import { HomePage } from './pages/HomePage'
import { LoginPage } from './pages/LoginPage'
import { MyPage } from './pages/MyPage'
import { SignUpPage } from './pages/SignUpPage'
import { StudyDetailPage } from './pages/StudyDetailPage'
import { StudyCreatePage } from './pages/StudyCreatePage'
import { StudyListPage } from './pages/StudyListPage'
import './App.css'

function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<HomePage />} />
        <Route path="studies" element={<StudyListPage />} />
        <Route path="studies/:studyId" element={<StudyDetailPage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="signup" element={<SignUpPage />} />
        <Route element={<ProtectedRoute />}>
          <Route path="me" element={<MyPage />} />
          <Route path="studies/new" element={<StudyCreatePage />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  )
}

export default App
