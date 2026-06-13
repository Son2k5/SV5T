<template>
  <div class="w-full rounded-lg block h-full">
    <NuxtImg
      :src="PROFILEBANNER"
      class="w-full h-40 object-cover rounded-t-lg"
      format="webp"
      quality="70"
      preload
    />
    <CommonPageSection
      container-class="rounded-t-none lg:h-30"
      inner-class="gap-4 relative flex-col lg:flex-row h-full"
    >
      <NuxtImg
        :src="profile?.avatar ?? 'profilePlaceholder.png'"
        class="absolute -top-1/2 size-40 rounded-full object-cover shadow-xl border-white border-4"
        format="webp"
        quality="70"
        preload
      />
      <div class="h-8 lg:w-40" />
      <div class="flex flex-col">
        <h3 class="text-2xl font-bold text-center lg:text-start">
          {{ profile?.lastName }} {{ profile?.firstName }}
        </h3>
        <p class="text-dimmed">
          MSV: {{ profile?.studentCode }}
        </p>
        <UBadge
          :label="profile?.currentPosition ?? '...'"
          color="neutral"
          class="w-max mt-2"
        />
      </div>
      <div class="lg:ml-auto flex gap-2">
        <UButton
          v-for="(action, id) in PROFILEACTIONS"
          :key="id"
          :label="action.label"
          :color="action.color"
          :variant="action.variant"
          :to="action.to"
          class="h-10 cursor-pointer px-6"
        />
      </div>
    </CommonPageSection>
  </div>
</template>

<script setup lang="ts">
import type { ProfileData } from '~/types/profile'

const PROFILEBANNER = '/ProfileBannerTemp.jpg'

const { data: profile } = useNuxtData<ProfileData>('current-user')

const PROFILEACTIONS: action_types[] = [
  {
    label: 'Chỉnh sửa Hồ sơ',
    color: 'info',
    variant: 'outline',
    to: '/dashboard/me/edit',
  },
  {
    label: 'Chia sẻ',
    color: 'info',
    variant: 'solid',
    onclick: () => { },
  },
]

type action_types = {
  label: string
  color: 'info' | 'error' | 'primary' | 'secondary' | 'success' | 'warning' | 'neutral'
  variant: 'solid' | 'outline' | 'link' | 'soft' | 'subtle' | 'ghost'
  onclick?: () => void
  to?: string
}
</script>
