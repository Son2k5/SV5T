<template>
  <UForm
    class="flex flex-col gap-6 w-full px-6 xl:px-16"
    :state="resetPasswordPayloadState"
    :schema="schema"
    @submit="handleResetPassword"
  >
    <p class="text-4xl text-info-500 font-bold text-center">
      Đặt Lại Mật Khẩu
    </p>
    <UFormField
      v-if="!resetPasswordTokenTimedout"
      label="Mật Khẩu Mới"
      name="newPw"
      :ui="{ label: 'text-lg' }"
    >
      <CommonPopper>
        <UInput
          v-model="resetPasswordPayloadState.newPw"
          class="w-full"
          color="neutral"
          placeholder="Nhập mật khẩu mới..."
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
      v-if="!resetPasswordTokenTimedout"
      label="Xác Nhận Mật Khẩu Mới"
      name="confirmPassword"
      :ui="{ label: 'text-lg' }"
    >
      <UInput
        v-model="resetPasswordPayloadState.confirmPassword"
        class="w-full"
        color="neutral"
        placeholder="Xác nhận mật khẩu mới..."
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
            aria-controls="password"
            @click="confirmPasswordShow = !confirmPasswordShow"
          />
        </template>
      </UInput>
    </UFormField>
    <UButton
      v-if="!resetPasswordTokenTimedout"
      class="h-10 cursor-pointer"
      color="info"
      label="Cập Nhật"
      :loading="isLoading"
      :ui="{ label: ['mx-auto text-lg', isLoading && 'hidden'], leadingIcon: 'mx-auto' }"
      type="submit"
    />
    <p
      v-else
      class="text-center"
    >
      Yêu cầu đã hết hạn! <br> Hãy tạo yêu cầu đổi mật khẩu mới.
    </p>
    <NuxtLink
      to="/login"
      class="text-info-500 hover:text-muted text-sm text-center"
    >Quay Lại Đăng Nhập</NuxtLink>
  </UForm>
</template>

<script setup lang="ts">
import type { ResetPasswordPayload } from '~/types/auth'
import { z } from 'zod'
import { REQS } from '~/constants/passwordRequirements'

definePageMeta({
  layout: 'auth',
  middleware: 'require-reset-password-token',
})

const route = useRoute()

const isLoading = ref<boolean>(false)

const schema = z
  .object({
    newPw: z.string().min(8, 'Mật khẩu phải có ít nhất 8 kí tự!'),
    confirmPassword: z.string(),
  })
  .refine(data => data.newPw === data.confirmPassword, {
    message: 'Mật khẩu xác nhận không trùng khớp!',
    path: ['confirmPassword'],
  })

const resetPasswordTokenTimedout = useState('resetPasswordTokenTimedout')

const { resetPassword } = useAuth()

let toastTimer: ReturnType<typeof setTimeout> | null = null
const handleResetPassword = async () => {
  isLoading.value = true
  const { confirmPassword, ...apiPayload } = resetPasswordPayloadState
  try {
    const data = await resetPassword(apiPayload)
    if (data !== false) {
      toastTimer = setTimeout(() => {
        navigateTo('/login')
      }, 2000)
    }
  }
  finally {
    isLoading.value = false
  }
}

const resetPasswordPayloadState = reactive<ResetPasswordPayload & { confirmPassword: string }>({
  token: route.query.token as string,
  newPw: '',
  confirmPassword: '',
})

const passwordShow = ref<boolean>(false)
const confirmPasswordShow = ref<boolean>(false)

onUnmounted(() => {
  if (toastTimer) {
    clearTimeout(toastTimer)
  }
})
</script>
