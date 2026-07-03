<template>
  <div class="space-y-6">
    <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
      <div>
        <h1 class="text-2xl font-bold text-[#1E293B]">
          Cài đặt tài khoản
        </h1>
        <p class="mt-1 text-sm text-[#64748B]">
          Quản lý bảo mật, phiên đăng nhập và các tùy chọn hiển thị cá nhân.
        </p>
      </div>
      <div class="flex flex-wrap gap-3 sm:justify-end">
        <UButton
          color="neutral"
          variant="outline"
          icon="i-lucide-user-pen"
          label="Chỉnh sửa hồ sơ"
          to="/dashboard/me/edit"
        />
        <UButton
          color="info"
          icon="i-lucide-bell"
          label="Thông báo"
          to="/notifications"
        />
      </div>
    </div>

    <div class="grid gap-4 md:grid-cols-3">
      <div
        v-for="stat in accountStats"
        :key="stat.label"
        class="sv-card flex items-center gap-4 p-5"
      >
        <div
          class="flex size-12 shrink-0 items-center justify-center rounded-xl"
          :class="stat.iconClass"
        >
          <UIcon
            :name="stat.icon"
            class="size-6"
          />
        </div>
        <div class="min-w-0">
          <p class="text-sm font-semibold text-[#64748B]">
            {{ stat.label }}
          </p>
          <p class="mt-1 truncate text-xl font-bold text-[#1E293B]">
            {{ stat.value }}
          </p>
        </div>
      </div>
    </div>

    <div class="grid gap-6 xl:grid-cols-[minmax(0,1.1fr)_minmax(320px,0.9fr)]">
      <div class="space-y-6">
        <CommonPageSection
          title="Tài khoản"
          title-icon="i-lucide-id-card"
          inner-class="!block"
        >
          <div class="flex flex-col gap-5 sm:flex-row sm:items-center">
            <UAvatar
              :src="profile?.avatar || '/profilePlaceholder.png'"
              :alt="profile?.fullName || profile?.email || 'Người dùng'"
              size="3xl"
              class="shrink-0"
            />
            <div class="min-w-0 flex-1">
              <div class="flex flex-wrap items-center gap-2">
                <h2 class="break-words text-xl font-bold text-[#1E293B]">
                  {{ profile?.fullName || 'Chưa cập nhật họ tên' }}
                </h2>
                <UBadge
                  :color="profile?.active ? 'success' : 'error'"
                  variant="soft"
                  :label="profile?.active ? 'Đang hoạt động' : 'Tạm khóa'"
                />
              </div>
              <p class="mt-1 break-words text-sm text-[#64748B]">
                {{ profile?.email || '-' }}
              </p>
              <div class="mt-3 flex flex-wrap gap-2">
                <UBadge
                  color="info"
                  variant="soft"
                  :label="roleLabel"
                />
                <UBadge
                  :color="profileCompleted ? 'success' : 'warning'"
                  variant="soft"
                  :label="profileCompleted ? 'Hồ sơ đầy đủ' : 'Cần bổ sung hồ sơ'"
                />
              </div>
            </div>
          </div>

          <dl class="mt-6 divide-y divide-[#E5E7EB]">
            <div
              v-for="item in accountDetails"
              :key="item.label"
              class="grid gap-1 py-4 sm:grid-cols-[12rem_minmax(0,1fr)] sm:gap-4"
            >
              <dt class="text-sm font-semibold text-[#64748B]">
                {{ item.label }}
              </dt>
              <dd class="break-words text-sm font-semibold text-[#1E293B]">
                {{ item.value }}
              </dd>
            </div>
          </dl>
        </CommonPageSection>

        <CommonPageSection
          title="Bảo mật"
          title-icon="i-lucide-shield-check"
          inner-class="!block"
        >
          <form
            class="space-y-5"
            @submit.prevent="submitPassword"
          >
            <div class="grid gap-5 lg:grid-cols-3">
              <UFormField
                label="Mật khẩu hiện tại"
                name="currentPassword"
                :error="passwordErrors.currentPassword"
              >
                <UInput
                  v-model="passwordForm.currentPassword"
                  autocomplete="current-password"
                  :type="passwordVisible.current ? 'text' : 'password'"
                  placeholder="Nhập mật khẩu hiện tại"
                >
                  <template #trailing>
                    <UButton
                      color="neutral"
                      variant="link"
                      size="sm"
                      :icon="passwordVisible.current ? 'i-lucide-eye-off' : 'i-lucide-eye'"
                      :aria-label="passwordVisible.current ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
                      @click="passwordVisible.current = !passwordVisible.current"
                    />
                  </template>
                </UInput>
              </UFormField>

              <UFormField
                label="Mật khẩu mới"
                name="newPassword"
                :error="passwordErrors.newPassword"
              >
                <UInput
                  v-model="passwordForm.newPassword"
                  autocomplete="new-password"
                  :type="passwordVisible.next ? 'text' : 'password'"
                  placeholder="Tối thiểu 8 ký tự"
                >
                  <template #trailing>
                    <UButton
                      color="neutral"
                      variant="link"
                      size="sm"
                      :icon="passwordVisible.next ? 'i-lucide-eye-off' : 'i-lucide-eye'"
                      :aria-label="passwordVisible.next ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
                      @click="passwordVisible.next = !passwordVisible.next"
                    />
                  </template>
                </UInput>
              </UFormField>

              <UFormField
                label="Nhập lại mật khẩu mới"
                name="confirmPassword"
                :error="passwordErrors.confirmPassword"
              >
                <UInput
                  v-model="passwordForm.confirmPassword"
                  autocomplete="new-password"
                  :type="passwordVisible.confirm ? 'text' : 'password'"
                  placeholder="Xác nhận mật khẩu"
                >
                  <template #trailing>
                    <UButton
                      color="neutral"
                      variant="link"
                      size="sm"
                      :icon="passwordVisible.confirm ? 'i-lucide-eye-off' : 'i-lucide-eye'"
                      :aria-label="passwordVisible.confirm ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
                      @click="passwordVisible.confirm = !passwordVisible.confirm"
                    />
                  </template>
                </UInput>
              </UFormField>
            </div>

            <div class="flex flex-col gap-4 border-t border-[#E5E7EB] pt-5 lg:flex-row lg:items-center lg:justify-between">
              <div class="text-sm text-[#64748B]">
                <p
                  v-for="requirement in REQS"
                  :key="requirement"
                >
                  {{ requirement }}
                </p>
                <p>Đổi mật khẩu sẽ yêu cầu đăng nhập lại.</p>
              </div>
              <UButton
                type="submit"
                color="info"
                icon="i-lucide-key-round"
                label="Đổi mật khẩu"
                :loading="passwordSubmitting"
                class="justify-center"
              />
            </div>
          </form>
        </CommonPageSection>
      </div>

      <div class="space-y-6">
        <CommonPageSection
          title="Tùy chọn hiển thị"
          title-icon="i-lucide-sliders-horizontal"
          inner-class="!block"
        >
          <div class="divide-y divide-[#E5E7EB]">
            <div class="flex items-start justify-between gap-4 py-4 first:pt-0">
              <div>
                <p class="text-sm font-bold text-[#1E293B]">
                  Chuông thông báo
                </p>
                <p class="mt-1 text-sm text-[#64748B]">
                  Hiển thị lối tắt thông báo trên thanh trên cùng.
                </p>
              </div>
              <UCheckbox
                v-model="settings.showNotificationBell"
                aria-label="Hiển thị chuông thông báo"
              />
            </div>

            <div class="flex items-start justify-between gap-4 py-4">
              <div>
                <p class="text-sm font-bold text-[#1E293B]">
                  Hộp chat nhanh
                </p>
                <p class="mt-1 text-sm text-[#64748B]">
                  Giữ widget chat ở góc màn hình trong khu vực user.
                </p>
              </div>
              <UCheckbox
                v-model="settings.showChatWidget"
                aria-label="Hiển thị hộp chat nhanh"
              />
            </div>

            <div class="flex items-start justify-between gap-4 py-4 last:pb-0">
              <div>
                <p class="text-sm font-bold text-[#1E293B]">
                  Mở rộng thanh điều hướng
                </p>
                <p class="mt-1 text-sm text-[#64748B]">
                  Áp dụng trạng thái sidebar cho phiên làm việc hiện tại.
                </p>
              </div>
              <UCheckbox
                v-model="settings.sidebarExpanded"
                aria-label="Mở rộng thanh điều hướng"
              />
            </div>
          </div>

          <div class="mt-5 border-t border-[#E5E7EB] pt-5">
            <UButton
              color="neutral"
              variant="outline"
              icon="i-lucide-rotate-ccw"
              label="Đặt lại tùy chọn"
              @click="resetSettings"
            />
          </div>
        </CommonPageSection>

        <CommonPageSection
          title="Phiên đăng nhập"
          title-icon="i-lucide-monitor-check"
          inner-class="!block"
        >
          <div class="space-y-4">
            <div class="flex items-start gap-3">
              <div class="flex size-10 shrink-0 items-center justify-center rounded-xl bg-emerald-50 text-emerald-600">
                <UIcon
                  name="i-lucide-check"
                  class="size-5"
                />
              </div>
              <div>
                <p class="text-sm font-bold text-[#1E293B]">
                  Phiên hiện tại đang hoạt động
                </p>
                <p class="mt-1 text-sm text-[#64748B]">
                  Tài khoản sẽ tự đăng xuất khi không hoạt động quá thời gian cho phép.
                </p>
              </div>
            </div>

            <UButton
              color="error"
              variant="soft"
              icon="i-lucide-log-out"
              label="Đăng xuất khỏi thiết bị này"
              block
              @click="() => logOut()"
            />
          </div>
        </CommonPageSection>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { REQS } from '~/constants/passwordRequirements'
