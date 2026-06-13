<template>
  <div class="flex bg-gray-200">
    <div>
      <CommonSidebar v-if="!isMobile" />
      <CommonSidebarMobile v-if="isMobile" />
    </div>
    <div class="w-full">
      <CommonTopbar />
      <div class="mt-4 mb-10 mx-2 md:ml-0 min-h-screen">
        <slot />
      </div>
      <CommonFooter />
    </div>
  </div>
</template>

<script setup lang="ts">
const { sidebarOpen, toggleSidebarOpen } = useSidebar()
const { isMobile } = useMobile()

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
