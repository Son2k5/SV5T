<template>
  <div
    v-if="settings.showNotificationBell"
    ref="root"
    class="relative"
  >
    <UButton
      color="neutral"
      variant="ghost"
      icon="i-lucide-bell"
      class="relative inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-xl !p-0 text-white/90 transition-colors duration-200 hover:bg-white/15 hover:text-white"
      :ui="{ leadingIcon: 'size-5 shrink-0' }"
      aria-label="Thong bao"
      @click="toggle"
    >
      <span
        v-if="unreadCount > 0"
        class="absolute -right-1 -top-1 flex min-w-5 items-center justify-center rounded-full bg-red-500 px-1.5 text-[10px] font-bold leading-5 text-white"
      >
        {{ unreadCount > 99 ? '99+' : unreadCount }}
      </span>
    </UButton>

    <Transition
      enter-from-class="translate-y-1 opacity-0"
      enter-active-class="transition duration-150"
      leave-active-class="transition duration-100"
      leave-to-class="translate-y-1 opacity-0"
    >
      <div
        v-if="open"
        class="absolute right-0 top-12 z-60 w-[min(22rem,calc(100vw-1.5rem))] overflow-hidden rounded-2xl border border-[#E5E7EB] bg-white shadow-[0_24px_60px_rgba(15,23,42,0.18)]"
      >
        <div class="flex items-center justify-between border-b border-[#E5E7EB] px-4 py-3">
          <div>
            <p class="text-sm font-bold text-[#1E293B]">
              Thong bao
            </p>
            <p class="text-xs text-[#64748B]">
              {{ unreadCount }} chua doc
            </p>
          </div>
          <UButton
            v-if="unreadCount > 0"
            color="neutral"
            variant="ghost"
            size="xs"
            icon="i-lucide-check-check"
            label="Doc het"
            @click="readAll"
          />
        </div>

        <div class="max-h-96 overflow-y-auto">
          <div
            v-if="loading && !latestNotifications.length"
            class="space-y-3 p-4"
          >
            <div
              v-for="index in 3"
              :key="index"
              class="h-14 animate-pulse rounded-xl bg-slate-100"
            />
          </div>
          <div
            v-else-if="!latestNotifications.length"
            class="flex flex-col items-center gap-2 px-4 py-8 text-center"
          >
            <UIcon
              name="i-lucide-inbox"
              class="size-8 text-slate-300"
            />
            <p class="text-sm font-semibold text-[#334155]">
              Chua co thong bao
            </p>
          </div>
          <button
            v-for="notification in latestNotifications"
            v-else
            :key="notification.publicId"
            type="button"
            class="flex w-full gap-3 border-b border-[#F1F5F9] px-4 py-3 text-left transition hover:bg-blue-50/60"
            @click="openNotification(notification.publicId)"
          >
            <span
              class="mt-1 size-2 shrink-0 rounded-full"
              :class="notification.read ? 'bg-slate-200' : 'bg-[#2563EB]'"
            />
            <span class="min-w-0 flex-1">
              <span class="line-clamp-1 text-sm font-semibold text-[#1E293B]">{{ notification.title }}</span>
              <span class="mt-1 line-clamp-2 text-xs leading-5 text-[#64748B]">{{ notification.content }}</span>
              <span class="mt-1 block text-[11px] font-medium text-slate-400">{{ formatTime(notification.createdAt) }}</span>
            </span>
          </button>
        </div>

        <div class="border-t border-[#E5E7EB] p-2">
          <UButton
            to="/notifications"
            color="info"
            variant="ghost"
            block
            icon="i-lucide-list"
            label="Xem tat ca"
            @click="open = false"
          />
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
const { settings } = useUserSettings()
const root = ref<HTMLElement | null>(null)
const open = ref(false)
const {
  fetchNotifications,
  fetchUnreadCount,
  latestNotifications,
  loading,
  markAllAsRead,
  markAsRead,
  unreadCount,
} = useNotifications()

const toggle = async () => {
  open.value = !open.value
  if (open.value) {
    await loadLatest()
  }
}

const loadLatest = async () => {
  await Promise.allSettled([
    fetchUnreadCount(),
    fetchNotifications({ page: 0, size: 5 }),
  ])
}

const readAll = async () => {
  await markAllAsRead()
}

const openNotification = async (publicId: string) => {
  await markAsRead(publicId)
  open.value = false
  await navigateTo('/notifications')
}

const closeOnOutsideClick = (event: MouseEvent) => {
  if (!root.value || root.value.contains(event.target as Node)) return
  open.value = false
}

const formatTime = (value: string) => {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return new Intl.DateTimeFormat('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

onMounted(() => {
  document.addEventListener('click', closeOnOutsideClick)
  if (settings.value.showNotificationBell) {
    void fetchUnreadCount().catch(() => {})
  }
})

onBeforeUnmount(() => {
  document.removeEventListener('click', closeOnOutsideClick)
})

watch(() => settings.value.showNotificationBell, (showNotificationBell) => {
  if (!showNotificationBell) {
    open.value = false
    return
  }

  void fetchUnreadCount().catch(() => {})
})
</script>
