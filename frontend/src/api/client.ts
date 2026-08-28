import type { ApiResponse } from './types'

export class ApiError extends Error {
  readonly status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

interface RequestOptions extends Omit<RequestInit, 'body'> {
  body?: unknown
  token?: string
}

function resolveApiUrl(path: string): string {
  const baseUrl = import.meta.env.VITE_API_BASE_URL?.replace(/\/+$/, '')

  if (!baseUrl) {
    return path
  }

  return `${baseUrl}${path.startsWith('/') ? path : `/${path}`}`
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { body, token, headers, ...requestInit } = options
  const response = await fetch(resolveApiUrl(path), {
    ...requestInit,
    headers: {
      ...(body === undefined ? {} : { 'Content-Type': 'application/json' }),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...headers,
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  })

  const payload = (await response.json().catch(() => null)) as ApiResponse<T> | null

  if (!response.ok || !payload?.success) {
    throw new ApiError(
      payload?.message ?? '요청을 처리하는 중 문제가 발생했습니다.',
      response.status,
    )
  }

  return payload.data
}
