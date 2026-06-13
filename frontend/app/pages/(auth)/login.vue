<template>
  <UForm
    class="flex flex-col gap-6 w-full px-6 xl:px-16"
    :state="logInPayloadState"
    :schema="schema"
    @submit="handleLogIn"
  >
    <p class="text-4xl text-info-500 font-bold text-center">
      Đăng Nhập
    </p>
    <UFormField
      label="Email"
      name="email"
      :ui="{ label: 'text-lg' }"
    >
      <CommonPopper>
        <UInput
          v-model="logInPayloadState.email"
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
      label="Mật Khẩu"
      name="userPassword"
      :ui="{ label: 'text-lg' }"
    >
      <UInput
        v-model="logInPayloadState.userPassword"
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
    </UFormField>
    <div class="flex justify-between items-center">
      <UCheckbox
        color="neutral"
        label="Lưu Tài Khoản"
        :ui="{ base: 'cursor-pointer', label: 'cursor-pointer' }"
      />
      <NuxtLink
        to="/forgot_password"
        class="text-info-500 hover:text-muted text-sm"
      >Quên Mật Khẩu</NuxtLink>
    </div>
    <UButton
      class="h-10 cursor-pointer"
      color="info"
      label="Đăng Nhập"
      :loading="isLoading"
      :ui="{ label: ['mx-auto text-lg', isLoading && 'hidden'], leadingIcon: 'mx-auto' }"
      type="submit"
    />
    <p class="text-sm mx-auto">
      Chưa có tài khoản?
      <NuxtLink
        to="/signup"
        class="text-info-500 hover:text-muted"
      >Đăng Ký</NuxtLink>
    </p>
  </UForm>
</template>

<script setup lang="ts">
import type { LogInPayload } from '~/types/auth'
import { z } from 'zod'

definePageMeta({
  layout: 'auth',
})

const route = useRoute()
const router = useRouter()
const toast = useToast()

const isLoading = ref<boolean>(false)

const schema = z.object({
  email: z.email('Email không hợp lệ!'),
  userPassword: z.string().min(1, 'Vui lòng nhập mật khẩu!'),
})

const { logIn } = useAuth()

const handleLogIn = async () => {
  isLoading.value = true
  const data = await logIn(logInPayloadState)
  isLoading.value = false
  if (data) {
    navigateTo('/dashboard/me')
  }
}

const passwordShow = ref<boolean>(false)

const logInPayloadState = reactive<LogInPayload>({
  email: '',
  userPassword: '',
})

const errorKeys: Record<string, { title: string, desc: string }> = {
  unauthorized: { title: 'Chưa được xác minh!', desc: 'Vui lòng đăng nhập để truy cập' },
  invalid_token: { title: 'Link không hợp lệ!', desc: 'Vui lòng đăng ký để làm mới' },
}

onMounted(() => {
  const errorKey = errorKeys[route.query.error as string]

  if (route.query.error && errorKey) {
    toast.add({
      title: errorKey.title,
      description: errorKey.desc,
      color: 'error',
    })
    router.replace('/login')
  }
})
</script>
