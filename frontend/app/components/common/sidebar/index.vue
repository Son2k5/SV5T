<template>
  <div>
    <div :class="['h-screen transition-all', sidebarOpen ? 'w-50' : 'w-20']" />
    <div :class="['fixed h-screen top-0 left-0 p-2 transition-all', sidebarOpen ? 'w-50' : 'w-20']">
      <div class="size-full bg-info-300 flex flex-col gap-6 rounded-lg items-center shadow-xl overflow-hidden">
        <div class="w-full px-4">
          <NuxtImg
            src="/logo5Merits.jpg"
            format="webp"
            quality="70"
            class="rounded-lg w-full max-w-30 mt-4 mx-auto"
            preload
          />
        </div>
        <div class="flex flex-col gap-2 w-full px-4">
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
      <div
        class="group absolute h-full w-2 py-2 right-0 top-0 cursor-pointer"
        @click="toggleSidebarOpen"
      >
        <div class="group-hover:bg-gray-300 h-full w-1/2 ml-auto rounded-lg" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { sidebarOptions } from '~/constants/sidebar'

const { sidebarOpen, toggleSidebarOpen } = useSidebar()
</script>
