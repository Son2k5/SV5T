export default defineNuxtRouteMiddleware(async (to) => {
  const { ensureAccessToken, hasIdleExpired, logOut } = useAuth()

  if (to.path.startsWith('/dashboard')) {
    if (import.meta.client && hasIdleExpired()) {
      await logOut({ redirect: false, reason: 'idle' })
      return navigateTo('/login?error=session_idle')
    }

    const hasToken = await ensureAccessToken()
    if (!hasToken) return navigateTo('/login?error=session_expired')
  }
})
