import type { RealtimeNotificationPayload } from '~/types/notification'
import { useAuth } from '~/composables/useAuth'
import { useNotifications } from '~/composables/useNotifications'

type TokenPayload = {
  userId?: number | string
  exp?: number
}

type StompFrame = {
  command: string
  headers: Record<string, string>
  body: string
}

let socket: WebSocket | null = null
let reconnectTimer: ReturnType<typeof setTimeout> | null = null
let heartbeatTimer: ReturnType<typeof setInterval> | null = null
let reconnectAttempts = 0
let desiredConnection = false
let subscribedUserId: number | null = null
let reconnectHandler: (() => void) | null = null

export const useNotificationConnection = () => {
  const config = useRuntimeConfig()
  const { accessToken, ensureAccessToken } = useAuth()
  const { applyRealtimeNotification, fetchUnreadCount } = useNotifications()

  const connected = useState<boolean>('notifications:ws-connected', () => false)
  const connecting = useState<boolean>('notifications:ws-connecting', () => false)
  const connectionError = useState<string | null>('notifications:ws-error', () => null)

  const connect = async () => {
    if (!import.meta.client) return

    desiredConnection = true
    if (connected.value || connecting.value || socket?.readyState === WebSocket.OPEN) return

    const hasToken = await ensureAccessToken()
    if (!hasToken || !accessToken.value) {
      scheduleReconnect()
      return
    }

    const userId = getUserId(accessToken.value)
    if (!userId) {
      connectionError.value = 'Missing notification user id'
      scheduleReconnect()
      return
    }

    connecting.value = true
    connectionError.value = null

    socket = new WebSocket(notificationWebSocketUrl(String(config.public.apiBaseUrl || 'http://localhost:8080')))

    socket.onopen = () => {
      sendFrame('CONNECT', {
        Authorization: `Bearer ${accessToken.value}`,
        'accept-version': '1.2',
        'heart-beat': '10000,10000',
      })
    }

    socket.onmessage = (event) => {
      if (typeof event.data !== 'string' || event.data === '\n') return
      parseFrames(event.data).forEach((frame) => handleFrame(frame, userId))
    }

    socket.onerror = () => {
      connectionError.value = 'Notification connection error'
    }

    socket.onclose = () => {
      stopHeartbeat()
      connected.value = false
      connecting.value = false
      subscribedUserId = null
      socket = null
      if (desiredConnection) scheduleReconnect()
    }
  }

  const disconnect = () => {
    desiredConnection = false
    clearReconnectTimer()
    stopHeartbeat()
    connected.value = false
    connecting.value = false
    subscribedUserId = null

    if (socket && socket.readyState === WebSocket.OPEN) {
      sendFrame('DISCONNECT', { receipt: 'disconnect' })
    }

    socket?.close()
    socket = null
  }

  const reconnect = () => {
    disconnect()
    desiredConnection = true
    void connect()
  }

  reconnectHandler = () => {
    void connect()
  }

  if (import.meta.client) {
    watch(accessToken, (token, previousToken) => {
      if (!token) {
        disconnect()
        return
      }

      const userId = getUserId(token)
      const previousUserId = getUserId(previousToken)
      if (desiredConnection && userId && previousUserId && userId !== previousUserId) {
        reconnect()
      }
    })
  }

  const handleFrame = (frame: StompFrame, userId: number) => {
    if (frame.command === 'CONNECTED') {
      connected.value = true
      connecting.value = false
      reconnectAttempts = 0
      subscribe(userId)
      startHeartbeat()
      void fetchUnreadCount().catch(() => {
        // Realtime still works if the count refresh fails once.
      })
      return
    }

    if (frame.command === 'MESSAGE') {
      const payload = parseRealtimePayload(frame.body)
      if (payload) applyRealtimeNotification(payload)
      return
    }

    if (frame.command === 'ERROR') {
      connectionError.value = frame.body || 'Notification connection refused'
      socket?.close()
    }
  }

  const subscribe = (userId: number) => {
    if (subscribedUserId === userId) return

    sendFrame('SUBSCRIBE', {
      id: `notifications-${userId}`,
      destination: `/topic/notifications/${userId}`,
      ack: 'auto',
    })
    subscribedUserId = userId
  }

  return {
    connect,
    connected,
    connecting,
    connectionError,
    disconnect,
    reconnect,
  }
}

const notificationWebSocketUrl = (apiBaseUrl: string) => {
  const base = apiBaseUrl.replace(/\/$/, '')
  const wsBase = base
    .replace(/^https:\/\//, 'wss://')
    .replace(/^http:\/\//, 'ws://')
  return `${wsBase}/ws/notifications-native`
}

const sendFrame = (command: string, headers: Record<string, string> = {}, body = '') => {
  if (!socket || socket.readyState !== WebSocket.OPEN) return

  const headerLines = Object.entries(headers).map(([key, value]) => `${key}:${value}`)
  socket.send([command, ...headerLines, '', body].join('\n') + '\0')
}

const parseFrames = (rawMessage: string): StompFrame[] =>
  rawMessage
    .split('\0')
    .map(rawFrame => rawFrame.trim())
    .filter(Boolean)
    .map(parseFrame)

const parseFrame = (rawFrame: string): StompFrame => {
  const [head = '', ...bodyParts] = rawFrame.split('\n\n')
  const [command = '', ...headerLines] = head.split('\n')
  const headers = Object.fromEntries(
    headerLines
      .map((line) => {
        const separatorIndex = line.indexOf(':')
        return separatorIndex === -1
          ? null
          : [line.slice(0, separatorIndex), line.slice(separatorIndex + 1)]
      })
      .filter((entry): entry is [string, string] => Array.isArray(entry)),
  )

  return {
    command,
    headers,
    body: bodyParts.join('\n\n'),
  }
}

const parseRealtimePayload = (body: string): RealtimeNotificationPayload | null => {
  try {
    return JSON.parse(body) as RealtimeNotificationPayload
  }
  catch {
    return null
  }
}

const getUserId = (token: string | null): number | null => {
  const payload = readTokenPayload(token)
  if (!payload?.userId) return null

  const userId = Number(payload.userId)
  return Number.isFinite(userId) && userId > 0 ? userId : null
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

const scheduleReconnect = () => {
  clearReconnectTimer()
  if (!desiredConnection) return

  const delay = Math.min(30000, 1000 * 2 ** reconnectAttempts)
  reconnectAttempts += 1
  reconnectTimer = setTimeout(() => {
    reconnectHandler?.()
  }, delay)
}

const clearReconnectTimer = () => {
  if (!reconnectTimer) return

  clearTimeout(reconnectTimer)
  reconnectTimer = null
}

const startHeartbeat = () => {
  stopHeartbeat()
  heartbeatTimer = setInterval(() => {
    if (socket?.readyState === WebSocket.OPEN) {
      socket.send('\n')
    }
  }, 10000)
}

const stopHeartbeat = () => {
  if (!heartbeatTimer) return

  clearInterval(heartbeatTimer)
  heartbeatTimer = null
}
