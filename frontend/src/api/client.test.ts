import { afterEach, describe, expect, it, vi } from 'vitest'
import { ApiError, apiRequest } from './client'

interface TestData {
  id: number
}

const errorCases = [
  [400, '입력값이 올바르지 않습니다.'],
  [401, '인증이 필요합니다.'],
  [403, '접근 권한이 없습니다.'],
  [409, '이미 처리된 요청입니다.'],
] as const

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('apiRequest', () => {
  it('성공 응답의 data를 반환한다', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse({
      success: true,
      message: '조회 성공',
      data: { id: 1 },
    })))

    await expect(apiRequest<TestData>('/api/test')).resolves.toEqual({ id: 1 })
  })

  it.each(errorCases)('%i 응답의 상태 코드와 메시지를 ApiError에 보존한다', async (status, message) => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse({
      success: false,
      message,
      data: null,
    }, status)))

    await expect(apiRequest('/api/test')).rejects.toMatchObject({
      name: 'ApiError',
      status,
      message,
    })
  })

  it('HTTP 200이어도 success가 false이면 ApiError를 던진다', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse({
      success: false,
      message: '요청 처리에 실패했습니다.',
      data: null,
    })))

    await expect(apiRequest('/api/test')).rejects.toEqual(
      new ApiError('요청 처리에 실패했습니다.', 200),
    )
  })

  it('JSON이 아닌 오류 응답에는 기본 메시지를 사용한다', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response('Internal Server Error', {
      status: 500,
    })))

    await expect(apiRequest('/api/test')).rejects.toMatchObject({
      name: 'ApiError',
      status: 500,
      message: '요청을 처리하는 중 문제가 발생했습니다.',
    })
  })

  it('요청 본문과 인증 토큰을 fetch 옵션으로 변환한다', async () => {
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse({
      success: true,
      message: '생성 성공',
      data: { id: 2 },
    }))
    vi.stubGlobal('fetch', fetchMock)

    await apiRequest<TestData>('/api/test', {
      method: 'POST',
      token: 'access-token',
      body: { title: '테스트 스터디' },
    })

    expect(fetchMock).toHaveBeenCalledWith('/api/test', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer access-token',
      },
      body: JSON.stringify({ title: '테스트 스터디' }),
    })
  })
})
