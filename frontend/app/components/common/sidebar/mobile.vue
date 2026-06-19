<template>
  <div>
    <Transition
      enter-from-class="opacity-0"
      enter-active-class="transition-opacity duration-300 ease-out"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-300 ease-in"
      leave-to-class="opacity-0"
    >
      <div
        v-if="sidebarOpen"
        class="fixed inset-0 z-98 bg-slate-950/50 backdrop-blur-sm"
        @click="toggleSidebarOpen"
      />
    </Transition>
    <Transition
      enter-from-class="-translate-x-full"
      enter-active-class="transition-transform duration-300 ease-out"
      enter-to-class="-translate-x-0"
      leave-active-class="transition-transform duration-300 ease-in"
      leave-to-class="-translate-x-full"
    >
      <div
        v-show="sidebarOpen"
        class="fixed left-0 top-0 z-98 h-full w-72 bg-gradient-to-b from-[#60A5FA] to-[#3B82F6] p-4 shadow-[0_24px_60px_rgba(15,23,42,0.28)]"
      >
        <div class="flex h-full flex-col">
          <div class="flex items-center gap-3 py-4">
            <div class="flex size-12 shrink-0 items-center justify-center overflow-hidden rounded-2xl bg-white shadow-sm">
              <NuxtImg
                src="/logo5Merits.jpg"
                format="webp"
                quality="70"
                class="size-full object-cover"
                preload
              />
            </div>
            <div>
              <p class="text-xs font-semibold text-blue-100">
                HANU
              </p>
              <h1 class="text-lg font-bold text-white">
                Sinh viên 5 Tốt
              </h1>
            </div>
          </div>

          <nav class="mt-6 flex flex-col items-center gap-3">
            <UButton
              v-for="section in sidebarOptions"
              :key="section.path"
              :leading-icon="section.icon"
              variant="ghost"
              color="neutral"
              class="h-12 w-full justify-center gap-3 rounded-2xl px-4 text-white shadow-sm transition duration-200 hover:-translate-y-0.5 hover:bg-white/15 hover:text-white hover:shadow-[0_14px_28px_rgba(15,23,42,0.14)]"
              :to="section.path"
              active-class="!bg-white !text-[#2563EB] !shadow-[0_16px_34px_rgba(15,23,42,0.16)]"
              @click="toggleSidebarOpen"
            >
              <span class="truncate text-sm font-semibold">
                {{ section.name }}
              </span>
            </UButton>
          </nav>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { sidebarOptions } from '~/constants/sidebar'

const { sidebarOpen, toggleSidebarOpen } = useSidebar()
</script>
