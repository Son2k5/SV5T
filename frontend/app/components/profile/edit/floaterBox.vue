<template>
  <div>
    <CommonPageSection inner-class="flex-col gap-5 w-full text-center">
      <div class="relative">
        <ProfileAvatarUploader
          size-class="size-36"
          image-class="rounded-full border-4 border-white object-cover shadow-[0_18px_45px_rgba(15,23,42,0.16)]"
          button-class="absolute bottom-2 right-2 z-3 flex size-9 cursor-pointer items-center justify-center rounded-full border-2 border-white bg-[#3B82F6] text-white shadow-lg transition hover:bg-[#2563EB] disabled:cursor-wait disabled:opacity-80"
          show-hover-label
        />
      </div>
      <div class="flex flex-col items-center">
        <h3 class="text-2xl font-bold text-[#1E293B]">
          {{ profile?.fullName ?? '...' }}
        </h3>
        <p class="mt-1 text-sm text-[#64748B]">
          MSV: {{ profile?.studentCode ?? '...' }}
        </p>
        <UBadge
          v-if="profile?.currentPosition"
          :label="profile?.currentPosition"
          color="neutral"
          variant="subtle"
          class="mt-3 w-max rounded-full px-3 py-1"
        />
      </div>
    </CommonPageSection>
  </div>
</template>

<script setup lang="ts">
import type { ProfileData } from '~/types/profile'

const { data: profile } = useNuxtData<ProfileData>('current-user')
</script>
