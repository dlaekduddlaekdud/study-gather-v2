import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  cancelApplication,
  getMyApplications,
  type ApplicationListItem,
} from '../api/applications'
import { ApiError } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { ApplicationStatusBadge } from '../components/ApplicationStatusBadge'

type ApplicationsState =
  | { status: 'loading' }
  | { status: 'success'; applications: ApplicationListItem[] }
  | { status: 'error'; message: string }

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
    : '내 신청 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.'
}

export function MyApplicationsPage() {
  const { accessToken } = useAuth()
  const [state, setState] = useState<ApplicationsState>({ status: 'loading' })
  const [requestVersion, setRequestVersion] = useState(0)
  const [cancelingId, setCancelingId] = useState<number | null>(null)
  const [actionError, setActionError] = useState('')

  useEffect(() => {
    if (!accessToken) return

    const controller = new AbortController()
    getMyApplications(accessToken, controller.signal)
      .then((applications) => setState({ status: 'success', applications }))
      .catch((error: unknown) => {
        if (error instanceof DOMException && error.name === 'AbortError') return
        setState({ status: 'error', message: getErrorMessage(error) })
      })

    return () => controller.abort()
  }, [accessToken, requestVersion])

  const retry = () => {
    setState({ status: 'loading' })
    setRequestVersion((version) => version + 1)
  }

  const handleCancel = async (applicationId: number) => {
    if (!accessToken || state.status !== 'success') return

    setActionError('')
    setCancelingId(applicationId)
    try {
      const result = await cancelApplication(applicationId, accessToken)
      setState({
        status: 'success',
        applications: state.applications.map((application) =>
          application.id === applicationId
            ? { ...application, status: result.status, decidedAt: result.decidedAt }
            : application),
      })
    } catch (error) {
      setActionError(getErrorMessage(error))
    } finally {
      setCancelingId(null)
    }
  }

  return (
    <section className="applications-page">
      <header className="applications-header">
        <p className="eyebrow">MY APPLICATIONS</p>
        <h1>내 참여 신청</h1>
        <p>신청 상태를 확인하고 승인 대기 중인 신청을 취소할 수 있습니다.</p>
      </header>

      {actionError ? <p className="form-error" role="alert">{actionError}</p> : null}

      {state.status === 'loading' ? (
        <div className="study-state" role="status">
          <div className="study-loading" aria-hidden="true" />
          <p>내 신청 목록을 불러오고 있습니다.</p>
        </div>
      ) : null}

      {state.status === 'error' ? (
        <div className="study-state study-state--error" role="alert">
          <h2>신청 목록을 불러오지 못했어요</h2>
          <p>{state.message}</p>
          <button className="primary-button" type="button" onClick={retry}>다시 시도</button>
        </div>
      ) : null}

      {state.status === 'success' && state.applications.length === 0 ? (
        <div className="study-state">
          <h2>아직 참여 신청이 없어요</h2>
          <p>관심 있는 스터디를 찾아 첫 신청을 보내보세요.</p>
          <Link className="primary-button" to="/studies">스터디 찾기</Link>
        </div>
      ) : null}

      {state.status === 'success' && state.applications.length > 0 ? (
        <div className="application-list">
          {state.applications.map((application) => (
            <article className="application-card" key={application.id}>
              <div className="application-card__heading">
                <ApplicationStatusBadge status={application.status} />
                <span>신청 #{application.id}</span>
              </div>
              <h2><Link to={`/studies/${application.studyId}`}>{application.studyTitle}</Link></h2>
              <p className="application-card__message">
                {application.message || '신청 메시지가 없습니다.'}
              </p>
              <div className="application-card__footer">
                <time dateTime={application.createdAt}>{formatDate(application.createdAt)}</time>
                {application.status === 'PENDING' ? (
                  <button
                    className="application-cancel-button"
                    type="button"
                    disabled={cancelingId === application.id}
                    onClick={() => handleCancel(application.id)}
                  >
                    {cancelingId === application.id ? '취소 중...' : '신청 취소'}
                  </button>
                ) : null}
              </div>
            </article>
          ))}
        </div>
      ) : null}
    </section>
  )
}
