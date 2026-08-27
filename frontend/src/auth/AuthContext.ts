import { createContext, useContext } from 'react'
import type { LoginRequest, User } from '../api/types'

export interface AuthContextValue {
  user: User | null
  isRestoring: boolean
  login: (request: LoginRequest) => Promise<void>
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth는 AuthProvider 내부에서 사용해야 합니다.')
  return context
}
