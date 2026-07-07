import type { AdminRole } from '~/types/admin'
import { useAuth } from '~/composables/useAuth'

type TokenPayload = { role?: string }

const readTokenPayload = (token: string | null): TokenPayload | null => {
  if (!token) return null

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

export const useAdminAccess = () => {
  const { accessToken } = useAuth()

  const role = computed<AdminRole | null>(() => {
    const tokenRole = readTokenPayload(accessToken.value)?.role
    return tokenRole === 'ADMIN' || tokenRole === 'MENTOR' || tokenRole === 'USER'
      ? tokenRole
      : null
  })

  const isAdmin = computed(() => role.value === 'ADMIN' || role.value === 'MENTOR')
  return { role, isAdmin }
}
