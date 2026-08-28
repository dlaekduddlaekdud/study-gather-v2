import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ApiError } from '../api/client'
import {
  getStudy,
  updateStudy,
  type StudyDetail,
  type UpdateStudyRequest,
} from '../api/studies'
import { useAuth } from '../auth/AuthContext'
import { toLocalDateTimeInput } from '../utils/dateTime'

type StudyEditState =
  | { status: 'loading' }
  | { status: 'success'; requestId: number; study: StudyDetail }
  | { status: 'error'; requestId: number; message: string }

interface StudyEditFormProps {
  study: StudyDetail
  token: string
}

function getLoadErrorMessage(error: unknown): string {
  return error instanceof ApiError
    ? error.message
    : '스터디 정보를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.'
}

function StudyEditForm({ study, token }: StudyEditFormProps) {
  const navigate = useNavigate()
  const originalDeadline = toLocalDateTimeInput(study.recruitmentDeadline)
  const [title, setTitle] = useState(study.title)
  const [description, setDescription] = useState(study.description)
  const [capacity, setCapacity] = useState(study.capacity)
  const [recruitmentDeadline, setRecruitmentDeadline] = useState(originalDeadline)
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')

    const trimmedTitle = title.trim()
    const trimmedDescription = description.trim()

    if (!trimmedTitle || !trimmedDescription) {
      setError('제목과 설명을 입력해 주세요.')
      return
    }

    if (capacity < Math.max(2, study.approvedCount)) {
      setError(`정원은 현재 참여 인원인 ${study.approvedCount}명보다 작을 수 없습니다.`)
      return
    }

    const request: UpdateStudyRequest = {}

    if (trimmedTitle !== study.title) request.title = trimmedTitle
    if (trimmedDescription !== study.description) request.description = trimmedDescription
    if (capacity !== study.capacity) request.capacity = capacity

    if (recruitmentDeadline !== originalDeadline) {
      if (!recruitmentDeadline || new Date(recruitmentDeadline).getTime() <= Date.now()) {
        setError('변경할 모집 마감일은 현재 시각 이후여야 합니다.')
        return
      }
      request.recruitmentDeadline = recruitmentDeadline
    }

    if (Object.keys(request).length === 0) {
      setError('변경된 내용이 없습니다.')
      return
    }

    setIsSubmitting(true)
    try {
      const updatedStudy = await updateStudy(study.id, request, token)
      navigate(`/studies/${updatedStudy.id}`, { replace: true })
    } catch (submitError) {
      setError(submitError instanceof ApiError
        ? submitError.message
        : '서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <form className="study-create-form" onSubmit={handleSubmit}>
      <div className="study-create-field">
        <label htmlFor="study-title">스터디 제목</label>
        <input
          id="study-title"
          value={title}
          onChange={(event) => setTitle(event.target.value)}
          maxLength={100}
          required
        />
        <span>{title.length}/100</span>
      </div>

      <div className="study-create-field">
        <label htmlFor="study-description">스터디 소개</label>
        <textarea
          id="study-description"
          value={description}
          onChange={(event) => setDescription(event.target.value)}
          maxLength={2000}
          rows={9}
          required
        />
        <span>{description.length}/2000</span>
      </div>

      <div className="study-create-row">
        <div className="study-create-field">
          <label htmlFor="study-capacity">모집 정원</label>
          <input
            id="study-capacity"
            type="number"
            value={capacity}
            onChange={(event) => setCapacity(Number(event.target.value))}
            min={Math.max(2, study.approvedCount)}
            required
          />
          <span>현재 참여 인원 {study.approvedCount}명 이상</span>
        </div>

        <div className="study-create-field">
          <label htmlFor="study-deadline">모집 마감일</label>
          <input
            id="study-deadline"
            type="datetime-local"
            value={recruitmentDeadline}
            onChange={(event) => setRecruitmentDeadline(event.target.value)}
            required
          />
          <span>변경할 경우 현재 시각 이후로 선택해 주세요.</span>
        </div>
      </div>

      {error ? <p className="form-error" role="alert">{error}</p> : null}

      <div className="study-edit-actions">
        <Link className="secondary-button" to={`/studies/${study.id}`}>취소</Link>
        <button className="submit-button" type="submit" disabled={isSubmitting}>
          {isSubmitting ? '수정하는 중...' : '변경 내용 저장'}
        </button>
      </div>
    </form>
  )
}

export function StudyEditPage() {
  const { user, accessToken } = useAuth()
  const { studyId: studyIdParam } = useParams()
  const studyId = Number(studyIdParam)
  const isValidStudyId = Number.isSafeInteger(studyId) && studyId > 0
  const [state, setState] = useState<StudyEditState>({ status: 'loading' })
  const [requestVersion, setRequestVersion] = useState(0)

  useEffect(() => {
    if (!isValidStudyId) return

    const controller = new AbortController()

    getStudy(studyId, controller.signal)
      .then((study) => setState({ status: 'success', requestId: studyId, study }))
      .catch((error: unknown) => {
        if (error instanceof DOMException && error.name === 'AbortError') return
        setState({ status: 'error', requestId: studyId, message: getLoadErrorMessage(error) })
      })

    return () => controller.abort()
  }, [isValidStudyId, requestVersion, studyId])

  const retry = () => {
    setState({ status: 'loading' })
    setRequestVersion((version) => version + 1)
  }

  if (!isValidStudyId) {
    return (
      <section className="study-create-page">
        <div className="study-state study-state--error" role="alert">
          <h1>잘못된 스터디 주소입니다</h1>
          <Link className="primary-button" to="/studies">목록으로 돌아가기</Link>
        </div>
      </section>
    )
  }

  if (state.status === 'loading' || state.requestId !== studyId) {
    return (
      <section className="study-create-page">
        <div className="study-state" role="status">
          <div className="study-loading" aria-hidden="true" />
          <p>수정할 스터디 정보를 불러오고 있습니다.</p>
        </div>
      </section>
    )
  }

  if (state.status === 'error') {
    return (
      <section className="study-create-page">
        <div className="study-state study-state--error" role="alert">
          <h1>스터디를 불러오지 못했어요</h1>
          <p>{state.message}</p>
          <button className="primary-button" type="button" onClick={retry}>다시 시도</button>
        </div>
      </section>
    )
  }

  if (!user || user.id !== state.study.ownerId) {
    return (
      <section className="study-create-page">
        <div className="study-state study-state--error" role="alert">
          <h1>수정 권한이 없습니다</h1>
          <p>스터디 운영자만 정보를 수정할 수 있습니다.</p>
          <Link className="primary-button" to={`/studies/${studyId}`}>상세로 돌아가기</Link>
        </div>
      </section>
    )
  }

  if (!accessToken) {
    return null
  }

  return (
    <section className="study-create-page">
      <header className="study-create-intro">
        <p className="eyebrow">EDIT STUDY</p>
        <h1>스터디 정보 수정</h1>
        <p>현재 참여 인원을 고려해 모집 정보와 스터디 소개를 관리하세요.</p>
      </header>
      <StudyEditForm key={state.study.id} study={state.study} token={accessToken} />
    </section>
  )
}
