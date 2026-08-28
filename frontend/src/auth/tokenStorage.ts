const ACCESS_TOKEN_KEY = 'study-gather:v1:access-token'

export const tokenStorage = {
  get: (): string | null => localStorage.getItem(ACCESS_TOKEN_KEY),
  set: (token: string): void => localStorage.setItem(ACCESS_TOKEN_KEY, token),
  remove: (): void => localStorage.removeItem(ACCESS_TOKEN_KEY),
}
