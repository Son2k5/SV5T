import type { ChatRealtimePayload } from '~/types/chat'
import { useAuth } from '~/composables/useAuth'
import { useChat } from '~/composables/useChat'

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
let accessTokenWatcherStarted = false
let reconnectHandler: (() => void) | null = null
const desiredDestinations = new Set<string>()
const subscribedDestinations = new Map<string, string>()

export const useChatConnection = () => {
  const config = useRuntimeConfig()
  const { accessToken, ensureAccessToken } = useAuth()
  const { applyRealtimePayload } = useChat()

  const connected = useState<boolean>('chat:ws-connected', () => false)
  const connecting = useState<boolean>('chat:ws-connecting', () => false)
  const connectionError = useState<string | null>('chat:ws-error', () => null)

  const connect = async () => {
    if (!import.meta.client) return

    desiredConnection = true
    if (connected.value || connecting.value || socket?.readyState === WebSocket.OPEN) return

    const hasToken = await ensureAccessToken()
    if (!hasToken || !accessToken.value) {
      scheduleReconnect()
      return
    }

    connecting.value = true
    connectionError.value = null

    socket = new WebSocket(chatWebSocketUrl(String(config.public.apiBaseUrl || 'http://localhost:8080')))
    socket.onopen = () => {
      sendFrame('CONNECT', {
        'Authorization': `Bearer ${accessToken.value}`,
        'accept-version': '1.2',
        'heart-beat': '10000,10000',
      })
    }

    socket.onmessage = (event) => {
      if (typeof event.data !== 'string' || event.data === '\n') return
      parseFrames(event.data).forEach(handleFrame)
    }

    socket.onerror = () => {
      connectionError.value = 'Chat connection error'
    }

    socket.onclose = () => {
      stopHeartbeat()
      connected.value = false
      connecting.value = false
      subscribedDestinations.clear()
      socket = null
      if (desiredConnection && desiredDestinations.size > 0) scheduleReconnect()
    }
  }

  const disconnect = () => {
    desiredConnection = false
    clearReconnectTimer()
    stopHeartbeat()
    connected.value = false
    connecting.value = false
    subscribedDestinations.clear()

    if (socket && socket.readyState === WebSocket.OPEN) {
      sendFrame('DISCONNECT', { receipt: 'chat-disconnect' })
    }

    socket?.close()
    socket = null
  }

  const subscribe = (destination: string) => {
    if (!import.meta.client || !destination) return

    desiredDestinations.add(destination)
    desiredConnection = true
    if (connected.value) {
      subscribeNow(destination)
    }
    else {
      void connect()
    }
  }

  const unsubscribe = (destination: string) => {
    desiredDestinations.delete(destination)
    const subscriptionId = subscribedDestinations.get(destination)
    if (subscriptionId && socket?.readyState === WebSocket.OPEN) {
      sendFrame('UNSUBSCRIBE', { id: subscriptionId })
    }
    subscribedDestinations.delete(destination)

    if (desiredDestinations.size === 0) {
      disconnect()
    }
  }

  reconnectHandler = () => {
    void connect()
  }

  const handleFrame = (frame: StompFrame) => {
    if (frame.command === 'CONNECTED') {
      connected.value = true
      connecting.value = false
      reconnectAttempts = 0
      desiredDestinations.forEach(subscribeNow)
      startHeartbeat()
      return
    }

    if (frame.command === 'MESSAGE') {
      const payload = parseRealtimePayload(frame.body)
      if (payload) applyRealtimePayload(payload)
      return
    }

    if (frame.command === 'ERROR') {
      connectionError.value = frame.body || 'Chat connection refused'
      socket?.close()
    }
  }

  const subscribeNow = (destination: string) => {
    if (!socket || socket.readyState !== WebSocket.OPEN || subscribedDestinations.has(destination)) return

    const id = subscriptionId(destination)
    sendFrame('SUBSCRIBE', {
      id,
      destination,
      ack: 'auto',
    })
    subscribedDestinations.set(destination, id)
  }

  if (import.meta.client && !accessTokenWatcherStarted) {
    accessTokenWatcherStarted = true
    watch(accessToken, (token) => {
      if (!token) {
        desiredDestinations.clear()
        disconnect()
        return
      }

      if (desiredConnection && desiredDestinations.size > 0) {
        disconnect()
        desiredConnection = true
        void connect()
      }
    })
  }

  return {
    connect,
    connected,
    connecting,
    connectionError,
    disconnect,
    subscribe,
    unsubscribe,
  }
}

const chatWebSocketUrl = (apiBaseUrl: string) => {
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

const parseRealtimePayload = (body: string): ChatRealtimePayload | null => {
  try {
    return JSON.parse(body) as ChatRealtimePayload
  }
  catch {
    return null
  }
}

const subscriptionId = (destination: string) =>
  `chat-${destination.replace(/[^a-zA-Z0-9_-]/g, '-')}`

const scheduleReconnect = () => {
  clearReconnectTimer()
  if (!desiredConnection || desiredDestinations.size === 0) return

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
