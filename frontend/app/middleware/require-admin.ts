export default defineNuxtRouteMiddleware(async () => {
  const { ensureAccessToken, accessToken } = useAuth()
  const hasToken = await ensureAccessToken()

  if (!hasToken) {
    return navigateTo('/login?error=session_expired')
  }

  // Parse JWT token directly to ensure absolute consistency and no reactive delay/lag
  const parseTokenRole = (token: string | null): string | null => {
    if (!token) return null
    try {
      const payload = token.split('.')[1]
      if (!payload) return null
      const normalized = payload.replace(/-/g, '+').replace(/_/g, '/')
      const padded = normalized.padEnd(normalized.length + ((4 - normalized.length % 4) % 4), '=')
      return (JSON.parse(atob(padded)) as { role?: string }).role || null
    }
    catch {
      return null
    }
  }

  const role = parseTokenRole(accessToken.value)
  if (role !== 'ADMIN' && role !== 'MENTOR') {
    return navigateTo('/dashboard')
  }
})
