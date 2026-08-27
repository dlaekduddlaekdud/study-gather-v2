import type { operations } from './generated/schema'
import { ApiError, apiRequest } from './client'

type GetOpenStudiesResponse =
  operations['getOpenStudies']['responses'][200]['content']['*/*']
type GeneratedStudyList = NonNullable<GetOpenStudiesResponse['data']>
type GeneratedStudySummary = GeneratedStudyList[number]
type GetStudyResponse = operations['getStudy']['responses'][200]['content']['*/*']
type GeneratedStudyDetail = NonNullable<GetStudyResponse['data']>
type CreateStudyOperation = operations['createStudy']
type GeneratedCreateStudyRequest =
  CreateStudyOperation['requestBody']['content']['application/json']

export type StudySummary = Required<GeneratedStudySummary>
export type StudyDetail = Required<GeneratedStudyDetail>
export type CreateStudyRequest = Required<GeneratedCreateStudyRequest>

function isStudySummary(study: GeneratedStudySummary): study is StudySummary {
  return (
    typeof study.id === 'number' &&
    typeof study.ownerId === 'number' &&
    typeof study.title === 'string' &&
    typeof study.capacity === 'number' &&
    typeof study.approvedCount === 'number' &&
    typeof study.recruitmentDeadline === 'string' &&
    (study.status === 'OPEN' || study.status === 'CLOSED')
  )
}

function isStudyDetail(study: GeneratedStudyDetail): study is StudyDetail {
  return (
    typeof study.id === 'number' &&
    typeof study.ownerId === 'number' &&
    typeof study.title === 'string' &&
    typeof study.description === 'string' &&
    typeof study.capacity === 'number' &&
    typeof study.approvedCount === 'number' &&
    typeof study.recruitmentDeadline === 'string' &&
    typeof study.createdAt === 'string' &&
    (study.status === 'OPEN' || study.status === 'CLOSED')
  )
}

export async function getOpenStudies(signal?: AbortSignal): Promise<StudySummary[]> {
  const studies = await apiRequest<GeneratedStudyList>('/api/studies', { signal })

  if (!studies.every(isStudySummary)) {
    throw new ApiError('스터디 목록 응답 형식이 올바르지 않습니다.', 500)
  }

  return studies
}

export async function getStudy(studyId: number, signal?: AbortSignal): Promise<StudyDetail> {
  const study = await apiRequest<GeneratedStudyDetail>(`/api/studies/${studyId}`, { signal })

  if (!isStudyDetail(study)) {
    throw new ApiError('스터디 상세 응답 형식이 올바르지 않습니다.', 500)
  }

  return study
}

export async function createStudy(
  request: CreateStudyRequest,
  token: string,
): Promise<StudyDetail> {
  const study = await apiRequest<GeneratedStudyDetail>('/api/studies', {
    method: 'POST',
    body: request,
    token,
  })

  if (!isStudyDetail(study)) {
    throw new ApiError('스터디 생성 응답 형식이 올바르지 않습니다.', 500)
  }

  return study
}
