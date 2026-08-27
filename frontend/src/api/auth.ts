import { apiRequest } from './client'
import type { LoginRequest, LoginResponse, SignUpRequest, User } from './types'

export function login(request: LoginRequest) {
  return apiRequest<LoginResponse>('/api/auth/login', { method: 'POST', body: request })
}

export function signUp(request: SignUpRequest) {
  return apiRequest<User>('/api/auth/signup', { method: 'POST', body: request })
}

export function getMyInfo(token: string) {
  return apiRequest<User>('/api/users/me', { token })
}
