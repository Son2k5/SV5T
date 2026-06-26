<template>
  <div class="space-y-6">
    <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
      <div>
        <h1 class="text-2xl font-bold text-[#1E293B]">
          Cấu hình hệ thống
        </h1>
        <p class="mt-1 text-sm text-[#64748B]">
          Quản lý các cấu hình key–value do hệ thống cung cấp.
        </p>
      </div>
      <UButton
        color="info"
        icon="i-lucide-plus"
        label="Thêm cấu hình"
        @click="openCreate"
      />
    </div>

    <CommonPageLoading v-if="loading && !settings.length" />
    <CommonPageSection
      v-else-if="error"
      title="Không thể tải cấu hình"
      title-icon="i-lucide-circle-alert"
      inner-class="flex-col items-start gap-4"
    >
      <p class="text-sm text-red-600">
        {{ error }}
      </p>
      <UButton
        color="info"
        label="Thử lại"
        @click="load"
      />
    </CommonPageSection>
    <CommonPageEmpty
      v-else-if="!settings.length"
      title="Chưa có cấu hình"
      desc="Tạo cấu hình đầu tiên cho hệ thống."
    />
    <CommonPageSection
      v-else
      inner-class="!block p-0"
    >
      <UTable
        :data="settings"
        :columns="columns"
        :loading="loading"
        class="w-full"
      >
        <template #keyName-cell="{ row }">
          <code class="rounded-lg bg-blue-50 px-2 py-1 text-xs font-semibold text-[#2563EB]">{{ row.original.keyName }}</code>
        </template>
        <template #value-cell="{ row }">
          <p class="max-w-xl break-words text-sm text-[#1E293B]">
            {{ row.original.value }}
          </p>
        </template>
        <template #description-cell="{ row }">
          <p class="max-w-xs text-sm text-[#64748B]">
            {{ row.original.description || '—' }}
          </p>
        </template>
        <template #actions-cell="{ row }">
          <div class="flex justify-end gap-1">
            <UButton
              color="info"
              variant="ghost"
              icon="i-lucide-pen-line"
              aria-label="Sửa cấu hình"
              @click="openEdit(row.original)"
            />
            <UButton
              color="error"
              variant="ghost"
              icon="i-lucide-trash-2"
              aria-label="Xóa cấu hình"
              @click="askDelete(row.original)"
            />
          </div>
        </template>
      </UTable>
    </CommonPageSection>

    <UModal
      :open="formOpen"
      :title="editing ? 'Cập nhật cấu hình' : 'Tạo cấu hình'"
      @update:open="formOpen = $event"
    >
      <template #body>
        <form
          class="space-y-4"
          @submit.prevent="save"
        >
          <UFormField
            label="Khóa cấu hình"
            name="keyName"
            :error="formErrors.keyName"
          >
            <UInput
              v-model="form.keyName"
              :disabled="Boolean(editing)"
              placeholder="Ví dụ: system.name"
            />
          </UFormField>
          <UFormField
            label="Giá trị"
            name="value"
            :error="formErrors.value"
          >
            <UTextarea
              v-model="form.value"
              :rows="4"
              placeholder="Giá trị cấu hình"
            />
          </UFormField>
          <UFormField
            label="Mô tả"
            name="description"
          >
            <UTextarea
              v-model="form.description"
              :rows="3"
              placeholder="Mô tả ý nghĩa của cấu hình"
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
            :disabled="submitting"
            @click="formOpen = false"
          />
          <UButton
            color="info"
            :loading="submitting"
            :label="editing ? 'Lưu thay đổi' : 'Tạo cấu hình'"
            @click="save"
          />
        </div>
      </template>
    </UModal>

    <AdminSharedConfirmDeleteModal
      v-model:open="deleteOpen"
      title="Xóa cấu hình?"
      :description="`Cấu hình ${deleting?.keyName || ''} sẽ bị xóa vĩnh viễn.`"
      :loading="submitting"
      @confirm="confirmDelete"
    />
  </div>
</template>

<script setup lang="ts">
import type { TableColumn } from '@nuxt/ui'
import type { SystemSetting, SystemSettingForm } from '~/types/admin'
import { useAdminSettings } from '~/composables/admin/useAdminSettings'
import { getErrorMessage } from '~/utils/errors'

definePageMeta({ layout: 'admin', middleware: 'require-admin' })

const toast = useToast()
const { fetchSettings, createSetting, updateSetting, deleteSetting } = useAdminSettings()

const columns: TableColumn<SystemSetting>[] = [
  { accessorKey: 'keyName', header: 'Khóa' },
  { accessorKey: 'value', header: 'Giá trị' },
  { accessorKey: 'description', header: 'Mô tả' },
  { id: 'actions', header: '' },
]

const settings = ref<SystemSetting[]>([])
const loading = ref(false)
const submitting = ref(false)
const error = ref('')
const formOpen = ref(false)
const deleteOpen = ref(false)
const editing = ref<SystemSetting | null>(null)
const deleting = ref<SystemSetting | null>(null)
const form = reactive<SystemSettingForm>({ keyName: '', value: '', description: '' })
const formErrors = reactive<Partial<Record<'keyName' | 'value', string>>>({})

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await fetchSettings()
    settings.value = response.data
  }
  catch (cause) {
    error.value = getErrorMessage(cause, 'Không thể tải cấu hình hệ thống.')
  }
  finally {
    loading.value = false
  }
}

const openCreate = () => {
  editing.value = null
  Object.assign(form, { keyName: '', value: '', description: '' })
  formOpen.value = true
}
const openEdit = (setting: SystemSetting) => {
  editing.value = setting
  Object.assign(form, { keyName: setting.keyName, value: setting.value, description: setting.description || '' })
  formOpen.value = true
}
const askDelete = (setting: SystemSetting) => {
  deleting.value = setting
  deleteOpen.value = true
}

const save = async () => {
  Object.assign(formErrors, { keyName: undefined, value: undefined })
  if (!editing.value && !form.keyName.trim()) formErrors.keyName = 'Khóa cấu hình là bắt buộc.'
  if (!form.value.trim()) formErrors.value = 'Giá trị cấu hình là bắt buộc.'
  if (Object.values(formErrors).some(Boolean)) return

  submitting.value = true
  try {
    if (editing.value) {
      await updateSetting(editing.value.keyName, { value: form.value, description: form.description })
      toast.add({ title: 'Đã cập nhật cấu hình', color: 'success' })
    }
    else {
      await createSetting({ keyName: form.keyName.trim(), value: form.value, description: form.description })
      toast.add({ title: 'Đã tạo cấu hình', color: 'success' })
    }
    formOpen.value = false
    await load()
  }
  catch (cause) {
    toast.add({ title: 'Không thể lưu cấu hình', description: getErrorMessage(cause, 'Vui lòng thử lại.'), color: 'error' })
  }
  finally {
    submitting.value = false
  }
}

const confirmDelete = async () => {
  if (!deleting.value) return
  submitting.value = true
  try {
    await deleteSetting(deleting.value.keyName)
    toast.add({ title: 'Đã xóa cấu hình', color: 'success' })
    deleteOpen.value = false
    await load()
  }
  catch (cause) {
    toast.add({ title: 'Không thể xóa cấu hình', description: getErrorMessage(cause, 'Vui lòng thử lại.'), color: 'error' })
  }
  finally {
    submitting.value = false
  }
}

onMounted(load)
</script>
