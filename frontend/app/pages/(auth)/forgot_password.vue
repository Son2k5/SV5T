<template>
  <UForm
    class="flex flex-col gap-6 w-full px-6 xl:px-16"
    :state="missingPasswordPayloadState"
    :schema="schema"
    @submit="handleMissingPassword"
  >
    <p class="text-4xl text-info-500 font-bold text-center">
      Quên Mật Khẩu
    </p>
    <p class="text-center">
      Nhận hướng dẫn khôi phục trong email
    </p>
    <UFormField
      label="Email"
      name="email"
      :ui="{ label: 'text-lg' }"
    >
      <UInput
        v-model="missingPasswordPayloadState.email"
        class="w-full"
        color="neutral"
        placeholder="Nhập email..."
        :ui="{ base: 'bg-gray-100 h-10 text-black' }"
        autocomplete="email"
      />
    </UFormField>
    <UButton
      class="h-10 cursor-pointer"
      color="info"
      label="Gửi Liên Kết"
      :ui="{ label: ['mx-auto text-lg', isLoading && 'hidden'], leadingIcon: 'mx-auto' }"
      :loading="isLoading"
      type="submit"
    />
    <NuxtLink
      to="/login"
      class="text-info-500 hover:text-muted text-sm text-center"
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
