export type ChatRole = 'ADMIN' | 'MENTOR' | 'USER'

export interface ChatRoom {
  publicId: string
  userPublicId: string
  userName: string
  userEmail: string
  userAvatar: string | null
  lastMessage: string | null
  lastMessageAt: string | null
  unreadForUser: number
  unreadForAdmin: number
  createdAt: string | null
  updatedAt: string | null
}

export interface ChatMessage {
  publicId: string
  roomPublicId: string
  senderPublicId: string
  senderName: string
  senderEmail: string
  senderRole: ChatRole
  mine: boolean
  content: string
  createdAt: string
}

export interface ChatPage<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface ChatRealtimePayload {
  type: 'MESSAGE_CREATED' | 'ROOM_UPDATED'
  room: ChatRoom
  message: ChatMessage | null
}
