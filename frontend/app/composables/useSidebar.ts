export const useSidebar = () => {
  const sidebarOpen = useState<boolean>(() => true)

  const toggleSidebarOpen = useThrottleFn(() => {
    sidebarOpen.value = !sidebarOpen.value
  }, 700)

  return { sidebarOpen, toggleSidebarOpen }
}
