import type { ApiResponse } from '~/types/auth'
import type {
  NotificationDto,
  NotificationPage,
  NotificationType,
  RealtimeNotificationPayload,
} from '~/types/notification'
import { useAuth } from '~/composables/useAuth'

type NotificationQuery = {
  page?: number
  size?: number
  type?: NotificationType | null
}

const emptyPage = (): NotificationPage<NotificationDto> => ({
  content: [],
  page: 0,
  size: 10,
  totalElements: 0,
  totalPages: 0,
})

export const useNotifications = () => {
  const config = useRuntimeConfig()
  const { authFetch } = useAuth()
  const baseUrl = String(config.public.apiBaseUrl || 'http://localhost:8080').replace(/\/$/, '')

  const notifications = useState<NotificationDto[]>('notifications:list', () => [])
  const latestNotifications = useState<NotificationDto[]>('notifications:latest', () => [])
  const pageState = useState<NotificationPage<NotificationDto>>('notifications:page', emptyPage)
  const unreadCount = useState<number>('notifications:unread-count', () => 0)
  const loading = useState<boolean>('notifications:loading', () => false)
  const error = useState<string | null>('notifications:error', () => null)

  const fetchNotifications = async (query: NotificationQuery = {}) => {
    loading.value = true
    error.value = null

    try {
      const cleanQuery = Object.fromEntries(
        Object.entries({
          page: query.page ?? 0,
          size: query.size ?? 10,
          type: query.type,
        }).filter(([, value]) => value !== undefined && value !== null && value !== ''),
      )

      const response = await authFetch<ApiResponse<NotificationPage<NotificationDto>>>(`${baseUrl}/api/notifications`, {
        method: 'GET',
        query: cleanQuery,
      })

      pageState.value = response.data
      notifications.value = response.data.content
      latestNotifications.value = response.data.content.slice(0, 5)
      return response.data
    }
    catch (caughtError) {
      error.value = 'Khong the tai danh sach thong bao'
      throw caughtError
    }
    finally {
      loading.value = false
    }
  }

  const fetchUnreadCount = async () => {
    const response = await authFetch<ApiResponse<number>>(`${baseUrl}/api/notifications/unread-count`, {
      method: 'GET',
    })
    unreadCount.value = response.data
    return response.data
  }

  const markAsRead = async (publicId: string) => {
    const wasUnread = notifications.value.find(notification => notification.publicId === publicId)?.read === false
    const response = await authFetch<ApiResponse<NotificationDto>>(`${baseUrl}/api/notifications/${encodeURIComponent(publicId)}/read`, {
      method: 'PATCH',
    })

    updateNotification(response.data)
    if (wasUnread && response.data.read) {
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }

    return response.data
  }

  const markAllAsRead = async () => {
    await authFetch<ApiResponse<undefined>>(`${baseUrl}/api/notifications/read-all`, {
      method: 'PATCH',
    })

    notifications.value = notifications.value.map(notification => ({ ...notification, read: true }))
    latestNotifications.value = latestNotifications.value.map(notification => ({ ...notification, read: true }))
    pageState.value = {
      ...pageState.value,
      content: pageState.value.content.map(notification => ({ ...notification, read: true })),
    }
    unreadCount.value = 0
  }

  const applyRealtimeNotification = (payload: RealtimeNotificationPayload) => {
    const notification = payload.notification
    unreadCount.value = payload.unreadCount
    notifications.value = upsertFirst(notifications.value, notification)
    latestNotifications.value = upsertFirst(latestNotifications.value, notification).slice(0, 5)
    pageState.value = {
      ...pageState.value,
      totalElements: Math.max(pageState.value.totalElements, notifications.value.length),
      content: upsertFirst(pageState.value.content, notification),
    }
  }

  const updateNotification = (notification: NotificationDto) => {
    notifications.value = replaceByPublicId(notifications.value, notification)
    latestNotifications.value = replaceByPublicId(latestNotifications.value, notification)
    pageState.value = {
      ...pageState.value,
      content: replaceByPublicId(pageState.value.content, notification),
    }
  }

  return {
    applyRealtimeNotification,
    error,
    fetchNotifications,
    fetchUnreadCount,
    latestNotifications,
    loading,
    markAllAsRead,
    markAsRead,
    notifications,
    pageState,
    unreadCount,
  }
}

const upsertFirst = (items: NotificationDto[], notification: NotificationDto) => [
  notification,
  ...items.filter(item => item.publicId !== notification.publicId),
]

const replaceByPublicId = (items: NotificationDto[], notification: NotificationDto) =>
  items.map(item => item.publicId === notification.publicId ? notification : item)
