<template>
  <div class="space-y-6">
    <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
      <div>
        <h1 class="text-2xl font-bold text-[#1E293B]">
          Quản lý người dùng
        </h1>
        <p class="mt-1 text-sm text-[#64748B]">
          Quản lý tài khoản sinh viên, cố vấn và quản trị viên.
        </p>
      </div>
      <UButton
        color="info"
        icon="i-lucide-user-plus"
        label="Tạo tài khoản"
        @click="openCreate"
      />
    </div>

    <CommonPageSection inner-class="!block">
      <div class="mb-5 flex flex-col gap-3 lg:flex-row lg:items-center">
        <UInput
          v-model="search"
          class="w-full lg:max-w-md"
          icon="i-lucide-search"
          placeholder="Tìm theo tên, email hoặc mã sinh viên"
        />
        <USelect
          v-model="role"
          class="w-full lg:w-48"
          :items="roleOptions"
          placeholder="Tất cả vai trò"
        />
        <USelect
          v-model="active"
          class="w-full lg:w-48"
          :items="statusOptions"
          placeholder="Tất cả trạng thái"
        />
      </div>

      <div
        v-if="error"
        class="mb-4 flex flex-wrap items-center justify-between gap-3 rounded-2xl bg-red-50 p-4 text-sm text-red-700"
      >
        <span>{{ error }}</span>
        <UButton
          color="error"
          variant="soft"
          label="Thử lại"
          @click="load"
        />
      </div>

      <CommonPageLoading v-if="loading && !pageData" />
      <CommonPageEmpty
        v-else-if="pageData && !pageData.content.length"
        title="Chưa có người dùng"
        desc="Thử đổi bộ lọc hoặc tạo tài khoản mới."
      />
      <template v-else>
        <AdminUsersUserTable
          :users="pageData?.content || []"
          :loading="loading"
          @edit="openEdit"
          @delete="askDelete"
        />
        <div
          v-if="pageData"
          class="mt-5 flex flex-col justify-between gap-3 sm:flex-row sm:items-center"
        >
          <p class="text-sm text-[#64748B]">
            Tổng {{ pageData.totalElements }} tài khoản
          </p>
          <UPagination
            v-model:page="uiPage"
            :total="pageData.totalElements"
            :items-per-page="pageData.size"
          />
        </div>
      </template>
    </CommonPageSection>

    <AdminUsersUserFormModal
      v-model:open="formOpen"
      :user="editingUser"
      :loading="submitting"
      @submit="saveUser"
    />
    <AdminSharedConfirmDeleteModal
      v-model:open="deleteOpen"
      title="Vô hiệu hóa tài khoản?"
      :description="`Tài khoản ${deletingUser?.email || ''} sẽ không thể đăng nhập. Bạn có thể kích hoạt lại sau.`"
      confirm-label="Vô hiệu hóa"
      :loading="submitting"
      @confirm="confirmDelete"
    />
  </div>
</template>

<script setup lang="ts">
import type { AdminPage, AdminRole, AdminUser, AdminUserForm } from '~/types/admin'
import { useAdminUsers } from '~/composables/admin/useAdminUsers'
import { getErrorMessage } from '~/utils/errors'

definePageMeta({ layout: 'admin', middleware: 'require-admin' })

const route = useRoute()
const toast = useToast()
const { fetchUsers, createUser, updateUser, deleteUser } = useAdminUsers()

const search = ref(typeof route.query.keyword === 'string' ? route.query.keyword : '')
const role = ref<AdminRole | undefined>()
const active = ref<boolean | undefined>()
const uiPage = ref(1)
const pageData = ref<AdminPage<AdminUser> | null>(null)
const loading = ref(false)
const submitting = ref(false)
const error = ref('')
const formOpen = ref(false)
const deleteOpen = ref(false)
const editingUser = ref<AdminUser | null>(null)
const deletingUser = ref<AdminUser | null>(null)

const roleOptions: Array<{ label: string, value?: AdminRole }> = [
  { label: 'Tất cả vai trò', value: undefined },
  { label: 'Sinh viên', value: 'USER' },
  { label: 'Cố vấn', value: 'MENTOR' },
  { label: 'Quản trị viên', value: 'ADMIN' },
]
const statusOptions: Array<{ label: string, value?: boolean }> = [
  { label: 'Tất cả trạng thái', value: undefined },
  { label: 'Hoạt động', value: true },
  { label: 'Vô hiệu hóa', value: false },
]

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await fetchUsers(uiPage.value - 1, search.value, { role: role.value, active: active.value })
    pageData.value = response.data
  }
  catch (cause) {
    error.value = getErrorMessage(cause, 'Không thể tải danh sách người dùng.')
  }
  finally {
    loading.value = false
  }
}

const debouncedLoad = useDebounceFn(() => {
  uiPage.value = 1
  load()
}, 350)

watch(search, debouncedLoad)
watch([role, active], () => {
  uiPage.value = 1
  load()
})
watch(uiPage, load)

const openCreate = () => {
  editingUser.value = null
  formOpen.value = true
}
const openEdit = (user: AdminUser) => {
  editingUser.value = user
  formOpen.value = true
}
const askDelete = (user: AdminUser) => {
  deletingUser.value = user
  deleteOpen.value = true
}

const saveUser = async (form: AdminUserForm) => {
  submitting.value = true
  try {
    if (editingUser.value) {
      await updateUser(editingUser.value.publicId, form)
      toast.add({ title: 'Đã cập nhật tài khoản', color: 'success' })
    }
    else {
      await createUser(form)
      toast.add({ title: 'Đã tạo tài khoản', color: 'success' })
    }
    formOpen.value = false
    await load()
  }
  catch (cause) {
    toast.add({ title: 'Không thể lưu tài khoản', description: getErrorMessage(cause, 'Vui lòng kiểm tra lại dữ liệu.'), color: 'error' })
  }
  finally {
    submitting.value = false
  }
}

const confirmDelete = async () => {
  if (!deletingUser.value) return
  submitting.value = true
  try {
    await deleteUser(deletingUser.value.publicId)
    toast.add({ title: 'Đã vô hiệu hóa tài khoản', color: 'success' })
    deleteOpen.value = false
    await load()
  }
  catch (cause) {
    toast.add({ title: 'Không thể vô hiệu hóa tài khoản', description: getErrorMessage(cause, 'Vui lòng thử lại.'), color: 'error' })
  }
  finally {
    submitting.value = false
  }
}

onMounted(load)
</script>
