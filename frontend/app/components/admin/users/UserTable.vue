<template>
  <UTable
    :data="users"
    :columns="columns"
    :loading="loading"
    loading-color="info"
    empty="Không tìm thấy tài khoản phù hợp."
    class="w-full"
  >
    <template #fullName-cell="{ row }">
      <div class="min-w-36">
        <p class="font-semibold text-[#1E293B]">
          {{ row.original.fullName || 'Chưa cập nhật' }}
        </p>
        <p class="text-xs text-[#64748B]">
          {{ row.original.studentCode || row.original.phoneNumber || '—' }}
        </p>
      </div>
    </template>

    <template #role-cell="{ row }">
      <UBadge
        :color="roleMeta(row.original.role).color"
        variant="subtle"
        class="rounded-full"
      >
        {{ roleMeta(row.original.role).label }}
      </UBadge>
    </template>

    <template #active-cell="{ row }">
      <UBadge
        :color="row.original.active ? 'success' : 'error'"
        variant="subtle"
        class="rounded-full"
      >
        {{ row.original.active ? 'Hoạt động' : 'Đã vô hiệu' }}
      </UBadge>
    </template>

    <template #createdAt-cell="{ row }">
      {{ formatDateTime(row.original.createdAt) }}
    </template>

    <template #actions-cell="{ row }">
      <div class="flex justify-end gap-1">
        <UButton
          color="info"
          variant="ghost"
          icon="i-lucide-pen-line"
          aria-label="Chỉnh sửa tài khoản"
          @click="emit('edit', row.original)"
        />
        <UButton
          color="error"
          variant="ghost"
          icon="i-lucide-user-x"
          aria-label="Vô hiệu hóa tài khoản"
          :disabled="!row.original.active"
          @click="emit('delete', row.original)"
        />
      </div>
    </template>
  </UTable>
</template>

<script setup lang="ts">
import type { TableColumn } from '@nuxt/ui'
import type { AdminRole, AdminUser } from '~/types/admin'

defineProps<{ users: AdminUser[], loading?: boolean }>()

const emit = defineEmits<{
  edit: [user: AdminUser]
  delete: [user: AdminUser]
}>()

const columns: TableColumn<AdminUser>[] = [
  { accessorKey: 'fullName', header: 'Sinh viên' },
  { accessorKey: 'email', header: 'Email' },
  { accessorKey: 'role', header: 'Vai trò' },
  { accessorKey: 'active', header: 'Trạng thái' },
  { accessorKey: 'createdAt', header: 'Ngày tạo' },
  { id: 'actions', header: '' },
]

const roleMeta = (role: AdminRole) => ({
  ADMIN: { label: 'Quản trị viên', color: 'error' as const },
  MENTOR: { label: 'Cố vấn', color: 'warning' as const },
  USER: { label: 'Sinh viên', color: 'info' as const },
}[role])

const formatDateTime = (value: string) => new Intl.DateTimeFormat('vi-VN', {
  dateStyle: 'short',
  timeStyle: 'short',
}).format(new Date(value))
</script>
