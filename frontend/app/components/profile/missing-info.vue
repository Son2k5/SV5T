<template>
  <CommonPageSection inner-class="flex flex-col justify-center items-center gap-6 py-10">
    <h2 class="text-3xl font-bold">
      Tài khoản thiếu thông tin
    </h2>
    <p class="text-xl">
      Vui lòng đến hoàn thành!
    </p>
    <UButton
      class="py-4 cursor-pointer"
      color="info"
      label="Chỉnh sửa Thông tin"
      @click="handleRouting"
    />
  </CommonPageSection>
</template>

<script setup lang="ts">
import type { ProfileData } from '~/types/profile'

const { data: profile } = useNuxtData<ProfileData>('current-user')

const handleRouting = () => {
  navigateTo(`/dashboard/${profile.value?.id}/edit`)
}
</script>
