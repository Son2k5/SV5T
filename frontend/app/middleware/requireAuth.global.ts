import { useAdminAccess } from '~/composables/admin/useAdminAccess'

export default defineNuxtRouteMiddleware(async (to) => {
  const { ensureAccessToken, hasIdleExpired, logOut } = useAuth()

  if (to.path.startsWith('/dashboard') || to.path === '/notifications') {
    if (import.meta.client && hasIdleExpired()) {
      await logOut({ redirect: false, reason: 'idle' })
      return navigateTo('/login?error=session_idle')
    }

    const hasToken = await ensureAccessToken()
    if (!hasToken) return navigateTo('/login?error=session_expired')

    // Keep administrator and student workspaces separate after authentication.
    const { isAdmin } = useAdminAccess()
    if (isAdmin.value) return navigateTo('/admin/dashboard')
  }
})
