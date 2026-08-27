import type { operations } from './generated/schema'
import { ApiError, apiRequest } from './client'

type GetOpenStudiesResponse =
  operations['getOpenStudies']['responses'][200]['content']['*/*']
type GeneratedStudyList = NonNullable<GetOpenStudiesResponse['data']>
type GeneratedStudySummary = GeneratedStudyList[number]

export type StudySummary = Required<GeneratedStudySummary>

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

export async function getOpenStudies(signal?: AbortSignal): Promise<StudySummary[]> {
  const studies = await apiRequest<GeneratedStudyList>('/api/studies', { signal })

  if (!studies.every(isStudySummary)) {
    throw new ApiError('스터디 목록 응답 형식이 올바르지 않습니다.', 500)
  }

  return studies
}
