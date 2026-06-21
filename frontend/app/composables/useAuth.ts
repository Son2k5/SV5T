import type { ApiResponse, LogInPayload, LoginResponse, RegisterPayload, ResetPasswordPayload } from '~/types/auth'
import type { H3Event } from 'h3'
import { LogInEndpoint, LogOutEndpoint, MissingPasswordEndpoint, refreshAccessTokenEndpoint, RegisterEndpoint, ResetPasswordEndpoint, VerifyResetEndpoint } from '~/constants/endpoints'
import { getErrorMessage } from '~/utils/errors'

type AuthFetchOptions = {
  method?: string
  headers?: HeadersInit
  body?: unknown
  query?: Record<string, unknown>
  credentials?: RequestCredentials
  [key: string]: unknown
}

type FetchErrorLike = {
  status?: number
  statusCode?: number
  response?: {
    status?: number
    headers?: Headers
  }
  data?: {
    message?: string
  }
  message?: string
}

type AuthClientError = Error & {
  status: number
  statusCode: number
  data: {
    message: string
  }
}

const ACCESS_TOKEN_KEY = 'accessToken'
const LAST_ACTIVITY_KEY = 'lastActivityAt'
const IDLE_TIMEOUT_MS = 3 * 60 * 60 * 1000
const ACTIVITY_WRITE_INTERVAL_MS = 60 * 1000
const SERVER_ACTIVITY_SYNC_INTERVAL_MS = 50 * 60 * 1000
const TOKEN_REFRESH_BUFFER_MS = 60 * 1000
const SESSION_EXPIRED_MESSAGE = 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.'

let refreshPromise: Promise<boolean> | null = null
let idleTimer: ReturnType<typeof setTimeout> | null = null
let idleWatcherStarted = false
let lastActivityWriteAt = 0
let lastServerActivitySyncAt = 0

const getErrorStatus = (error: unknown) => {
  if (typeof error !== 'object' || error === null) return undefined

  const apiError = error as FetchErrorLike
  return apiError.statusCode ?? apiError.status ?? apiError.response?.status
}

const createAuthError = (message = SESSION_EXPIRED_MESSAGE): AuthClientError =>
  Object.assign(new Error(message), {
    status: 401,
    statusCode: 401,
    data: { message },
  })

const decodeJwtPayload = (token: string) => {
  if (typeof atob !== 'function') return null

  const [, payload] = token.split('.')
  if (!payload) return null

  try {
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/')
    const padded = normalized.padEnd(normalized.length + ((4 - normalized.length % 4) % 4), '=')
    return JSON.parse(atob(padded)) as { exp?: number }
  }
  catch {
    return null
  }
}

const isAccessTokenExpiring = (token: string) => {
  const payload = decodeJwtPayload(token)
  if (!payload?.exp) return true

  return payload.exp * 1000 <= Date.now() + TOKEN_REFRESH_BUFFER_MS
}

const getStoredLastActivityAt = () => {
  if (!import.meta.client) return null

  const value = Number(localStorage.getItem(LAST_ACTIVITY_KEY))
  return Number.isFinite(value) && value > 0 ? value : null
}

const forwardSetCookie = async (headers: Headers, event?: H3Event | null) => {
  if (!import.meta.server || !event) return

  const setCookie = headers.get('set-cookie')
  if (!setCookie) return

  const { appendResponseHeader } = await import('h3')
  appendResponseHeader(event, 'set-cookie', setCookie)
}

const clearIdleTimer = () => {
  if (idleTimer) {
    clearTimeout(idleTimer)
    idleTimer = null
  }
}

