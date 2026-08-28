import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ApiError } from '../api/client'
import {
  getStudy,
  getStudyMembers,
  type StudyDetail,
  type StudyMember,
} from '../api/studies'
import { useAuth } from '../auth/AuthContext'

type StudyMembersState =
  | { status: 'loading' }
  | { status: 'success'; requestId: number; study: StudyDetail; members: StudyMember[] }
  | { status: 'error'; requestId: number; message: string }

const dateFormatter = new Intl.DateTimeFormat('ko-KR', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
})

function formatDate(value: string): string {
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? '날짜 정보 없음' : dateFormatter.format(date)
}

function getErrorMessage(error: unknown): string {
  return error instanceof ApiError
    ? error.message
    : '스터디 멤버 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.'
}

export function StudyMembersPage() {
  const { studyId: studyIdParam } = useParams()
  const studyId = Number(studyIdParam)
  const isValidStudyId = Number.isSafeInteger(studyId) && studyId > 0
  const { accessToken } = useAuth()
  const [state, setState] = useState<StudyMembersState>({ status: 'loading' })
  const [requestVersion, setRequestVersion] = useState(0)

  useEffect(() => {
    if (!accessToken || !isValidStudyId) return

    const controller = new AbortController()
    Promise.all([
      getStudy(studyId, controller.signal),
      getStudyMembers(studyId, accessToken, controller.signal),
    ])
      .then(([study, members]) => {
        setState({ status: 'success', requestId: studyId, study, members })
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

  if (!isValidStudyId) {
    return (
      <section className="members-page">
        <div className="study-state study-state--error" role="alert">
          <h1>잘못된 스터디 주소입니다</h1>
          <Link className="primary-button" to="/studies">목록으로 돌아가기</Link>
        </div>
      </section>
    )
  }

  if (state.status === 'loading' || state.requestId !== studyId) {
    return (
      <section className="members-page">
        <div className="study-state" role="status">
          <div className="study-loading" aria-hidden="true" />
          <p>스터디 멤버를 불러오고 있습니다.</p>
        </div>
      </section>
    )
  }

  if (state.status === 'error') {
    return (
      <section className="members-page">
        <div className="study-state study-state--error" role="alert">
          <h1>멤버 목록을 불러오지 못했어요</h1>
          <p>{state.message}</p>
          <button className="primary-button" type="button" onClick={retry}>다시 시도</button>
        </div>
      </section>
    )
  }

  return (
    <section className="members-page">
      <Link className="study-detail-back" to={`/studies/${state.study.id}`}>← 스터디 상세</Link>
      <header className="members-header">
        <p className="eyebrow">STUDY MEMBERS</p>
        <h1>함께하는 멤버</h1>
        <p>{state.study.title} · {state.members.length}/{state.study.capacity}명</p>
      </header>

      <div className="member-list">
        {state.members.map((member) => (
          <article className="member-card" key={member.memberId}>
            <div className="member-avatar" aria-hidden="true">
              {member.nickname.charAt(0).toUpperCase()}
            </div>
            <div className="member-card__content">
              <div>
                <h2>{member.nickname}</h2>
                <span className={`member-role member-role--${member.memberRole.toLowerCase()}`}>
                  {member.memberRole === 'OWNER' ? '운영자' : '멤버'}
                </span>
              </div>
              <p>사용자 #{member.userId} · {formatDate(member.joinedAt)} 참여</p>
            </div>
          </article>
        ))}
      </div>
    </section>
  )
}
