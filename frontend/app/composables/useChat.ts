import type { ApiResponse } from '~/types/auth'
import type { ChatMessage, ChatPage, ChatRealtimePayload, ChatRoom } from '~/types/chat'
import { useAuth } from '~/composables/useAuth'

type ChatQuery = Record<string, string | number | boolean | null | undefined>
type TokenPayload = {
  publicId?: string
}

const emptyMessagePage = (): ChatPage<ChatMessage> => ({
  content: [],
  page: 0,
  size: 30,
  totalElements: 0,
  totalPages: 0,
})

const emptyRoomPage = (): ChatPage<ChatRoom> => ({
  content: [],
  page: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0,
})

export const useChat = () => {
  const config = useRuntimeConfig()
  const { accessToken, authFetch } = useAuth()
  const baseUrl = String(config.public.apiBaseUrl || 'http://localhost:8080').replace(/\/$/, '')

  const userRoom = useState<ChatRoom | null>('chat:user-room', () => null)
  const userMessages = useState<ChatMessage[]>('chat:user-messages', () => [])
  const userMessagesPage = useState<ChatPage<ChatMessage>>('chat:user-messages-page', emptyMessagePage)
  const adminRooms = useState<ChatRoom[]>('chat:admin-rooms', () => [])
  const adminRoomsPage = useState<ChatPage<ChatRoom>>('chat:admin-rooms-page', emptyRoomPage)
  const adminMessages = useState<ChatMessage[]>('chat:admin-messages', () => [])
  const adminMessagesPage = useState<ChatPage<ChatMessage>>('chat:admin-messages-page', emptyMessagePage)
  const activeAdminRoomPublicId = useState<string | null>('chat:active-admin-room', () => null)
  const loading = useState<boolean>('chat:loading', () => false)
  const sending = useState<boolean>('chat:sending', () => false)
  const error = useState<string | null>('chat:error', () => null)

  const currentPublicId = computed(() => readTokenPayload(accessToken.value)?.publicId || null)

  const request = async <T>(path: string, options: { method?: 'GET' | 'POST' | 'PATCH', body?: unknown, query?: ChatQuery } = {}) => {
    const cleanQuery = options.query
      ? Object.fromEntries(
          Object.entries(options.query).filter(([, value]) => value !== undefined && value !== null && value !== ''),
        )
      : undefined

    return authFetch<ApiResponse<T>>(`${baseUrl}/api/chat${path}`, {
      method: options.method || 'GET',
      body: options.body as Record<string, unknown> | undefined,
      query: cleanQuery,
    })
  }

  const fetchMyRoom = async () => {
    const response = await request<ChatRoom>('/me/room')
    userRoom.value = response.data
    return response.data
  }

  const fetchMyMessages = async (page = 0, size = 30) => {
    loading.value = true
    error.value = null
    try {
      const response = await request<ChatPage<ChatMessage>>('/me/messages', { query: { page, size } })
      const messages = response.data.content.map(normalizeMessage).reverse()
      userMessagesPage.value = { ...response.data, content: messages }
      userMessages.value = page === 0 ? messages : mergeMessages(messages, userMessages.value)
      return userMessagesPage.value
    }
    finally {
      loading.value = false
    }
  }

  const sendMyMessage = async (content: string) => {
    const text = content.trim()
    if (!text) return null

    sending.value = true
    try {
      const response = await request<ChatMessage>('/me/messages', {
        method: 'POST',
        body: { content: text },
      })
      upsertUserMessage(normalizeMessage(response.data))
      return response.data
    }
    finally {
      sending.value = false
    }
  }

  const markMyRoomRead = async () => {
    const response = await request<ChatRoom>('/me/read', { method: 'PATCH' })
    userRoom.value = response.data
    return response.data
  }

  const fetchAdminRooms = async (query: { page?: number, size?: number, keyword?: string } = {}) => {
    loading.value = true
    error.value = null
    try {
      const response = await request<ChatPage<ChatRoom>>('/admin/rooms', {
        query: {
          page: query.page ?? 0,
          size: query.size ?? 20,
          keyword: query.keyword,
        },
      })
      adminRoomsPage.value = response.data
      adminRooms.value = sortRooms(response.data.content)
      return response.data
    }
    finally {
      loading.value = false
    }
  }

  const fetchAdminMessages = async (roomPublicId: string, page = 0, size = 30) => {
    activeAdminRoomPublicId.value = roomPublicId
    loading.value = true
    error.value = null
    try {
      const response = await request<ChatPage<ChatMessage>>(`/admin/rooms/${encodeURIComponent(roomPublicId)}/messages`, {
        query: { page, size },
      })
      const messages = response.data.content.map(normalizeMessage).reverse()
      adminMessagesPage.value = { ...response.data, content: messages }
      adminMessages.value = page === 0 ? messages : mergeMessages(messages, adminMessages.value)
      return adminMessagesPage.value
    }
    finally {
      loading.value = false
    }
  }

  const sendAdminMessage = async (roomPublicId: string, content: string) => {
    const text = content.trim()
    if (!roomPublicId || !text) return null

    sending.value = true
    try {
      const response = await request<ChatMessage>(`/admin/rooms/${encodeURIComponent(roomPublicId)}/messages`, {
        method: 'POST',
        body: { content: text },
      })
      upsertAdminMessage(normalizeMessage(response.data))
      return response.data
    }
    finally {
      sending.value = false
    }
  }

  const markAdminRoomRead = async (roomPublicId: string) => {
    const response = await request<ChatRoom>(`/admin/rooms/${encodeURIComponent(roomPublicId)}/read`, {
      method: 'PATCH',
    })
    upsertAdminRoom(response.data)
    return response.data
  }

  const setActiveAdminRoom = (roomPublicId: string | null) => {
    activeAdminRoomPublicId.value = roomPublicId
    if (!roomPublicId) {
      adminMessages.value = []
      adminMessagesPage.value = emptyMessagePage()
    }
  }

  const applyRealtimePayload = (payload: ChatRealtimePayload) => {
    if (payload.room) {
      if (!userRoom.value || userRoom.value.publicId === payload.room.publicId) {
        userRoom.value = payload.room
      }
      upsertAdminRoom(payload.room)
    }

    if (!payload.message) return

    const message = normalizeMessage(payload.message)
    if (userRoom.value?.publicId === message.roomPublicId) {
      upsertUserMessage(message)
    }
    if (activeAdminRoomPublicId.value === message.roomPublicId) {
      upsertAdminMessage(message)
    }
  }

  const upsertUserMessage = (message: ChatMessage) => {
    userMessages.value = mergeMessages(userMessages.value, [message])
  }

  const upsertAdminMessage = (message: ChatMessage) => {
    adminMessages.value = mergeMessages(adminMessages.value, [message])
  }

  const upsertAdminRoom = (room: ChatRoom) => {
    adminRooms.value = sortRooms([
      room,
      ...adminRooms.value.filter(item => item.publicId !== room.publicId),
    ])
    adminRoomsPage.value = {
      ...adminRoomsPage.value,
      content: sortRooms([
        room,
        ...adminRoomsPage.value.content.filter(item => item.publicId !== room.publicId),
      ]),
      totalElements: Math.max(adminRoomsPage.value.totalElements, adminRooms.value.length),
    }
  }

  const normalizeMessage = (message: ChatMessage): ChatMessage => ({
    ...message,
    mine: Boolean(currentPublicId.value && message.senderPublicId === currentPublicId.value),
  })

  return {
    activeAdminRoomPublicId,
    adminMessages,
    adminMessagesPage,
    adminRooms,
    adminRoomsPage,
    applyRealtimePayload,
    error,
    fetchAdminMessages,
    fetchAdminRooms,
    fetchMyMessages,
    fetchMyRoom,
    loading,
    markAdminRoomRead,
    markMyRoomRead,
    sendAdminMessage,
    sendMyMessage,
    sending,
    setActiveAdminRoom,
    userMessages,
    userMessagesPage,
    userRoom,
  }
}

const mergeMessages = (left: ChatMessage[], right: ChatMessage[]) =>
  [...left, ...right]
    .filter((message, index, items) => items.findIndex(item => item.publicId === message.publicId) === index)
    .sort((a, b) => timestamp(a.createdAt) - timestamp(b.createdAt))

const sortRooms = (rooms: ChatRoom[]) =>
  [...rooms].sort((a, b) => roomTimestamp(b) - roomTimestamp(a))

const roomTimestamp = (room: ChatRoom) =>
  timestamp(room.lastMessageAt || room.updatedAt || room.createdAt || '')

const timestamp = (value: string | null) => {
  const time = value ? new Date(value).getTime() : 0
  return Number.isFinite(time) ? time : 0
}

const readTokenPayload = (token: string | null): TokenPayload | null => {
  if (!token || !import.meta.client) return null

  try {
    const payload = token.split('.')[1]
    if (!payload) return null
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/')
    const padded = normalized.padEnd(normalized.length + ((4 - normalized.length % 4) % 4), '=')
    return JSON.parse(atob(padded)) as TokenPayload
  }
  catch {
    return null
  }
}
