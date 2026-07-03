<template>
  <div class="w-full overflow-x-auto rounded-xl border border-slate-200/70 bg-white">
    <UTable
      :data="applications"
      :columns="columns"
      :loading="loading"
      loading-color="info"
      empty="Không tìm thấy sinh viên nào tham gia đợt xét duyệt."
      class="min-w-[940px] text-slate-700"
      :ui="{
        thead: 'bg-slate-50 border-b border-slate-200/80',
        th: 'px-4 py-3.5 text-center text-[11px] font-semibold uppercase tracking-wide text-slate-600',
        td: 'px-4 py-3 text-center text-[13px] align-middle border-b border-slate-100/80',
        tr: 'hover:bg-slate-50/70 transition-colors',
      }"
    >
      <!-- Student Info Column -->
      <template #studentInfo-cell="{ row }">
        <div class="mx-auto flex min-w-60 max-w-76 items-center justify-center gap-2.5 py-0.5 text-left">
          <div class="flex size-9 shrink-0 items-center justify-center rounded-xl border border-blue-100 bg-blue-50 text-[13px] font-semibold text-blue-600 shadow-2xs">
            {{ getInitials(row.original.studentName || row.original.userEmail) }}
          </div>
          <div class="min-w-0">
            <h4 class="truncate text-[13px] font-semibold leading-tight text-slate-900">
              {{ row.original.studentName || 'Chưa cập nhật' }}
            </h4>
            <div class="mt-1 flex flex-wrap items-center gap-x-1.5 gap-y-1 text-[11px] text-slate-500">
              <span class="rounded-md bg-slate-100 px-1.5 py-0.5 font-mono font-medium text-slate-700">
                {{ row.original.studentCode || 'MSSV: N/A' }}
              </span>
              <span class="truncate">{{ row.original.userEmail }}</span>
            </div>
          </div>
        </div>
      </template>

      <!-- Campaign Column -->
      <template #campaignName-cell="{ row }">
        <div class="mx-auto max-w-64">
          <p class="line-clamp-2 text-[12px] font-medium leading-relaxed text-slate-600">
            {{ row.original.campaignName }}
          </p>
        </div>
      </template>

      <!-- Level Column -->
      <template #level-cell="{ row }">
        <UBadge
          :color="levelMeta(row.original.level).color"
          variant="subtle"
          class="rounded-lg px-2 py-0.5 text-[11px] font-semibold"
        >
          {{ levelMeta(row.original.level).label }}
        </UBadge>
      </template>

      <!-- Status Column -->
      <template #status-cell="{ row }">
        <UBadge
          :color="statusMeta(row.original.status).color"
          variant="subtle"
          class="rounded-lg px-2 py-0.5 text-[11px] font-semibold"
        >
          {{ statusMeta(row.original.status).label }}
        </UBadge>
      </template>

      <!-- Updated At Column -->
      <template #updatedAt-cell="{ row }">
        <div class="flex items-center justify-center gap-1.5 text-[12px] font-medium text-slate-500">
          <UIcon
            name="i-lucide-clock-3"
            class="size-3.5 shrink-0 text-slate-400"
          />
          <span>{{ formatDate(row.original.updatedAt || row.original.createdAt) }}</span>
        </div>
      </template>

      <!-- Actions Column -->
      <template #actions-cell="{ row }">
        <div class="flex justify-center">
          <UButton
            color="info"
            variant="soft"
            icon="i-lucide-eye"
            label="Xem minh chứng"
            size="xs"
            class="rounded-lg font-semibold cursor-pointer"
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
  const parts = name.split('@')[0]?.split(' ') || []
  if (parts.length >= 2) {
    const first = parts[0]?.charAt(0) || ''
    const last = parts[parts.length - 1]?.charAt(0) || ''
    return (first + last).toUpperCase()
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
