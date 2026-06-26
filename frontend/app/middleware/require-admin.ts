import { useAdminAccess } from '~/composables/admin/useAdminAccess'

export default defineNuxtRouteMiddleware(async () => {
  const { ensureAccessToken } = useAuth()
  const hasToken = await ensureAccessToken()

  if (!hasToken) {
    return navigateTo('/login?error=session_expired')
  }

  const { isAdmin } = useAdminAccess()
  if (!isAdmin.value) {
    return navigateTo('/dashboard')
  }
})
