<template>
  <div>
    <div :class="['h-screen transition-all duration-300', sidebarOpen ? 'w-64' : 'w-24']" />
    <aside :class="['fixed top-0 left-0 z-50 h-screen transition-all duration-300', sidebarOpen ? 'w-64' : 'w-24']">
      <div class="size-full overflow-hidden border-r border-blue-200/40 bg-gradient-to-b from-[#60A5FA] to-[#3B82F6] shadow-[0_24px_60px_rgba(37,99,235,0.22)]">
        <div class="flex h-full flex-col">
          <div
            class="flex h-20 items-center transition-all duration-300"
            :class="sidebarOpen ? 'justify-start gap-3 px-4' : 'justify-center gap-0 px-0'"
          >
            <div class="flex size-12 shrink-0 items-center justify-center overflow-hidden rounded-2xl bg-white shadow-sm">
              <NuxtImg
                src="/logo5Merits.jpg"
                format="webp"
                quality="70"
                class="size-full object-cover"
                preload
              />
            </div>
            <div
              class="min-w-0 overflow-hidden transition-all duration-200"
              :class="sidebarOpen ? 'opacity-100' : 'w-0 opacity-0'"
            >
              <p class="truncate text-sm font-semibold text-blue-100">
                HANU
              </p>
              <h1 class="truncate text-lg font-bold text-white">
                Sinh viên 5 Tốt
              </h1>
            </div>
          </div>

          <nav class="flex flex-col items-center gap-3 px-3 py-6">
            <UButton
              v-for="section in sidebarOptions"
              :key="section.path"
              :leading-icon="section.icon"
              variant="ghost"
              color="neutral"
              class="h-12 w-full justify-center gap-3 rounded-2xl px-3 text-white  transition duration-200 hover:-translate-y-0.5 hover:bg-white/15 hover:text-white hover:shadow-[0_14px_28px_rgba(15,23,42,0.14)]"
              :class="!sidebarOpen && 'px-0'"
              :to="section.path"
              active-class="!bg-white !text-[#2563EB] !shadow-[0_16px_34px_rgba(15,23,42,0.16)]"
            >
              <span
                class="truncate text-sm font-semibold transition-all duration-200"
                :class="sidebarOpen ? 'opacity-100' : 'hidden opacity-0'"
              >
                {{ section.name }}
              </span>
            </UButton>
          </nav>

          <div class="mt-auto">
            <button
              class="mx-3 mb-3 flex h-11 w-[calc(100%-1.5rem)] items-center justify-center rounded-2xl border border-white/20 bg-white/10 text-white shadow-sm transition duration-200 hover:-translate-y-0.5 hover:bg-white/20 hover:shadow-[0_14px_28px_rgba(15,23,42,0.14)]"
              type="button"
              @click="toggleSidebarOpen"
            >
              <UIcon
                :name="sidebarOpen ? 'i-lucide-panel-left-close' : 'i-lucide-panel-left-open'"
                class="size-5"
              />
              <span
                v-if="sidebarOpen"
                class="ml-2 text-sm font-semibold"
              >
                Thu gọn
              </span>
            </button>
          </div>
        </div>
      </div>
    </aside>
  </div>
</template>

<script setup lang="ts">
import { sidebarOptions } from '~/constants/sidebar'

const { sidebarOpen, toggleSidebarOpen } = useSidebar()
</script>
