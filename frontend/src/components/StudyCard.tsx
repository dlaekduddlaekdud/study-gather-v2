import { Link } from 'react-router-dom'
import type { StudySummary } from '../api/studies'

interface StudyCardProps {
  study: StudySummary
}

const deadlineFormatter = new Intl.DateTimeFormat('ko-KR', {
  month: 'long',
  day: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
})

function formatDeadline(deadline: string): string {
  const date = new Date(deadline)

  return Number.isNaN(date.getTime()) ? '마감일 미정' : deadlineFormatter.format(date)
}

export function StudyCard({ study }: StudyCardProps) {
  const remainingSeats = Math.max(study.capacity - study.approvedCount, 0)

  return (
    <Link
      className="study-card"
      to={`/studies/${study.id}`}
      aria-label={`${study.title} 상세 보기`}
    >
      <div className="study-card__heading">
        <span className="study-card__badge">모집 중</span>
        <span className="study-card__id">#{study.id}</span>
      </div>

      <h2>{study.title}</h2>

      <dl className="study-card__details">
        <div>
          <dt>모집 마감</dt>
          <dd>{formatDeadline(study.recruitmentDeadline)}</dd>
        </div>
        <div>
          <dt>현재 인원</dt>
          <dd>
            {study.approvedCount}명 / {study.capacity}명
          </dd>
        </div>
      </dl>

      <div className="study-card__capacity">
        <progress
          aria-label={`${study.title} 참여 인원`}
          max={Math.max(study.capacity, 1)}
          value={Math.min(study.approvedCount, study.capacity)}
        />
        <span>{remainingSeats > 0 ? `${remainingSeats}자리 남음` : '모집 인원 마감'}</span>
      </div>
    </Link>
  )
}
