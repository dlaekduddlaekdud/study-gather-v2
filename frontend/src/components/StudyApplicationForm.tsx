import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { createApplication } from '../api/applications'
import { ApiError } from '../api/client'

interface StudyApplicationFormProps {
  studyId: number
  token: string
}

export function StudyApplicationForm({ studyId, token }: StudyApplicationFormProps) {
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isComplete, setIsComplete] = useState(false)

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')

    if (!message.trim()) {
      setError('신청 메시지를 입력해 주세요.')
      return
    }

    setIsSubmitting(true)
    try {
      await createApplication(studyId, { message: message.trim() }, token)
      setIsComplete(true)
    } catch (submitError) {
      setError(submitError instanceof ApiError
        ? submitError.message
        : '서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.')
    } finally {
      setIsSubmitting(false)
    }
  }

  if (isComplete) {
    return (
      <div className="application-complete" role="status">
        <strong>참여 신청을 보냈습니다.</strong>
        <p>운영자가 확인하기 전까지 내 신청에서 취소할 수 있습니다.</p>
        <Link className="text-link" to="/applications/me">내 신청 확인하기</Link>
      </div>
    )
  }

  return (
    <form className="application-form" onSubmit={handleSubmit}>
      <label htmlFor="application-message">참여 신청 메시지</label>
      <textarea
        id="application-message"
        value={message}
        onChange={(event) => setMessage(event.target.value)}
        maxLength={500}
        rows={5}
        placeholder="참여하고 싶은 이유와 목표를 간단히 적어주세요."
        required
      />
      <span>{message.length}/500</span>
      {error ? <p className="form-error" role="alert">{error}</p> : null}
      <button className="submit-button" type="submit" disabled={isSubmitting}>
        {isSubmitting ? '신청하는 중...' : '참여 신청하기'}
      </button>
    </form>
  )
}
