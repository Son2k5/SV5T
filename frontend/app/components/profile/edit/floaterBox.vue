<template>
  <div>
    <CommonPageSection inner-class="flex-col gap-4 w-full">
      <div class="relative">
        <NuxtImg
          :src="profile?.avatar ?? 'profilePlaceholder.png'"
          class="size-40 rounded-full object-cover shadow-xl border-white border-4"
          format="webp"
          quality="70"
        />
        <button
          class="absolute size-8 bottom-2 right-2 bg-info rounded-full p-2 z-3 cursor-pointer peer"
        >
          <UIcon
            name="i-heroicons-photo-solid"
            class="bg-white size-full"
          />
        </button>
        <button
          class="size-40 p-0 m-0 absolute inset-0 bg-black/60 rounded-full z-2 border-white border-4 text-white font-bold text-center opacity-0 peer-hover:opacity-100 hover:opacity-100 transition-opacity cursor-pointer"
        >
          Thay Ảnh
        </button>
      </div>
      <div class="flex flex-col items-center">
        <h3 class="text-2xl font-bold text-center lg:text-start">
          {{ profile?.lastName ?? '...' }} {{ profile?.firstName ?? '' }}
        </h3>
        <p class="text-dimmed">
          MSV: {{ profile?.studentCode ?? '...' }}
        </p>
        <UBadge
          v-if="profile?.currentPosition"
          :label="profile?.currentPosition"
          color="neutral"
          class="w-max mt-2"
        />
      </div>
    </CommonPageSection>
  </div>
</template>

<script setup lang="ts">
import type { ProfileData } from '~/types/profile'

const { data: profile } = useNuxtData<ProfileData>('current-user')
</script>
