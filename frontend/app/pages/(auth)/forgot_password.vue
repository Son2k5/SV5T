<template>
  <UForm
    class="flex w-full flex-col gap-5"
    :state="missingPasswordPayloadState"
    :schema="schema"
    @submit="handleMissingPassword"
  >
    <p class="text-center text-3xl font-bold text-[#1E293B]">
      Quên Mật Khẩu
    </p>
    <p class="text-center text-sm text-[#64748B]">
      Nhận hướng dẫn khôi phục trong email
    </p>
    <UFormField
      label="Email"
      name="email"
      :ui="{ label: 'text-sm font-semibold text-[#334155]' }"
    >
      <UInput
        v-model="missingPasswordPayloadState.email"
        class="w-full"
        color="neutral"
        placeholder="Nhập email..."
        :ui="{ base: 'h-11' }"
        autocomplete="email"
      />
    </UFormField>
    <UButton
      class="h-11 cursor-pointer shadow-sm"
      color="info"
      label="Gửi Liên Kết"
      :ui="{ label: ['mx-auto text-lg', isLoading && 'hidden'], leadingIcon: 'mx-auto' }"
      :loading="isLoading"
      type="submit"
    />
    <NuxtLink
      to="/login"
      class="text-center text-sm font-semibold text-[#2563EB] hover:text-[#1D4ED8]"
    >Quay Lại Đăng Nhập</NuxtLink>
  </UForm>
</template>

<script setup lang="ts">
import { z } from 'zod'

definePageMeta({
  layout: 'auth',
})

const isLoading = ref<boolean>(false)

const schema = z.object({ email: z.email('Email không hợp lệ!') })

const { missingPassword } = useAuth()

const handleMissingPassword = async () => {
  isLoading.value = true
  try {
    await missingPassword(missingPasswordPayloadState.value.email)
  }
  finally {
    isLoading.value = false
  }
}

const missingPasswordPayloadState = ref({ email: '' })
</script>
