import { useEffect, useState } from 'react'
import { ApiError } from '../api/client'
import { getOpenStudies, type StudySummary } from '../api/studies'
import { StudyCard } from '../components/StudyCard'

type StudyListState =
  | { status: 'loading' }
  | { status: 'success'; studies: StudySummary[] }
  | { status: 'error'; message: string }

function getErrorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    return error.message
  }

  return '스터디 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.'
}

export function StudyListPage() {
  const [state, setState] = useState<StudyListState>({ status: 'loading' })
  const [requestVersion, setRequestVersion] = useState(0)

  useEffect(() => {
    const controller = new AbortController()

    getOpenStudies(controller.signal)
      .then((studies) => setState({ status: 'success', studies }))
      .catch((error: unknown) => {
        if (error instanceof DOMException && error.name === 'AbortError') {
          return
        }

        setState({ status: 'error', message: getErrorMessage(error) })
      })

    return () => controller.abort()
  }, [requestVersion])

  const retry = () => {
    setState({ status: 'loading' })
    setRequestVersion((version) => version + 1)
  }

  return (
    <section className="study-list-page">
      <header className="study-list-header">
        <p className="eyebrow">OPEN STUDIES</p>
        <h1>함께 성장할 스터디를 찾아보세요</h1>
        <p>현재 참여 신청을 받고 있는 스터디만 모았습니다.</p>
      </header>

      {state.status === 'loading' && (
        <div className="study-state" role="status">
          <div className="study-loading" aria-hidden="true" />
          <p>모집 중인 스터디를 불러오고 있습니다.</p>
        </div>
      )}

      {state.status === 'error' && (
        <div className="study-state study-state--error" role="alert">
          <h2>목록을 불러오지 못했어요</h2>
          <p>{state.message}</p>
          <button className="primary-button" type="button" onClick={retry}>
            다시 시도
          </button>
        </div>
      )}

      {state.status === 'success' && state.studies.length === 0 && (
        <div className="study-state">
          <h2>현재 모집 중인 스터디가 없어요</h2>
          <p>새로운 스터디가 열리면 이곳에서 확인할 수 있습니다.</p>
        </div>
      )}

      {state.status === 'success' && state.studies.length > 0 && (
        <div className="study-grid">
          {state.studies.map((study) => (
            <StudyCard key={study.id} study={study} />
          ))}
        </div>
      )}
    </section>
  )
}
