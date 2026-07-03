<template>
  <div class="flex min-h-screen bg-[#F8FAFC] text-[#1E293B]">
    <div>
      <CommonSidebar v-if="!isMobile" />
      <CommonSidebarMobile v-if="isMobile" />
    </div>
    <div class="w-full min-w-0">
      <CommonTopbar />
      <CommonMaintenanceBanner />
      <main class="mx-auto min-h-screen w-full max-w-[1440px] px-3 py-5 sm:px-5 lg:px-8">
        <slot />
      </main>
      <CommonFooter />
    </div>
    <ChatUserChatWidget v-if="settings.showChatWidget" />
  </div>
</template>

<script setup lang="ts">
const { sidebarOpen, toggleSidebarOpen } = useSidebar()
const { isMobile } = useMobile()
const { settings } = useUserSettings()

const handleKeyboardToggleSidebar = (e: KeyboardEvent) => {
  const target = e.target as HTMLElement
  if (target && ['INPUT', 'TEXTAREA', 'SELECT'].includes(target.tagName)) {
    return
  }

  if (e.key.toLocaleLowerCase() === 'k') {
    e.preventDefault()
    toggleSidebarOpen()
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeyboardToggleSidebar)
  if (isMobile.value) {
    sidebarOpen.value = false
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeyboardToggleSidebar)
})
</script>
