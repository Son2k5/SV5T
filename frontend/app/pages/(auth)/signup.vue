<template>
  <UForm
    class="flex flex-col gap-6 w-full px-6 xl:px-16 pt-10"
    :state="registerPayloadState"
    :schema="schema"
    @submit="handleRegister"
  >
    <p class="text-4xl text-info-500 font-bold text-center">
      Đăng Ký
    </p>
    <UFormField
      label="Email"
      name="email"
      :ui="{ label: 'text-lg' }"
    >
      <CommonPopper>
        <UInput
          v-model="registerPayloadState.email"
          class="w-full"
          color="neutral"
          placeholder="Nhập email..."
          :ui="{ base: 'bg-gray-100 h-10 text-black' }"
          autocomplete="email"
        />
        <template #content>
          <p>Sinh viên sử dụng tài khoản Outlook nhà trường đã cung cấp</p>
        </template>
      </CommonPopper>
    </UFormField>
    <UFormField
      class="group"
      label="Mật Khẩu"
      name="userPassword"
      :ui="{ label: 'text-lg' }"
    >
      <CommonPopper>
        <UInput
          v-model="registerPayloadState.userPassword"
          class="w-full"
          color="neutral"
          placeholder="Nhập mật khẩu..."
          :ui="{ base: 'bg-gray-100 h-10 text-black' }"
          :type="passwordShow ? 'text' : 'password'"
        >
          <template #trailing>
            <UButton
              class="cursor-pointer"
              color="neutral"
              variant="link"
              size="sm"
              :icon="passwordShow ? 'i-lucide-eye-off' : 'i-lucide-eye'"
              :aria-label="passwordShow ? 'Hide password' : 'Show password'"
              :aria-pressed="passwordShow"
              aria-controls="password"
              @click="passwordShow = !passwordShow"
            />
          </template>
        </UInput>
        <template #content>
          <ul
            style="list-style-type: disc;"
            class="pl-4"
          >
            <li
              v-for="req in REQS"
              :key="req"
            >
              {{ req }}
            </li>
          </ul>
        </template>
      </CommonPopper>
    </UFormField>
    <UFormField
      label="Xác Nhận Mật Khẩu"
      name="confirmPassword"
      :ui="{ label: 'text-lg' }"
    >
      <UInput
        v-model="registerPayloadState.confirmPassword"
        class="w-full"
        color="neutral"
        placeholder="Xác nhận mật khẩu..."
        :ui="{ base: 'bg-gray-100 h-10 text-black' }"
        :type="confirmPasswordShow ? 'text' : 'password'"
      >
        <template #trailing>
          <UButton
            class="cursor-pointer"
            color="neutral"
            variant="link"
            size="sm"
            :icon="confirmPasswordShow ? 'i-lucide-eye-off' : 'i-lucide-eye'"
            :aria-label="confirmPasswordShow ? 'Hide password' : 'Show password'"
            :aria-pressed="confirmPasswordShow"
            aria-controls="confirmPassword"
            @click="confirmPasswordShow = !confirmPasswordShow"
          />
        </template>
      </UInput>
    </UFormField>
    <UButton
      class="h-10 cursor-pointer"
      color="info"
      label="Đăng Ký"
      :ui="{ label: ['mx-auto text-lg', isLoading && 'hidden'], leadingIcon: 'mx-auto' }"
      :loading="isLoading"
      type="submit"
    />
    <p class="text-sm mx-auto">
      Đã có tài khoản?
      <NuxtLink
        to="/login"
        class="text-info-500 hover:text-muted"
      >Đăng Nhập</NuxtLink>
    </p>
  </UForm>
</template>

<script setup lang="ts">
import { z } from 'zod'
import { REQS } from '~/constants/passwordRequirements'
import type { RegisterPayload } from '~/types/auth'

definePageMeta({
  layout: 'auth',
})

const isLoading = ref<boolean>(false)

const schema
  = z.object({
    email: z.email('Email không hợp lệ!').endsWith('@ms.hanu.edu.vn', { message: 'Sinh viên sử dụng mail Mã_Sinh_Viên@ms.hanu.edu.vn' }),
    userPassword: z.string().min(8, 'Mật khẩu phải có ít nhất 8 kí tự!'),
    confirmPassword: z.string(),
  })
    .refine(data => data.userPassword === data.confirmPassword, {
      message: 'Mật khẩu xác nhận không trùng khớp!',
      path: ['confirmPassword'],
    })

const { register } = useAuth()

const handleRegister = async () => {
  isLoading.value = true
  const { confirmPassword, ...apiPayload } = registerPayloadState
  try {
    const data = await register(apiPayload)
    if (data !== false) {
      navigateTo('/login')
    }
  }
  finally {
    isLoading.value = false
  }
}

const registerPayloadState = reactive<RegisterPayload & { confirmPassword: string }>({
  email: '',
  userPassword: '',
  confirmPassword: '',
})

const passwordShow = ref<boolean>(false)
const confirmPasswordShow = ref<boolean>(false)
</script>
