<template>
  <header class="sticky top-0 z-40 border-b border-blue-200/40 bg-gradient-to-b from-[#60A5FA] to-[#3B82F6] px-3 py-3 shadow-[0_8px_28px_rgba(15,23,42,0.06)] backdrop-blur-xl sm:px-5 lg:px-8">
    <div class="mx-auto flex h-12 w-full max-w-[1440px] items-center gap-3">
      <UButton
        color="neutral"
        variant="ghost"
        class="inline-flex h-10 w-10 shrink-0 cursor-pointer items-center justify-center rounded-xl !p-0 text-[#64748B] transition-colors duration-200 hover:bg-blue-50 hover:text-[#2563EB]"
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

      <div class="ml-auto hidden items-center gap-1 sm:flex">
        <UButton
          v-for="action in headerActions"
          :key="action.icon"
          color="neutral"
          variant="ghost"
          class="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-xl !p-0 text-[#64748B] transition-colors duration-200 hover:bg-blue-50 hover:text-[#2563EB]"
          :icon="action.icon"
          :ui="{ leadingIcon: 'size-5 shrink-0' }"
          :aria-label="action.label"
        />
      </div>

      <UDropdownMenu :items="profileActions">
        <UButton
          class="cursor-pointer rounded-2xl   px-2 py-3  hover:bg-[#F8FAFC]"
          color="neutral"
          variant="ghost"
        >
          <UUser
            :avatar="{ src: profile?.avatar ?? 'profilePlaceholder.png' }"
            :name="profile?.fullName ?? 'Profile Name'"
            :ui="{ name: 'hidden md:block text-[#1E293B] font-semibold', description: 'hidden md:block text-[#64748B]' }"
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
    icon: 'i-lucide-bell',
    label: 'Thông báo',
  },
  {
    icon: 'i-lucide-message-square',
    label: 'Tin nhắn',
  },
  {
    icon: 'i-lucide-settings',
    label: 'Cài đặt',
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
    label: 'Đăng Xuất',
    icon: 'i-heroicons-arrow-left-start-on-rectangle',
    onSelect: handleLogOut,
  },
])

const { sidebarOpen, toggleSidebarOpen } = useSidebar()
</script>
