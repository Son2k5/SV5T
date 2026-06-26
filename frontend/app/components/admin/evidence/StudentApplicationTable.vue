<template>
  <div class="w-full bg-white rounded-2xl border border-slate-200/80 shadow-xs overflow-hidden">
    <UTable
      :data="applications"
      :columns="columns"
      :loading="loading"
      loading-color="info"
      empty="Không tìm thấy sinh viên nào tham gia đợt xét duyệt."
      class="w-full"
    >
      <!-- Student Info Column -->
      <template #studentInfo-cell="{ row }">
        <div class="flex items-center gap-3 py-2.5">
          <div class="flex size-10 shrink-0 items-center justify-center rounded-full bg-gradient-to-tr from-blue-50 to-indigo-50 border border-blue-100 text-sm font-bold text-[#2563EB]">
            {{ getInitials(row.original.studentName || row.original.userEmail) }}
          </div>
          <div class="min-w-0">
            <h4 class="font-semibold text-sm text-[#1E293B] leading-tight truncate">
              {{ row.original.studentName || 'Chưa cập nhật' }}
            </h4>
            <div class="flex flex-wrap items-center gap-x-2 gap-y-0.5 mt-1 text-xs text-[#64748B]">
              <span class="font-mono bg-slate-100 px-1.5 py-0.2 rounded text-slate-700 font-medium">
                {{ row.original.studentCode || 'MSSV: N/A' }}
              </span>
              <span class="truncate">{{ row.original.userEmail }}</span>
            </div>
          </div>
        </div>
      </template>

      <!-- Campaign Column -->
      <template #campaignName-cell="{ row }">
        <span class="text-xs font-semibold text-[#475569] line-clamp-2 max-w-[220px]">
          {{ row.original.campaignName }}
        </span>
      </template>

      <!-- Level Column -->
      <template #level-cell="{ row }">
        <UBadge
          :color="levelMeta(row.original.level).color"
          variant="subtle"
          class="rounded-full px-2.5 py-0.5 font-bold text-xs"
        >
          {{ levelMeta(row.original.level).label }}
        </UBadge>
      </template>

      <!-- Status Column -->
      <template #status-cell="{ row }">
        <UBadge
          :color="statusMeta(row.original.status).color"
          variant="subtle"
          class="rounded-full px-2.5 py-0.5 font-bold text-xs"
        >
          {{ statusMeta(row.original.status).label }}
        </UBadge>
      </template>

      <!-- Updated At Column -->
      <template #updatedAt-cell="{ row }">
        <span class="text-xs text-[#64748B] font-medium">
          {{ formatDate(row.original.updatedAt || row.original.createdAt) }}
        </span>
      </template>

      <!-- Actions Column -->
      <template #actions-cell="{ row }">
        <div class="flex justify-end pr-2">
          <UButton
            color="info"
            variant="solid"
            icon="i-lucide-eye"
            label="Xem minh chứng"
            size="xs"
            class="rounded-xl font-bold shadow-xs hover:scale-102 transition-transform cursor-pointer"
            @click="emit('view-details', row.original)"
          />
        </div>
      </template>
    </UTable>
  </div>
</template>

<script setup lang="ts">
import type { TableColumn } from '@nuxt/ui'
import type { StudentApplication } from '~/types/admin'

const props = defineProps<{
  applications: StudentApplication[]
  loading?: boolean
  isGroup?: boolean
}>()

const emit = defineEmits<{
  'view-details': [application: StudentApplication]
}>()

const columns = computed<TableColumn<StudentApplication>[]>(() => [
  { accessorKey: 'studentInfo', header: props.isGroup ? 'Đại diện tập thể' : 'Sinh viên' },
  { accessorKey: 'campaignName', header: 'Đợt xét chọn' },
  { accessorKey: 'level', header: 'Cấp hồ sơ' },
  { accessorKey: 'status', header: 'Trạng thái hồ sơ' },
  { accessorKey: 'updatedAt', header: 'Ngày cập nhật' },
  { id: 'actions', header: '' },
])

function getInitials(name: string) {
  const parts = name.split('@')[0].split(' ')
  if (parts.length >= 2) {
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
  }
  return name.substring(0, 2).toUpperCase()
}

function formatDate(dateStr?: string) {
  if (!dateStr) return 'N/A'
  try {
    return new Date(dateStr).toLocaleString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    })
  }
  catch {
    return dateStr
  }
}

const levelMeta = (level: string) => {
  switch (level?.toUpperCase()) {
    case 'UNIVERSITY':
      return { label: 'Cấp trường', color: 'primary' as const }
    case 'CITY':
      return { label: 'Cấp thành phố', color: 'info' as const }
    case 'NATION':
      return { label: 'Cấp trung ương', color: 'secondary' as const }
    default:
      return { label: level || 'N/A', color: 'neutral' as const }
  }
}

const statusMeta = (status: string) => {
  switch (status?.toUpperCase()) {
    case 'APPROVED':
    case 'PASS':
      return { label: 'Đạt yêu cầu', color: 'success' as const }
    case 'REJECTED':
    case 'FAIL':
      return { label: 'Không đạt', color: 'error' as const }
    case 'SUBMITTED':
      return { label: 'Đã nộp hồ sơ', color: 'info' as const }
    case 'DRAFT':
    default:
      return { label: 'Nháp (Đang làm)', color: 'warning' as const }
  }
}
</script>
