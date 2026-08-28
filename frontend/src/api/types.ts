export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}

export type UserRole = 'USER' | 'ADMIN'

export interface User {
  id: number
  email: string
  nickname: string
  role: UserRole
}

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  accessToken: string
}

export interface SignUpRequest {
  email: string
  password: string
  nickname: string
}
