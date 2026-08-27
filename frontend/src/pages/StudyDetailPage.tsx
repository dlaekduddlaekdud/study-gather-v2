import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ApiError } from '../api/client'
import { getStudy, type StudyDetail } from '../api/studies'
import { useAuth } from '../auth/AuthContext'
import { StudyApplicationForm } from '../components/StudyApplicationForm'

type StudyDetailState =
  | { status: 'loading' }
  | { status: 'success'; requestId: number; study: StudyDetail }
  | { status: 'error'; requestId: number; message: string }

const dateTimeFormatter = new Intl.DateTimeFormat('ko-KR', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
})

function formatDateTime(value: string): string {
  const date = new Date(value)

  return Number.isNaN(date.getTime()) ? '날짜 정보 없음' : dateTimeFormatter.format(date)
}

function getErrorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    return error.message
  }

  return '스터디 정보를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.'
}

export function StudyDetailPage() {
  const { user, accessToken } = useAuth()
  const { studyId: studyIdParam } = useParams()
  const studyId = Number(studyIdParam)
  const isValidStudyId = Number.isSafeInteger(studyId) && studyId > 0
  const [state, setState] = useState<StudyDetailState>({ status: 'loading' })
  const [requestVersion, setRequestVersion] = useState(0)

  useEffect(() => {
    if (!isValidStudyId) {
      return
    }

    const controller = new AbortController()

    getStudy(studyId, controller.signal)
      .then((study) => setState({ status: 'success', requestId: studyId, study }))
      .catch((error: unknown) => {
        if (error instanceof DOMException && error.name === 'AbortError') {
          return
        }

        setState({ status: 'error', requestId: studyId, message: getErrorMessage(error) })
      })

    return () => controller.abort()
  }, [isValidStudyId, requestVersion, studyId])

  const retry = () => {
    setState({ status: 'loading' })
    setRequestVersion((version) => version + 1)
  }
  if (!isValidStudyId) {
    return (
      <section className="study-detail-page">
        <div className="study-state study-state--error" role="alert">
          <h1>잘못된 스터디 주소입니다</h1>
          <p>스터디 목록에서 다시 선택해 주세요.</p>
          <Link className="primary-button" to="/studies">목록으로 돌아가기</Link>
        </div>
      </section>
    )
  }

  if (state.status === 'loading' || state.requestId !== studyId) {
    return (
      <section className="study-detail-page">
        <div className="study-state" role="status">
          <div className="study-loading" aria-hidden="true" />
          <p>스터디 정보를 불러오고 있습니다.</p>
        </div>
      </section>
    )
  }

  if (state.status === 'error') {
    return (
      <section className="study-detail-page">
        <div className="study-state study-state--error" role="alert">
          <h1>스터디를 불러오지 못했어요</h1>
          <p>{state.message}</p>
          <div className="study-state__actions">
            <button className="primary-button" type="button" onClick={retry}>다시 시도</button>
            <Link className="text-link" to="/studies">목록으로 돌아가기</Link>
          </div>
        </div>
      </section>
    )
  }

  const { study } = state
  const remainingSeats = Math.max(study.capacity - study.approvedCount, 0)

  return (
    <section className="study-detail-page">
      <Link className="study-detail-back" to="/studies">← 스터디 목록</Link>

      <article className="study-detail-card">
        <header className="study-detail-header">
          <div>
            <span className={`study-detail-status study-detail-status--${study.status.toLowerCase()}`}>
              {study.status === 'OPEN' ? '모집 중' : '모집 마감'}
            </span>
            <span className="study-card__id">스터디 #{study.id}</span>
          </div>
          <h1>{study.title}</h1>
          <p>운영자 #{study.ownerId}</p>
        </header>

        <div className="study-detail-content">
          <div className="study-detail-main">
            <section aria-labelledby="study-description-heading">
              <h2 id="study-description-heading">스터디 소개</h2>
              <p className="study-description">{study.description}</p>
            </section>

            <section className="study-application-section" aria-labelledby="study-application-heading">
              <h2 id="study-application-heading">참여 신청</h2>
              {!user ? (
                <div className="application-notice">
                  <p>로그인하면 이 스터디에 참여 신청할 수 있습니다.</p>
                  <Link className="text-link" to="/login" state={{ from: `/studies/${study.id}` }}>
                    로그인하기
                  </Link>
                </div>
              ) : null}
              {user?.id === study.ownerId ? (
                <div className="application-notice"><p>내가 운영하는 스터디입니다.</p></div>
              ) : null}
              {user && user.id !== study.ownerId && study.status !== 'OPEN' ? (
                <div className="application-notice"><p>모집이 마감되어 신청할 수 없습니다.</p></div>
              ) : null}
              {user && accessToken && user.id !== study.ownerId && study.status === 'OPEN' ? (
                <StudyApplicationForm studyId={study.id} token={accessToken} />
              ) : null}
            </section>
          </div>

          <aside className="study-detail-summary" aria-label="모집 정보">
            <h2>모집 정보</h2>
            <dl>
              <div><dt>참여 인원</dt><dd>{study.approvedCount}명 / {study.capacity}명</dd></div>
              <div><dt>남은 자리</dt><dd>{remainingSeats}자리</dd></div>
              <div><dt>모집 마감</dt><dd>{formatDateTime(study.recruitmentDeadline)}</dd></div>
              <div><dt>개설일</dt><dd>{formatDateTime(study.createdAt)}</dd></div>
            </dl>
          </aside>
        </div>
      </article>
    </section>
  )
}
