export default defineNuxtRouteMiddleware(async (to) => {
  const { accessToken, refreshAccessToken } = useAuth()

  if (to.path.startsWith('/dashboard')) {
    if (!accessToken.value) {
      const newToken = await refreshAccessToken()
      if (!newToken) return navigateTo('/login?error=unauthorized')
      return
    }
  }
})
