import type { components, operations } from './generated/schema'
import { ApiError, apiRequest } from './client'

type GeneratedCreateApplicationRequest =
  operations['createApplication']['requestBody']['content']['application/json']
type GeneratedApplicationResponse = components['schemas']['ApplicationResponse']
type GetMyApplicationsResponse =
  operations['getMyApplications']['responses'][200]['content']['*/*']
type GeneratedApplicationList = NonNullable<GetMyApplicationsResponse['data']>
type GeneratedApplicationListItem = GeneratedApplicationList[number]

export type CreateApplicationRequest = GeneratedCreateApplicationRequest
export type ApplicationStatus = NonNullable<GeneratedApplicationListItem['status']>
export type ApplicationListItem = Required<
  Omit<GeneratedApplicationListItem, 'message' | 'decidedAt'>
> & {
  message: string | null
  decidedAt: string | null
}

export type ApplicationResult = Required<
  Omit<GeneratedApplicationResponse, 'message' | 'decidedAt'>
> & {
  message: string | null
  decidedAt: string | null
}

function parseApplicationListItem(application: unknown): ApplicationListItem {
  if (typeof application !== 'object' || application === null) {
    throw new ApiError('내 참여 신청 목록 응답 형식이 올바르지 않습니다.', 500)
  }
  const candidate = application as Record<string, unknown>

  const isValid = (
    typeof candidate.id === 'number' &&
    typeof candidate.studyId === 'number' &&
    typeof candidate.studyTitle === 'string' &&
    typeof candidate.applicantId === 'number' &&
    typeof candidate.applicantNickname === 'string' &&
    (typeof candidate.message === 'string' || candidate.message == null) &&
    (candidate.status === 'PENDING' ||
      candidate.status === 'APPROVED' ||
      candidate.status === 'REJECTED' ||
      candidate.status === 'CANCELED') &&
    (typeof candidate.decidedAt === 'string' || candidate.decidedAt == null) &&
    typeof candidate.createdAt === 'string'
  )

  if (!isValid) {
    throw new ApiError('내 참여 신청 목록 응답 형식이 올바르지 않습니다.', 500)
  }

  return {
    id: candidate.id as number,
    studyId: candidate.studyId as number,
    studyTitle: candidate.studyTitle as string,
    applicantId: candidate.applicantId as number,
    applicantNickname: candidate.applicantNickname as string,
    message: typeof candidate.message === 'string' ? candidate.message : null,
    status: candidate.status as ApplicationStatus,
    decidedAt: typeof candidate.decidedAt === 'string' ? candidate.decidedAt : null,
    createdAt: candidate.createdAt as string,
  }
}

function parseApplicationResult(application: unknown): ApplicationResult {
  if (typeof application !== 'object' || application === null) {
    throw new ApiError('참여 신청 응답 형식이 올바르지 않습니다.', 500)
  }
  const candidate = application as Record<string, unknown>

  const isValid = (
    typeof candidate.id === 'number' &&
    typeof candidate.studyId === 'number' &&
    typeof candidate.applicantId === 'number' &&
    (typeof candidate.message === 'string' || candidate.message == null) &&
    (candidate.status === 'PENDING' ||
      candidate.status === 'APPROVED' ||
      candidate.status === 'REJECTED' ||
      candidate.status === 'CANCELED') &&
    (typeof candidate.decidedAt === 'string' || candidate.decidedAt == null) &&
    typeof candidate.createdAt === 'string'
  )

  if (!isValid) {
    throw new ApiError('참여 신청 응답 형식이 올바르지 않습니다.', 500)
  }

  return {
    id: candidate.id as number,
    studyId: candidate.studyId as number,
    applicantId: candidate.applicantId as number,
    message: typeof candidate.message === 'string' ? candidate.message : null,
    status: candidate.status as ApplicationStatus,
    decidedAt: typeof candidate.decidedAt === 'string' ? candidate.decidedAt : null,
    createdAt: candidate.createdAt as string,
  }
}

export async function createApplication(
  studyId: number,
  request: CreateApplicationRequest,
  token: string,
): Promise<ApplicationResult> {
  const application = await apiRequest<GeneratedApplicationResponse>(
    `/api/studies/${studyId}/applications`,
    { method: 'POST', body: request, token },
  )

  return parseApplicationResult(application)
}

export async function getMyApplications(
  token: string,
  signal?: AbortSignal,
): Promise<ApplicationListItem[]> {
  const applications = await apiRequest<GeneratedApplicationList>('/api/applications/me', {
    token,
    signal,
  })

  return applications.map(parseApplicationListItem)
}

export async function cancelApplication(
  applicationId: number,
  token: string,
): Promise<ApplicationResult> {
  const application = await apiRequest<GeneratedApplicationResponse>(
    `/api/applications/${applicationId}/cancel`,
    { method: 'POST', token },
  )

  return parseApplicationResult(application)
}
