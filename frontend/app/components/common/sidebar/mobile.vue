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
        class="fixed size-full bg-black/60 z-98"
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
        class="fixed w-50 h-full bg-info-300 z-98"
      >
        <div class="h-12" />
        <div class="flex flex-col gap-6 items-center">
          <div class="w-full px-4">
            <NuxtImg
              src="/logo5Merits.jpg"
              format="webp"
              quality="70"
              class="rounded-lg w-30 mt-4 mx-auto"
              preload
            />
          </div>
          <div class="flex flex-col gap-2">
            <div
              v-for="(section, id) in sidebarOptions"
              :key="id"
            >
              <UButton
                :leading-icon="section.icon"
                variant="ghost"
                color="info"
                class="w-full cursor-pointer text-white hover:bg-info-400"
                :to="section.path"
                active-class="bg-info"
              >
                <p class="text-nowrap overflow-hidden transition-all font-bold">
                  {{ sidebarOpen ? section.name : '' }}
                </p>
              </UButton>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { sidebarOptions } from '~/constants/sidebar'

const { sidebarOpen, toggleSidebarOpen } = useSidebar()
</script>
