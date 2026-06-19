<template>
  <div>
    <div
      v-if="profileCompleted"
      class="flex flex-col gap-6"
    >
      <ProfileBanner />
      <ProfileOverview :sections="OVERVIEWSECTIONS" />
      <ProfileGeneral />
    </div>
    <ProfileMissingInfo v-else />
  </div>
</template>

<script setup lang="ts">
import { useCurrentUser } from '~/composables/profile/useCurrentUser'
import { isProfileComplete } from '~/types/profile'

const { profile } = await useCurrentUser()

const profileCompleted = computed(() => isProfileComplete(profile.value))

const OVERVIEWSECTIONS = [
  {
    label: 'GPA',
    desc: '3.8',
    icon: 'i-heroicons-academic-cap-solid',
  },
  {
    label: 'TRẠNG THÁI SINH VIÊN 5 TỐT',
    desc: 'TỐT',
    icon: 'i-heroicons-check-circle-solid',
  },
  {
    label: 'SỐ TIÊU CHÍ HOÀN THÀNH',
    desc: '36',
    icon: 'i-heroicons-clock-solid',
  },
  {
    label: 'HỒ SƠ BỊ LOẠI/THIẾU',
    desc: '12',
    icon: 'i-heroicons-exclamation-circle-solid',
  },
]
</script>
