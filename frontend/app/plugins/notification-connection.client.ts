export default defineNuxtPlugin(() => {
  const { accessToken } = useAuth()
  const { connect, disconnect } = useNotificationConnection()

  watch(accessToken, (token) => {
    if (token) {
      void connect()
    }
    else {
      disconnect()
    }
  }, { immediate: true })
})
