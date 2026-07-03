<template>
  <div class="flex min-h-screen bg-[#F8FAFC] text-[#1E293B]">
    <div v-if="!isMobile">
      <div :class="['h-screen transition-all duration-300', sidebarOpen ? 'w-64' : 'w-24']" />
      <aside :class="['fixed left-0 top-0 z-50 h-screen transition-all duration-300', sidebarOpen ? 'w-64' : 'w-24']">
        <div class="flex size-full flex-col overflow-hidden border-r border-blue-200/40 bg-gradient-to-b from-[#60A5FA] to-[#2563EB] shadow-[0_24px_60px_rgba(37,99,235,0.22)]">
          <div
            class="flex h-20 items-center"
            :class="sidebarOpen ? 'gap-3 px-4' : 'justify-center'"
          >
            <NuxtImg
              src="/logo5Merits.jpg"
              class="size-12 shrink-0 rounded-2xl bg-white object-cover shadow-sm"
              format="webp"
              quality="70"
            />
            <div
              v-if="sidebarOpen"
              class="min-w-0"
            >
              <p class="text-sm font-semibold text-blue-100">
                HANU
              </p>
              <h1 class="truncate text-lg font-bold text-white">
                Quản trị 5 Tốt
              </h1>
            </div>
          </div>

          <nav
            class="flex flex-col gap-2 px-3 py-6"
            aria-label="Điều hướng quản trị"
          >
            <UButton
              v-for="item in menu"
              :key="item.to"
              :to="item.to"
              :icon="item.icon"
              color="neutral"
              variant="ghost"
              class="h-12 justify-center gap-3 rounded-2xl text-white transition hover:bg-white/15 hover:text-white"
              active-class="!bg-white !text-[#2563EB] !shadow-[0_16px_34px_rgba(15,23,42,0.16)]"
              :aria-label="item.label"
            >
              <span
                v-if="sidebarOpen"
                class="truncate text-sm font-semibold"
              >{{ item.label }}</span>
            </UButton>
          </nav>

          <UButton
            color="neutral"
            variant="ghost"
            :icon="sidebarOpen ? 'i-lucide-panel-left-close' : 'i-lucide-panel-left-open'"
            class="mx-3 mt-auto mb-3 h-11 justify-center rounded-2xl border border-white/20 bg-white/10 text-white hover:bg-white/20 hover:text-white"
            :label="sidebarOpen ? 'Thu gọn' : undefined"
            @click="toggleSidebarOpen"
          />
        </div>
      </aside>
    </div>

    <div class="w-full min-w-0">
      <header class="sticky top-0 z-40 border-b border-blue-200/40 bg-gradient-to-b from-[#60A5FA] to-[#2563EB] px-3 py-3 shadow-[0_8px_28px_rgba(15,23,42,0.06)] sm:px-5 lg:px-8">
        <div class="mx-auto flex h-12 max-w-[1440px] items-center gap-3">
          <UButton
            color="neutral"
            variant="ghost"
            :icon="sidebarOpen ? 'i-lucide-panel-right-open' : 'i-lucide-panel-left-open'"
            class="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-xl !p-0 text-white/90 transition-colors duration-200 hover:bg-white/15 hover:text-white"
            :ui="{ leadingIcon: 'size-5 shrink-0' }"
            aria-label="Ẩn hoặc hiện thanh điều hướng"
            @click="toggleSidebarOpen"
          />
          <UInput
            v-model="search"
            class="h-10 w-full max-w-80"
            placeholder="Tìm người dùng..."
            icon="i-lucide-search"
            type="search"
            :ui="{ base: 'h-10 rounded-2xl bg-[#F8FAFC] border-[#E5E7EB] text-[#1E293B]' }"
            @keyup.enter="goToSearch"
          />
          <div class="ml-auto flex items-center gap-1">
            <CommonNotificationBell />
          </div>
          <UDropdownMenu :items="profileActions">
            <UButton
              color="neutral"
              variant="ghost"
              class="inline-flex h-10 max-w-[13rem] items-center rounded-xl px-2 py-0 transition-colors duration-200 hover:bg-white/15"
            >
              <UUser
                :name="profile?.fullName || profile?.email || 'Quản trị viên'"
                :avatar="{ src: profile?.avatar || '/profilePlaceholder.png' }"
                :ui="{ root: 'min-w-0', name: 'hidden truncate md:block text-white font-semibold', description: 'hidden truncate md:block text-blue-100' }"
              />
            </UButton>
          </UDropdownMenu>
        </div>
      </header>

      <main class="mx-auto min-h-screen w-full max-w-[1440px] px-3 py-5 sm:px-5 lg:px-8">
        <div
          class="mb-5 text-sm text-[#64748B]"
          aria-label="Breadcrumb"
        >
          <span>Quản trị</span><span class="mx-2">/</span><span class="font-semibold text-[#334155]">{{ pageTitle }}</span>
        </div>
        <slot />
      </main>
      <CommonFooter />
    </div>

    <div v-if="isMobile">
      <Transition
        enter-from-class="opacity-0"
        enter-active-class="transition-opacity duration-300"
        leave-active-class="transition-opacity duration-300"
        leave-to-class="opacity-0"
      >
        <button
          v-if="sidebarOpen"
          class="fixed inset-0 z-49 bg-slate-950/50 backdrop-blur-sm"
          aria-label="Đóng điều hướng"
          @click="toggleSidebarOpen"
        />
      </Transition>
      <Transition
        enter-from-class="-translate-x-full"
        enter-active-class="transition-transform duration-300"
        leave-active-class="transition-transform duration-300"
        leave-to-class="-translate-x-full"
      >
        <aside
          v-if="sidebarOpen"
          class="fixed inset-y-0 left-0 z-50 w-72 bg-gradient-to-b from-[#60A5FA] to-[#2563EB] p-4 shadow-2xl"
        >
          <div class="mb-7 flex items-center gap-3 py-3">
            <NuxtImg
              src="/logo5Merits.jpg"
              class="size-12 rounded-2xl bg-white object-cover"
              format="webp"
              quality="70"
            />
            <div>
              <p class="text-xs font-semibold text-blue-100">
                HANU
              </p><p class="font-bold text-white">
                Quản trị 5 Tốt
              </p>
            </div>
          </div>
          <nav
            class="flex flex-col gap-2"
            aria-label="Điều hướng quản trị trên điện thoại"
          >
            <UButton
              v-for="item in menu"
              :key="item.to"
              :to="item.to"
              :icon="item.icon"
              :label="item.label"
              color="neutral"
              variant="ghost"
              class="h-12 justify-start rounded-2xl text-white hover:bg-white/15 hover:text-white"
              active-class="!bg-white !text-[#2563EB]"
              @click="toggleSidebarOpen"
            />
          </nav>
        </aside>
      </Transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { DropdownMenuItem } from '@nuxt/ui'
