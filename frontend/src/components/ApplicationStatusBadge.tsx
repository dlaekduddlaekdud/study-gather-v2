import type { ApplicationStatus } from '../api/applications'

interface ApplicationStatusBadgeProps {
  status: ApplicationStatus
}

const statusLabels: Record<ApplicationStatus, string> = {
  PENDING: '승인 대기',
  APPROVED: '승인 완료',
  REJECTED: '승인 거절',
  CANCELED: '신청 취소',
}

export function ApplicationStatusBadge({ status }: ApplicationStatusBadgeProps) {
  return (
    <span className={`application-status application-status--${status.toLowerCase()}`}>
      {statusLabels[status]}
    </span>
  )
}
