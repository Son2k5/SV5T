<template>
  <UForm
    class="flex w-full flex-col gap-5"
    :state="logInPayloadState"
    :schema="schema"
    @submit="handleLogIn"
  >
    <p class="text-center text-3xl font-bold text-[#1E293B]">
      Đăng Nhập
    </p>
    <UFormField
      label="Email"
      name="email"
      :ui="{ label: 'text-sm font-semibold text-[#334155]' }"
    >
      <CommonPopper>
        <UInput
          v-model="logInPayloadState.email"
          class="w-full"
          color="neutral"
          placeholder="Nhập email..."
          :ui="{ base: 'h-11' }"
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
      :ui="{ label: 'text-sm font-semibold text-[#334155]' }"
    >
      <UInput
        v-model="logInPayloadState.userPassword"
        class="w-full"
        color="neutral"
        placeholder="Nhập mật khẩu..."
        :ui="{ base: 'h-11' }"
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
        class="text-sm font-semibold text-[#2563EB] hover:text-[#1D4ED8]"
      >Quên Mật Khẩu</NuxtLink>
    </div>
    <UButton
      class="h-11 cursor-pointer shadow-sm"
      color="info"
      label="Đăng Nhập"
      :loading="isLoading"
      :ui="{ label: ['mx-auto text-lg', isLoading && 'hidden'], leadingIcon: 'mx-auto' }"
      type="submit"
    />
    <p class="mx-auto text-sm text-[#64748B]">
      Chưa có tài khoản?
      <NuxtLink
        to="/signup"
        class="font-semibold text-[#2563EB] hover:text-[#1D4ED8]"
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

const { accessToken, logIn } = useAuth()

const handleLogIn = async () => {
  if (isLoading.value) return

  isLoading.value = true
  try {
    const data = await logIn(logInPayloadState)
    if (data && accessToken.value) {
      await router.replace('/dashboard')
    }
  }
  finally {
    isLoading.value = false
  }
}

const passwordShow = ref<boolean>(false)

const logInPayloadState = reactive<LogInPayload>({
  email: '',
  userPassword: '',
})

const errorKeys: Record<string, { title: string, desc: string }> = {
  unauthorized: { title: 'Cần đăng nhập!', desc: 'Vui lòng đăng nhập để tiếp tục.' },
  session_expired: { title: 'Phiên đăng nhập đã hết hạn!', desc: 'Vui lòng đăng nhập lại để tiếp tục.' },
  session_idle: { title: 'Đã tự động đăng xuất!', desc: 'Tài khoản không hoạt động trong 3 giờ.' },
  invalid_token: { title: 'Liên kết không hợp lệ!', desc: 'Vui lòng yêu cầu một liên kết mới.' },
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
