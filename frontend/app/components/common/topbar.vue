<template>
  <header class="sticky top-0 z-40 border-b border-blue-200/40 bg-gradient-to-b from-[#60A5FA] to-[#3B82F6] px-3 py-3 shadow-[0_8px_28px_rgba(15,23,42,0.06)] backdrop-blur-xl sm:px-5 lg:px-8">
    <div class="mx-auto flex h-12 w-full max-w-[1440px] items-center gap-3">
      <UButton
        color="neutral"
        variant="ghost"
        class="inline-flex h-10 w-10 shrink-0 cursor-pointer items-center justify-center rounded-xl !p-0 text-white/90 transition-colors duration-200 hover:bg-white/15 hover:text-white"
        :icon="sidebarOpen ? 'i-lucide-panel-right-open' : 'i-lucide-panel-left-open'"
        :ui="{ leadingIcon: 'size-5 shrink-0' }"
        aria-label="Toggle sidebar"
        @click="toggleSidebarOpen"
      />
      <UInput
        color="neutral"
        class="h-10 w-full max-w-80"
        placeholder="Tìm kiếm..."
        icon="i-lucide-search"
        type="search"
        :ui="{ base: 'h-10 rounded-2xl bg-[#F8FAFC] border-[#E5E7EB] text-[#1E293B]' }"
      />

      <div class="ml-auto flex items-center gap-1">
        <CommonNotificationBell />
        <UButton
          v-for="action in headerActions"
          :key="action.icon"
          color="neutral"
          variant="ghost"
          class="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-xl !p-0 text-white/90 transition-colors duration-200 hover:bg-white/15 hover:text-white"
          :icon="action.icon"
          :ui="{ leadingIcon: 'size-5 shrink-0' }"
          :aria-label="action.label"
          :to="action.to"
        />
      </div>

      <UDropdownMenu :items="profileActions">
        <UButton
          class="inline-flex h-10 max-w-[13rem] cursor-pointer items-center rounded-xl px-2 py-0 transition-colors duration-200 hover:bg-white/15"
          color="neutral"
          variant="ghost"
        >
          <UUser
            :avatar="{ src: profile?.avatar ?? 'profilePlaceholder.png' }"
            :name="profile?.fullName ?? 'Profile Name'"
            :ui="{ root: 'min-w-0', name: 'hidden truncate md:block text-white font-semibold', description: 'hidden truncate md:block text-blue-100' }"
          />
        </UButton>
        <template #user-profile>
          <div>
            <p class="font-bold">
              {{ profile?.fullName ?? '...' }}
            </p>
            <p class="text-dimmed text-sm text-start">
              {{ profile?.currentPosition }}
            </p>
          </div>
        </template>
      </UDropdownMenu>
    </div>
  </header>
</template>

<script setup lang="ts">
import type { DropdownMenuItem } from '@nuxt/ui'
import { useCurrentUser } from '~/composables/profile/useCurrentUser'

const { logOut } = useAuth()

const { profile } = await useCurrentUser()

const handleLogOut = async () => {
  await logOut()
}

const headerActions = [
  {
    icon: 'i-lucide-message-square',
    label: 'Tin nhắn',
  },
  {
    icon: 'i-lucide-settings',
    label: 'Cài đặt',
    to: '/dashboard/settings',
  },
]

const profileActions = ref<DropdownMenuItem[]>([
  {
    slot: 'user-profile' as const,
    type: 'label',
  },
  {
    label: 'Thông Tin Cá Nhân',
    icon: 'i-heroicons-user-solid',
    onSelect: () => navigateTo('/dashboard/me/edit'),
  },
  {
    label: 'Cài đặt',
    icon: 'i-lucide-settings',
    onSelect: () => navigateTo('/dashboard/settings'),
  },
  {
    label: 'Đăng Xuất',
    icon: 'i-heroicons-arrow-left-start-on-rectangle',
    onSelect: handleLogOut,
  },
])

const { sidebarOpen, toggleSidebarOpen } = useSidebar()
</script>