import { useCurrentUser } from '~/composables/profile/useCurrentUser'
import { isProfileComplete } from '~/types/profile'
import { getErrorMessage } from '~/utils/errors'

definePageMeta({ layout: 'default' })

const toast = useToast()
const { profile } = await useCurrentUser()
const { changePassword, logOut } = useAuth()
const { settings, resetSettings } = useUserSettings()
const { sidebarOpen } = useSidebar()
const { isMobile } = useMobile()

const passwordSubmitting = ref(false)
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
})
const passwordErrors = reactive<Partial<Record<'currentPassword' | 'newPassword' | 'confirmPassword', string>>>({})
const passwordVisible = reactive({
  current: false,
  next: false,
  confirm: false,
})

const roleLabels: Record<string, string> = {
  USER: 'Sinh viên',
  MENTOR: 'Cố vấn',
  ADMIN: 'Quản trị viên',
}

const hasText = (value: unknown) =>
  typeof value === 'string' ? value.trim().length > 0 : Boolean(value)

const profileFields = computed(() => [
  profile.value?.fullName,
  profile.value?.birthDay,
  profile.value?.gender,
  profile.value?.identityCardNumber,
  profile.value?.ethnicity,
  profile.value?.school,
  profile.value?.academicYear,
  profile.value?.studentCode,
  profile.value?.administrativeClass,
  profile.value?.faculty,
  profile.value?.currentPosition,
  profile.value?.permanentAddress?.provinceCity,
  profile.value?.permanentAddress?.district,
  profile.value?.permanentAddress?.detailAddress,
  profile.value?.temporaryAddress?.provinceCity,
  profile.value?.temporaryAddress?.district,
  profile.value?.temporaryAddress?.detailAddress,
  profile.value?.contactEmail,
  profile.value?.phoneNumber,
  profile.value?.politicalStatus,
])

