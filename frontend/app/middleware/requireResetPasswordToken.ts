export default defineNuxtRouteMiddleware(async (to) => {
  if (!to.query.token) {
    return navigateTo('/')
  }

  const { verifyResetToken } = useAuth()

  const resetPasswordTokenTimedout = useState<boolean>('resetPasswordTokenTimedout', () => false)
  const isValid = await verifyResetToken(to.query.token as string)
  resetPasswordTokenTimedout.value = !isValid
})
