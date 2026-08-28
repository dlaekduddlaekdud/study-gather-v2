import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import {
  approveApplication,
  getStudyApplications,
  rejectApplication,
  type ApplicationListItem,
} from '../api/applications'
import { ApiError } from '../api/client'
import { getStudy, type StudyDetail } from '../api/studies'
import { useAuth } from '../auth/AuthContext'
import { ApplicationStatusBadge } from '../components/ApplicationStatusBadge'

type OwnerApplicationsState =
  | { status: 'loading' }
  | {
      status: 'success'
      requestId: number
      study: StudyDetail
      applications: ApplicationListItem[]
    }
  | { status: 'error'; requestId: number; message: string }

type Decision = 'approve' | 'reject'

const dateFormatter = new Intl.DateTimeFormat('ko-KR', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
})

function formatDate(value: string): string {
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? '날짜 정보 없음' : dateFormatter.format(date)
}

function getErrorMessage(error: unknown): string {
  return error instanceof ApiError
    ? error.message
    : '받은 신청 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.'
}

export function OwnerApplicationsPage() {
  const { studyId: studyIdParam } = useParams()
  const studyId = Number(studyIdParam)
  const isValidStudyId = Number.isSafeInteger(studyId) && studyId > 0
  const { accessToken } = useAuth()
  const [state, setState] = useState<OwnerApplicationsState>({ status: 'loading' })
  const [requestVersion, setRequestVersion] = useState(0)
  const [processing, setProcessing] = useState<{ id: number; decision: Decision } | null>(null)
  const [actionError, setActionError] = useState('')

  useEffect(() => {
    if (!accessToken || !isValidStudyId) return

    const controller = new AbortController()
    Promise.all([
      getStudy(studyId, controller.signal),
      getStudyApplications(studyId, accessToken, controller.signal),
    ])
      .then(([study, applications]) => {
        setState({ status: 'success', requestId: studyId, study, applications })
      })
      .catch((error: unknown) => {
        if (error instanceof DOMException && error.name === 'AbortError') return
        setState({ status: 'error', requestId: studyId, message: getErrorMessage(error) })
      })

    return () => controller.abort()
  }, [accessToken, isValidStudyId, requestVersion, studyId])

  const retry = () => {
    setState({ status: 'loading' })
    setRequestVersion((version) => version + 1)
  }

  const handleDecision = async (applicationId: number, decision: Decision) => {
    if (!accessToken || state.status !== 'success') return

    setActionError('')
    setProcessing({ id: applicationId, decision })
    try {
      const result = decision === 'approve'
        ? await approveApplication(applicationId, accessToken)
        : await rejectApplication(applicationId, accessToken)
      setState({
        ...state,
        study: decision === 'approve'
          ? { ...state.study, approvedCount: state.study.approvedCount + 1 }
          : state.study,
        applications: state.applications.map((application) =>
          application.id === applicationId
            ? { ...application, status: result.status, decidedAt: result.decidedAt }
            : application),
      })
    } catch (error) {
      setActionError(getErrorMessage(error))
    } finally {
      setProcessing(null)
    }
  }

  if (!isValidStudyId) {
    return (
      <section className="applications-page">
        <div className="study-state study-state--error" role="alert">
          <h1>잘못된 스터디 주소입니다</h1>
          <Link className="primary-button" to="/studies">목록으로 돌아가기</Link>
        </div>
      </section>
    )
  }

  if (state.status === 'loading' || state.requestId !== studyId) {
    return (
      <section className="applications-page">
        <div className="study-state" role="status">
          <div className="study-loading" aria-hidden="true" />
          <p>받은 신청 목록을 불러오고 있습니다.</p>
        </div>
      </section>
    )
  }

  if (state.status === 'error') {
    return (
      <section className="applications-page">
        <div className="study-state study-state--error" role="alert">
          <h1>신청 목록을 불러오지 못했어요</h1>
          <p>{state.message}</p>
          <button className="primary-button" type="button" onClick={retry}>다시 시도</button>
        </div>
      </section>
    )
  }

  return (
    <section className="applications-page">
      <Link className="study-detail-back" to={`/studies/${state.study.id}`}>← 스터디 상세</Link>
      <header className="applications-header owner-applications-header">
        <p className="eyebrow">APPLICATION MANAGEMENT</p>
        <h1>받은 참여 신청</h1>
        <p>{state.study.title} · 승인 {state.study.approvedCount}/{state.study.capacity}명</p>
      </header>

      {actionError ? <p className="form-error" role="alert">{actionError}</p> : null}

      {state.applications.length === 0 ? (
        <div className="study-state">
          <h2>아직 받은 신청이 없어요</h2>
          <p>새로운 참여 신청이 들어오면 이곳에서 확인할 수 있습니다.</p>
        </div>
      ) : (
        <div className="application-list">
          {state.applications.map((application) => {
            const isProcessing = processing?.id === application.id

            return (
              <article className="application-card" key={application.id}>
                <div className="application-card__heading">
                  <ApplicationStatusBadge status={application.status} />
                  <span>신청 #{application.id}</span>
                </div>
                <h2>{application.applicantNickname}</h2>
                <p className="application-card__applicant">신청자 #{application.applicantId}</p>
                <p className="application-card__message">
                  {application.message || '신청 메시지가 없습니다.'}
                </p>
                <div className="application-card__footer">
                  <time dateTime={application.createdAt}>{formatDate(application.createdAt)}</time>
                  {application.status === 'PENDING' ? (
                    <div className="application-decision-actions">
                      <button
                        className="application-reject-button"
                        type="button"
                        disabled={processing !== null}
                        onClick={() => handleDecision(application.id, 'reject')}
                      >
                        {isProcessing && processing.decision === 'reject' ? '거절 중...' : '거절'}
                      </button>
                      <button
                        className="application-approve-button"
                        type="button"
                        disabled={processing !== null}
                        onClick={() => handleDecision(application.id, 'approve')}
                      >
                        {isProcessing && processing.decision === 'approve' ? '승인 중...' : '승인'}
                      </button>
                    </div>
                  ) : null}
                </div>
              </article>
            )
          })}
        </div>
      )}
    </section>
  )
}
