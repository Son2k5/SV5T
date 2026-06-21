export default defineNuxtPlugin(() => {
  const { startIdleWatcher } = useAuth()
  startIdleWatcher()
})
