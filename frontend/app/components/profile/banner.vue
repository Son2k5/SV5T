<template>
  <section class="sv-card overflow-hidden">
    <div class="relative h-48 overflow-hidden">
      <NuxtImg
        :src="PROFILEBANNER"
        class="size-full object-cover"
        format="webp"
        quality="80"
        preload
      />
      <div class="absolute inset-0 bg-gradient-to-t from-slate-950/35 via-slate-950/5 to-transparent" />
    </div>

    <div class="relative flex flex-col gap-5 px-5 pb-5 pt-0 lg:flex-row lg:items-end lg:px-7 lg:pb-7">
      <div class="-mt-16 flex justify-center lg:justify-start">
        <ProfileAvatarUploader
          size-class="size-32 lg:size-36"
          image-class="rounded-full border-4 border-white object-cover shadow-[0_18px_45px_rgba(15,23,42,0.20)]"
          button-class="absolute bottom-2 right-2 z-3 flex size-10 items-center justify-center rounded-full border-2 border-white bg-[#3B82F6] text-white shadow-lg transition hover:bg-[#2563EB] disabled:cursor-wait disabled:opacity-80"
          show-hover-label
        />
      </div>

      <div class="min-w-0 flex-1 text-center lg:text-left">
        <div class="flex flex-col gap-2">
          <h3 class="truncate text-3xl font-bold text-[#1E293B]">
            {{ profile?.fullName ?? '...' }}
          </h3>
          <div class="flex flex-wrap items-center justify-center gap-3 text-sm text-[#64748B] lg:justify-start">
            <span class="inline-flex items-center gap-1.5">
              <UIcon
                name="i-lucide-id-card"
                class="size-4 text-[#3B82F6]"
              />
              MSV: {{ profile?.studentCode ?? '...' }}
            </span>
            <span class="inline-flex items-center gap-1.5">
              <UIcon
                name="i-lucide-building-2"
                class="size-4 text-[#3B82F6]"
              />
              {{ profile?.faculty ?? 'Chưa cập nhật khoa' }}
            </span>
            <span class="inline-flex items-center gap-1.5">
              <UIcon
                name="i-lucide-map-pin"
                class="size-4 text-[#3B82F6]"
              />
              {{ profile?.permanentAddress?.provinceCity ?? 'Chưa cập nhật địa chỉ' }}
            </span>
          </div>
          <UBadge
            :label="profile?.currentPosition ?? 'Chưa cập nhật chức vụ'"
            color="neutral"
            variant="subtle"
            class="mx-auto mt-1 w-max rounded-full px-3 py-1 text-[#1E293B] lg:mx-0"
          />
        </div>
      </div>

      <div class="flex flex-col gap-2 sm:flex-row lg:ml-auto">
        <UButton
          v-for="(action, id) in PROFILEACTIONS"
          :key="id"
          :label="action.label"
          :color="action.color"
          :variant="action.variant"
          :icon="action.icon"
          :to="action.to"
          class="h-11 cursor-pointer justify-center px-5 shadow-sm"
        />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import type { ProfileData } from '~/types/profile'

const PROFILEBANNER = '/ProfileBannerTemp.jpg'

const { data: profile } = useNuxtData<ProfileData>('current-user')

const PROFILEACTIONS: action_types[] = [
  {
    label: 'Chỉnh sửa Hồ sơ',
    color: 'info',
    variant: 'solid',
    icon: 'i-lucide-pen-line',
    to: '/dashboard/me/edit',
  },
  {
    label: 'Chia sẻ',
    color: 'neutral',
    variant: 'outline',
    icon: 'i-lucide-share-2',
    onclick: () => { },
  },
]

type action_types = {
  label: string
  color: 'info' | 'error' | 'primary' | 'secondary' | 'success' | 'warning' | 'neutral'
  variant: 'solid' | 'outline' | 'link' | 'soft' | 'subtle' | 'ghost'
  icon?: string
  onclick?: () => void
  to?: string
}
</script>
