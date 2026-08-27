import { useState, type FormEvent } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import { createStudy } from '../api/studies'
import { useAuth } from '../auth/AuthContext'

function toLocalDateTimeInput(date: Date): string {
  const pad = (value: number) => String(value).padStart(2, '0')

  return [
    date.getFullYear(),
    '-',
    pad(date.getMonth() + 1),
    '-',
    pad(date.getDate()),
    'T',
    pad(date.getHours()),
    ':',
    pad(date.getMinutes()),
  ].join('')
}

export function StudyCreatePage() {
  const { accessToken } = useAuth()
  const navigate = useNavigate()
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [capacity, setCapacity] = useState(2)
  const [recruitmentDeadline, setRecruitmentDeadline] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  if (!accessToken) {
    return <Navigate to="/login" state={{ from: '/studies/new' }} replace />
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')

    if (!title.trim() || !description.trim()) {
      setError('제목과 설명을 입력해 주세요.')
      return
    }

    if (capacity < 2) {
      setError('정원은 2명 이상이어야 합니다.')
      return
    }

    if (!recruitmentDeadline || new Date(recruitmentDeadline).getTime() <= Date.now()) {
      setError('모집 마감일은 현재 시각 이후여야 합니다.')
      return
    }

    setIsSubmitting(true)
    try {
      const study = await createStudy({
        title: title.trim(),
        description: description.trim(),
        capacity,
        recruitmentDeadline,
      }, accessToken)
      navigate(`/studies/${study.id}`, { replace: true })
    } catch (submitError) {
      setError(submitError instanceof ApiError
        ? submitError.message
        : '서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.')
    } finally {
      setIsSubmitting(false)
    }
  }

  const minimumDeadline = new Date()
  minimumDeadline.setMinutes(minimumDeadline.getMinutes() + 1)

  return (
    <section className="study-create-page">
      <header className="study-create-intro">
        <p className="eyebrow">CREATE A STUDY</p>
        <h1>함께 공부할 사람들을 모아보세요</h1>
        <p>목표와 모집 정보를 입력하면 바로 스터디를 시작할 수 있습니다.</p>
      </header>

      <form className="study-create-form" onSubmit={handleSubmit}>
        <div className="study-create-field">
          <label htmlFor="study-title">스터디 제목</label>
          <input
            id="study-title"
            value={title}
            onChange={(event) => setTitle(event.target.value)}
            maxLength={100}
            placeholder="예: 매주 함께하는 Spring Boot 스터디"
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
            placeholder="학습 목표, 진행 방식, 함께하고 싶은 사람을 소개해 주세요."
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
              min={2}
              required
            />
            <span>개설자를 포함해 2명 이상</span>
          </div>

          <div className="study-create-field">
            <label htmlFor="study-deadline">모집 마감일</label>
            <input
              id="study-deadline"
              type="datetime-local"
              value={recruitmentDeadline}
              onChange={(event) => setRecruitmentDeadline(event.target.value)}
              min={toLocalDateTimeInput(minimumDeadline)}
              required
            />
            <span>현재 시각 이후로 선택해 주세요.</span>
          </div>
        </div>

        {error ? <p className="form-error" role="alert">{error}</p> : null}

        <button className="submit-button" type="submit" disabled={isSubmitting}>
          {isSubmitting ? '스터디 만드는 중...' : '스터디 만들기'}
        </button>
      </form>
    </section>
  )
}
