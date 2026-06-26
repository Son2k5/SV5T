<template>
  <UModal
    :open="open"
    :title="isEditing ? 'Cập nhật tài khoản' : 'Tạo tài khoản mới'"
    scrollable
    @update:open="emit('update:open', $event)"
  >
    <template #body>
      <form
        class="grid gap-4 sm:grid-cols-2"
        @submit.prevent="submit"
      >
        <UFormField
          label="Họ và tên"
          name="fullName"
          :error="errors.fullName"
        >
          <UInput
            v-model="form.fullName"
            placeholder="Nguyễn Văn A"
            autocomplete="name"
          />
        </UFormField>
        <UFormField
          label="Mã sinh viên"
          name="studentCode"
          :error="errors.studentCode"
        >
          <UInput
            v-model="form.studentCode"
            placeholder="22A1xxxx"
          />
        </UFormField>
        <UFormField
          label="Email"
          name="email"
          :error="errors.email"
          class="sm:col-span-2"
        >
          <UInput
            v-model="form.email"
            type="email"
            placeholder="email@hanu.edu.vn"
            autocomplete="email"
          />
        </UFormField>
        <UFormField
          v-if="!isEditing"
          label="Mật khẩu"
          name="password"
          :error="errors.password"
          class="sm:col-span-2"
        >
          <UInput
            v-model="form.password"
            type="password"
            placeholder="Tối thiểu 8 ký tự"
            autocomplete="new-password"
          />
        </UFormField>
        <UFormField
          label="Vai trò"
          name="role"
          :error="errors.role"
        >
          <USelect
            v-model="form.role"
            :items="roleOptions"
          />
        </UFormField>
        <UFormField
          label="Trạng thái"
          name="active"
        >
          <USelect
            v-model="form.active"
            :items="statusOptions"
          />
        </UFormField>
      </form>
    </template>
    <template #footer>
      <div class="flex w-full justify-end gap-3">
        <UButton
          color="neutral"
          variant="outline"
          label="Hủy"
          :disabled="loading"
          @click="emit('update:open', false)"
        />
        <UButton
          color="info"
          :loading="loading"
          :label="isEditing ? 'Lưu thay đổi' : 'Tạo tài khoản'"
          @click="submit"
        />
      </div>
    </template>
  </UModal>
</template>

<script setup lang="ts">
import type { AdminRole, AdminUser, AdminUserForm } from '~/types/admin'

const props = defineProps<{
  open: boolean
  user?: AdminUser | null
  loading?: boolean
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  'submit': [form: AdminUserForm]
}>()

type FieldErrors = Partial<Record<'email' | 'password' | 'role' | 'fullName' | 'studentCode', string>>

const blankForm = (): AdminUserForm => ({
  email: '',
  password: '',
  role: 'USER',
  active: true,
  fullName: '',
  studentCode: '',
})

const form = reactive<AdminUserForm>(blankForm())
const errors = reactive<FieldErrors>({})
const isEditing = computed(() => Boolean(props.user))

const roleOptions: Array<{ label: string, value: AdminRole }> = [
  { label: 'Sinh viên', value: 'USER' },
  { label: 'Cố vấn', value: 'MENTOR' },
  { label: 'Quản trị viên', value: 'ADMIN' },
]

const statusOptions = [
  { label: 'Hoạt động', value: true },
  { label: 'Vô hiệu hóa', value: false },
]

watch(() => [props.open, props.user] as const, () => {
  Object.assign(form, props.user
    ? {
        email: props.user.email,
        role: props.user.role,
        active: props.user.active,
        fullName: props.user.fullName || '',
        studentCode: props.user.studentCode || '',
      }
    : blankForm())
  Object.assign(errors, { email: undefined, password: undefined, role: undefined, fullName: undefined, studentCode: undefined })
}, { immediate: true })

const submit = () => {
  Object.assign(errors, { email: undefined, password: undefined, role: undefined, fullName: undefined, studentCode: undefined })
  if (!form.email || !/^\S+@\S+\.\S+$/.test(form.email)) errors.email = 'Email không hợp lệ.'
  if (!isEditing.value && (!form.password || form.password.length < 8)) errors.password = 'Mật khẩu tối thiểu 8 ký tự.'
  if (!form.role) errors.role = 'Vui lòng chọn vai trò.'

  // AdminService only accepts these two fields when a complete UserDetail already exists.
  const hasProfileValue = Boolean(form.fullName?.trim() || form.studentCode?.trim())
  const profileCanBePatched = Boolean(props.user?.fullName || props.user?.studentCode)
  if (hasProfileValue && (!isEditing.value || !profileCanBePatched)) {
    errors.fullName = 'API yêu cầu hồ sơ sinh viên đầy đủ; hãy để trống hai trường này khi tạo tài khoản mới.'
  }

  if (Object.values(errors).some(Boolean)) return
  emit('submit', { ...form, email: form.email.trim() })
}
</script>
