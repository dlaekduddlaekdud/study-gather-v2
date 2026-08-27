import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { getMyInfo, login as requestLogin } from '../api/auth'
import { ApiError } from '../api/client'
import type { LoginRequest, User } from '../api/types'
import { AuthContext } from './AuthContext'
import { tokenStorage } from './tokenStorage'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [initialToken] = useState(() => tokenStorage.get())
  const [isRestoring, setIsRestoring] = useState(initialToken !== null)

  useEffect(() => {
    if (!initialToken) return

    let isActive = true
    getMyInfo(initialToken)
      .then((restoredUser) => {
        if (isActive) setUser(restoredUser)
      })
      .catch((error: unknown) => {
        if (error instanceof ApiError && error.status === 401) tokenStorage.remove()
      })
      .finally(() => {
        if (isActive) setIsRestoring(false)
      })

    return () => {
      isActive = false
    }
  }, [initialToken])

  const login = useCallback(async (request: LoginRequest) => {
    const { accessToken } = await requestLogin(request)
    tokenStorage.set(accessToken)
    try {
      setUser(await getMyInfo(accessToken))
    } catch (error) {
      tokenStorage.remove()
      throw error
    }
  }, [])

  const logout = useCallback(() => {
    tokenStorage.remove()
    setUser(null)
  }, [])

  const value = useMemo(
    () => ({ user, isRestoring, login, logout }),
    [user, isRestoring, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