import { useCurrentUser } from '~/composables/profile/useCurrentUser'

const route = useRoute()
const { profile } = await useCurrentUser()
const { logOut } = useAuth()
const { sidebarOpen, toggleSidebarOpen } = useSidebar()
const { isMobile } = useMobile()

const search = ref('')

const menu = [
  { label: 'Tổng quan', icon: 'i-lucide-layout-dashboard', to: '/admin/dashboard' },
  { label: 'Người dùng', icon: 'i-lucide-users', to: '/admin/users' },
  { label: 'Đợt xét duyệt', icon: 'i-lucide-calendar-range', to: '/admin/campaigns' },
  { label: 'Quản lý tiêu chí', icon: 'i-lucide-list-checks', to: '/admin/criteria' },
  { label: 'Hồ sơ xét duyệt', icon: 'i-lucide-file-text', to: '/admin/evidence' },
  { label: 'Chat', icon: 'i-lucide-messages-square', to: '/admin/chat' },
  { label: 'Cấu hình', icon: 'i-lucide-settings-2', to: '/admin/settings' },
]

const pageTitle = computed(() => menu.find(item => route.path.startsWith(item.to))?.label || 'Quản trị')
const profileActions = ref<DropdownMenuItem[]>([
  { label: 'Cấu hình', icon: 'i-lucide-settings-2', onSelect: () => navigateTo('/admin/settings') },
  { label: 'Đăng xuất', icon: 'i-lucide-log-out', onSelect: () => logOut() },
])

const goToSearch = () => {
  const keyword = search.value.trim()
  navigateTo({ path: '/admin/users', query: keyword ? { keyword } : {} })
}

watch(isMobile, (mobile) => {
  if (mobile) sidebarOpen.value = false
}, { immediate: true })
</script>
