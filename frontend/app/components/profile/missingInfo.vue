<template>
  <CommonPageSection inner-class="flex flex-col justify-center items-center gap-5 py-16 text-center">
    <div class="flex size-16 items-center justify-center rounded-3xl bg-blue-50 text-[#3B82F6]">
      <UIcon
        name="i-lucide-user-round-plus"
        class="size-8"
      />
    </div>
    <h2 class="text-3xl font-bold text-[#1E293B]">
      Tài khoản thiếu thông tin
    </h2>
    <p class="max-w-md text-base text-[#64748B]">
      Hoàn thiện hồ sơ cá nhân để bắt đầu đăng ký minh chứng và theo dõi tiến độ Sinh viên 5 Tốt.
    </p>
    <UButton
      class="h-11 cursor-pointer px-6 shadow-sm"
      color="info"
      icon="i-lucide-pen-line"
      label="Chỉnh sửa Thông tin"
      @click="handleRouting"
    />
  </CommonPageSection>
</template>

<script setup lang="ts">
const handleRouting = () => {
  navigateTo('/dashboard/me/edit')
}
</script>