export const useAuth = () => {
  const toast = useToast()
  const requestEvent = import.meta.server ? useRequestEvent() : null
  const requestHeaders = import.meta.server ? useRequestHeaders(['cookie']) : undefined

  const accessToken = useState<string | null>('accessToken', () => null)

  if (import.meta.client && !accessToken.value) {
    accessToken.value = sessionStorage.getItem(ACCESS_TOKEN_KEY)
  }

  const setAccessToken = (token: string | null) => {
    accessToken.value = token

    if (!import.meta.client) return

    if (token) {
      sessionStorage.setItem(ACCESS_TOKEN_KEY, token)
    }
    else {
      sessionStorage.removeItem(ACCESS_TOKEN_KEY)
      clearIdleTimer()
    }
  }

  const hasIdleExpired = () => {
    const lastActivityAt = getStoredLastActivityAt()
    return Boolean(lastActivityAt && Date.now() - lastActivityAt >= IDLE_TIMEOUT_MS)
  }

  const markServerActivitySynced = () => {
    if (!import.meta.client) return

    lastServerActivitySyncAt = Date.now()
  }

  const scheduleIdleLogout = () => {
    if (!import.meta.client || !accessToken.value) return

    clearIdleTimer()

    const lastActivityAt = getStoredLastActivityAt() ?? Date.now()
    const remainingMs = IDLE_TIMEOUT_MS - (Date.now() - lastActivityAt)

    if (remainingMs <= 0) {
      void logOut({ reason: 'idle' })
      return
    }

    idleTimer = setTimeout(() => {
      void logOut({ reason: 'idle' })
    }, remainingMs)
  }

  const recordSessionActivity = (force = false) => {
    if (!import.meta.client || !accessToken.value) return

    const now = Date.now()
    if (!force && now - lastActivityWriteAt < ACTIVITY_WRITE_INTERVAL_MS) return

    lastActivityWriteAt = now
    localStorage.setItem(LAST_ACTIVITY_KEY, String(now))
    scheduleIdleLogout()
    syncServerSessionActivity()
  }

  const clearSessionState = () => {
    setAccessToken(null)
    lastActivityWriteAt = 0
    lastServerActivitySyncAt = 0

    if (import.meta.client) {
      localStorage.removeItem(LAST_ACTIVITY_KEY)
    }
  }

  const clearServerSession = async () => {
    try {
      const response = await $fetch.raw(LogOutEndpoint(), {
        method: 'POST',
        credentials: 'include',
      })
      await forwardSetCookie(response.headers, requestEvent)
    }
    catch {
      // Best-effort cookie cleanup. The client state is cleared in every path.
    }
  }

  const logOut = async (options?: { redirect?: boolean, reason?: 'manual' | 'expired' | 'idle' }) => {
    const redirect = options?.redirect ?? true
    const reason = options?.reason ?? 'manual'

    try {
      await clearServerSession()
    }
    finally {
      clearSessionState()

      if (redirect) {
        const query = reason === 'manual' ? '' : `?error=${reason === 'idle' ? 'session_idle' : 'session_expired'}`
        await navigateTo(`/login${query}`)
      }
    }
  }

  const syncServerSessionActivity = () => {
    if (!import.meta.client || !accessToken.value || refreshPromise) return

    if (hasIdleExpired()) {
      void logOut({ reason: 'idle' })
      return
    }

    const now = Date.now()
    if (now - lastServerActivitySyncAt < SERVER_ACTIVITY_SYNC_INTERVAL_MS) return

    lastServerActivitySyncAt = now
    void refreshAccessToken().then((refreshed) => {
      if (!refreshed) {
        void logOut({ reason: 'expired' })
      }
    })
  }

  const refreshAccessToken = async () => {
    if (import.meta.client && hasIdleExpired()) {
      await logOut({ redirect: false, reason: 'idle' })
      return false
    }

    if (import.meta.client && refreshPromise) return refreshPromise

    const refreshTask = (async () => {
      try {
        const response = await $fetch.raw<{ data?: { accessToken?: string }, message: string }>(refreshAccessTokenEndpoint(), {
          method: 'POST',
          credentials: 'include',
          ...(requestHeaders ? { headers: requestHeaders } : {}),
        })
        await forwardSetCookie(response.headers, requestEvent)

        const data = response._data?.data

        if (data?.accessToken) {
          setAccessToken(data.accessToken)
          markServerActivitySynced()
          recordSessionActivity(true)
          return true
        }

        setAccessToken(null)
        return false
      }
      catch (error) {
        const headers = (error as FetchErrorLike).response?.headers
        if (headers) {
          await forwardSetCookie(headers, requestEvent)
        }

        setAccessToken(null)
        return false
      }
      finally {
        refreshPromise = null
      }
    })()

    if (import.meta.client) {
      refreshPromise = refreshTask
    }

    return refreshTask
  }

  const ensureAccessToken = async () => {
    if (import.meta.client && hasIdleExpired()) {
      await logOut({ redirect: false, reason: 'idle' })
      return false
    }

    if (!accessToken.value || isAccessTokenExpiring(accessToken.value)) {
      return refreshAccessToken()
    }

    return true
  }

  const buildAuthHeaders = (headers?: HeadersInit) => {
    const mergedHeaders = new Headers(headers)

    if (accessToken.value) {
      mergedHeaders.set('Authorization', `Bearer ${accessToken.value}`)
    }

    return mergedHeaders
  }

  const authFetch = async <T>(url: string, options: AuthFetchOptions = {}) => {
    const hasToken = await ensureAccessToken()

    if (!hasToken) {
      throw createAuthError()
    }

    const request = () =>
      $fetch<T>(url, {
        ...options,
        credentials: 'include',
        headers: buildAuthHeaders(options.headers),
      })

    try {
      const response = await request()
      recordSessionActivity()
      return response
    }
    catch (error) {
      if (getErrorStatus(error) !== 401) {
        throw error
      }

      setAccessToken(null)
      const refreshed = await refreshAccessToken()

      if (!refreshed) {
        await logOut({ reason: 'expired' })
        throw createAuthError()
      }

      const response = await request()
      recordSessionActivity(true)
      return response
    }
  }

  const startIdleWatcher = () => {
    if (!import.meta.client || idleWatcherStarted) return

    idleWatcherStarted = true

    const handleActivity = () => {
      if (!accessToken.value) return

      if (hasIdleExpired()) {
        void logOut({ reason: 'idle' })
        return
      }

      recordSessionActivity()
    }

    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        handleActivity()
      }
    }

    const activityEvents = ['click', 'keydown', 'mousemove', 'scroll', 'touchstart']
    activityEvents.forEach(eventName => window.addEventListener(eventName, handleActivity, { passive: true }))
    document.addEventListener('visibilitychange', handleVisibilityChange)
    window.addEventListener('storage', (event) => {
      if (event.key === LAST_ACTIVITY_KEY) scheduleIdleLogout()
    })

    scheduleIdleLogout()
  }

  const logIn = async (payload: LogInPayload) => {
    try {
      const { message, data } = await $fetch<LoginResponse>(LogInEndpoint(), {
        method: 'POST',
        body: payload,
        credentials: 'include',
      })

      if (!data?.accessToken) {
        throw new Error('Missing access token')
      }

      toast.add({
        title: 'Đăng nhập thành công!',
        description: message,
      })
      setAccessToken(data.accessToken)
      markServerActivitySynced()
      recordSessionActivity(true)
      return data
    }
    catch (error) {
      toast.add({
        title: 'Đăng nhập thất bại!',
        description: getErrorMessage(error, 'Vui lòng thử lại sau.'),
        color: 'error',
      })
      return false
    }
  }

  const register = async (payload: RegisterPayload) => {
    try {
      const { message, data } = await $fetch<ApiResponse>(RegisterEndpoint(), {
        method: 'POST',
        body: payload,
      })

      toast.add({
        title: 'Đăng ký thành công!',
        description: message,
      })
      return data
    }
    catch (error) {
      toast.add({
        title: 'Đăng ký thất bại!',
        color: 'error',
        description: getErrorMessage(error, 'Vui lòng thử lại sau.'),
      })
      return false
    }
  }

  const missingPassword = async (email: string) => {
    try {
      const { data } = await $fetch<ApiResponse>(MissingPasswordEndpoint(), {
        method: 'POST',
        query: { email },
      })

      toast.add({
        title: 'Đã gửi mail!',
        description: `Hướng dẫn phục hồi tài khoản đã được gửi về ${email}`,
      })
      return data
    }
    catch {
      toast.add({
        title: 'Gửi mail thất bại!',
        description: 'Vui lòng thử lại sau.',
        color: 'error',
      })
      return false
    }
  }

  const resetPassword = async (payload: ResetPasswordPayload) => {
    try {
      const { message, data } = await $fetch<ApiResponse>(ResetPasswordEndpoint(), {
        method: 'POST',
        body: payload,
      })

      toast.add({
        title: 'Thay đổi mật khẩu thành công!',
        description: message,
      })
      return data
    }
    catch {
      toast.add({
        title: 'Thay đổi mật khẩu thất bại!',
        description: 'Vui lòng thử lại sau.',
        color: 'error',
      })
      return false
    }
  }

  const verifyResetToken = async (token: string) => {
    try {
      await $fetch<ApiResponse>(VerifyResetEndpoint(), {
        method: 'GET',
        query: { token: token },
      })
      return true
    }
    catch {
      return false
    }
  }

  return {
    accessToken,
    authFetch,
    ensureAccessToken,
    hasIdleExpired,
    logIn,
    logOut,
    missingPassword,
    refreshAccessToken,
    register,
    resetPassword,
    startIdleWatcher,
    verifyResetToken,
  }
}
