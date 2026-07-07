export type NotificationType
  = | 'SUBMISSION_RECEIVED'
    | 'APPROVED'
    | 'REJECTED'
    | 'DEADLINE_REMINDER'
    | 'SYSTEM_ALERT'

export type NotificationChannel = 'EMAIL' | 'REALTIME' | 'BOTH'

export interface NotificationSettingDto {
  publicId: string | null
  smtpHost: string | null
  smtpPort: number | null
  smtpUsername: string | null
  smtpPassword: string | null
  passwordConfigured: boolean
  emailEnabled: boolean
  realtimeEnabled: boolean
  reminderDaysBeforeDeadline: number | null
  updatedBy: string | null
  updatedAt: string | null
}

export interface NotificationSettingForm {
  smtpHost: string
  smtpPort: number
  smtpUsername: string
  smtpPassword: string
  emailEnabled: boolean
  realtimeEnabled: boolean
  reminderDaysBeforeDeadline: number
}

export interface SmtpTestForm {
  smtpHost: string
  smtpPort: number
  smtpUsername: string
  smtpPassword: string
  testEmail: string
}

export interface NotificationTemplateDto {
  publicId: string | null
  code: NotificationType
  subject: string
  bodyTemplate: string
  channel: NotificationChannel
  active: boolean
  updatedAt: string | null
}

export interface NotificationTemplateForm {
  subject: string
  bodyTemplate: string
  channel: NotificationChannel
  active: boolean
}

export interface NotificationDto {
  publicId: string
  title: string
  content: string
  type: NotificationType
  read: boolean
  createdAt: string
  relatedEntityType: string | null
  relatedEntityId: string | null
}

export interface NotificationPage<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface RealtimeNotificationPayload {
  notification: NotificationDto
  unreadCount: number
}
