import { Navigate, Route, Routes } from 'react-router-dom'
import { AppLayout } from './components/AppLayout'
import { ProtectedRoute } from './components/ProtectedRoute'
import { HomePage } from './pages/HomePage'
import { LoginPage } from './pages/LoginPage'
import { MyApplicationsPage } from './pages/MyApplicationsPage'
import { MyPage } from './pages/MyPage'
import { OwnerApplicationsPage } from './pages/OwnerApplicationsPage'
import { SignUpPage } from './pages/SignUpPage'
import { StudyDetailPage } from './pages/StudyDetailPage'
import { StudyCreatePage } from './pages/StudyCreatePage'
import { StudyListPage } from './pages/StudyListPage'
import { StudyMembersPage } from './pages/StudyMembersPage'
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
          <Route path="applications/me" element={<MyApplicationsPage />} />
          <Route path="studies/new" element={<StudyCreatePage />} />
          <Route path="studies/:studyId/applications" element={<OwnerApplicationsPage />} />
          <Route path="studies/:studyId/members" element={<StudyMembersPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  )
}

export default App