const profileCompletion = computed(() => {
  const fields = profileFields.value
  if (!fields.length) return 0

  const completed = fields.filter(hasText).length
  return Math.round((completed / fields.length) * 100)
})

const profileCompleted = computed(() => isProfileComplete(profile.value))
const roleLabel = computed(() => roleLabels[profile.value?.role || 'USER'] || 'Người dùng')

const accountStats = computed(() => [
  {
    label: 'Hoàn thiện hồ sơ',
    value: `${profileCompletion.value}%`,
    icon: 'i-lucide-clipboard-check',
    iconClass: profileCompleted.value ? 'bg-emerald-50 text-emerald-600' : 'bg-amber-50 text-amber-600',
  },
  {
    label: 'Vai trò',
    value: roleLabel.value,
    icon: 'i-lucide-badge-check',
    iconClass: 'bg-blue-50 text-[#2563EB]',
  },
  {
    label: 'Trạng thái',
    value: profile.value?.active ? 'Hoạt động' : 'Tạm khóa',
    icon: profile.value?.active ? 'i-lucide-circle-check' : 'i-lucide-circle-x',
    iconClass: profile.value?.active ? 'bg-emerald-50 text-emerald-600' : 'bg-red-50 text-red-600',
  },
])

const accountDetails = computed(() => [
  { label: 'Email tài khoản', value: profile.value?.email || '-' },
  { label: 'Email liên hệ', value: profile.value?.contactEmail || '-' },
  { label: 'Số điện thoại', value: profile.value?.phoneNumber || '-' },
  { label: 'Mã sinh viên', value: profile.value?.studentCode || '-' },
  { label: 'Cập nhật lần cuối', value: formatDateTime(profile.value?.updatedAt) },
])

const formatDateTime = (value?: string | null) => {
  if (!value) return 'Chưa có dữ liệu'

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return 'Chưa có dữ liệu'

  return new Intl.DateTimeFormat('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

const clearPasswordErrors = () => {
  Object.assign(passwordErrors, {
    currentPassword: undefined,
    newPassword: undefined,
    confirmPassword: undefined,
  })
}

const validatePasswordForm = () => {
  clearPasswordErrors()

  if (!passwordForm.currentPassword.trim()) {
    passwordErrors.currentPassword = 'Vui lòng nhập mật khẩu hiện tại.'
  }
  if (passwordForm.newPassword.length < 8) {
    passwordErrors.newPassword = 'Mật khẩu mới tối thiểu 8 ký tự.'
  }
  if (passwordForm.newPassword === passwordForm.currentPassword && passwordForm.newPassword.length > 0) {
    passwordErrors.newPassword = 'Mật khẩu mới phải khác mật khẩu hiện tại.'
  }
  if (passwordForm.confirmPassword !== passwordForm.newPassword) {
    passwordErrors.confirmPassword = 'Mật khẩu nhập lại chưa khớp.'
  }

  return !Object.values(passwordErrors).some(Boolean)
}

const resetPasswordForm = () => {
  Object.assign(passwordForm, {
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  })
  Object.assign(passwordVisible, {
    current: false,
    next: false,
    confirm: false,
  })
  clearPasswordErrors()
}

const submitPassword = async () => {
  if (!validatePasswordForm()) return

  passwordSubmitting.value = true
  try {
    await changePassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword,
    })
    resetPasswordForm()
    toast.add({
      title: 'Đã đổi mật khẩu',
      description: 'Vui lòng đăng nhập lại để tiếp tục.',
      color: 'success',
    })
    await logOut()
  }
  catch (cause) {
    toast.add({
      title: 'Không thể đổi mật khẩu',
      description: getErrorMessage(cause, 'Vui lòng kiểm tra mật khẩu hiện tại và thử lại.'),
      color: 'error',
    })
  }
  finally {
    passwordSubmitting.value = false
  }
}

watch(
  () => [settings.value.sidebarExpanded, isMobile.value] as const,
  ([expanded, mobile]) => {
    if (!mobile) {
      sidebarOpen.value = expanded
    }
  },
  { immediate: true },
)
</script>
