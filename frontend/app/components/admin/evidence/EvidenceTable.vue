<template>
  <UTable
    :data="evidences"
    :columns="columns"
    :loading="loading"
    loading-color="info"
    empty="Không có minh chứng phù hợp."
    class="w-full"
  >
    <template #studentName-cell="{ row }">
      <div>
        <p class="font-semibold text-[#1E293B]">
          {{ row.original.studentName || 'Chưa cập nhật' }}
        </p>
        <p class="text-xs text-[#64748B]">
          {{ row.original.userEmail }}
        </p>
      </div>
    </template>

    <template #criteriaName-cell="{ row }">
      <div>
        <p class="font-medium text-[#1E293B]">
          {{ row.original.criteriaName }}
        </p>
        <p class="text-xs text-[#64748B]">
          {{ row.original.campaignName }}
        </p>
      </div>
    </template>

    <template #evidenceUrl-cell="{ row }">
      <a
        :href="row.original.evidenceUrl"
        target="_blank"
        rel="noopener noreferrer"
        class="inline-flex max-w-44 items-center gap-1 truncate font-medium text-[#2563EB] hover:underline"
      >
        <UIcon
          name="i-lucide-external-link"
          class="size-4 shrink-0"
        />
        {{ row.original.evidenceOriginalFilename || 'Xem minh chứng' }}
      </a>
    </template>

    <template #status-cell="{ row }">
      <UBadge
        :color="statusMeta(row.original.status).color"
        variant="subtle"
        class="rounded-full"
      >
        {{ statusMeta(row.original.status).label }}
      </UBadge>
    </template>

    <template #actions-cell="{ row }">
      <UButton
        color="info"
        variant="ghost"
        icon="i-lucide-clipboard-check"
        aria-label="Xem và duyệt minh chứng"
        @click="emit('review', row.original)"
      />
    </template>
  </UTable>
</template>

<script setup lang="ts">
import type { TableColumn } from '@nuxt/ui'
import type { Evidence, EvidenceStatus } from '~/types/admin'

defineProps<{ evidences: Evidence[], loading?: boolean }>()

const emit = defineEmits<{ review: [evidence: Evidence] }>()

const columns: TableColumn<Evidence>[] = [
  { accessorKey: 'studentName', header: 'Sinh viên' },
  { accessorKey: 'criteriaName', header: 'Tiêu chí' },
  { accessorKey: 'evidenceUrl', header: 'Tệp minh chứng' },
  { accessorKey: 'status', header: 'Trạng thái' },
  { id: 'actions', header: '' },
]

const statusMeta = (status: EvidenceStatus) => ({
  PENDING: { label: 'Chờ duyệt', color: 'warning' as const },
  APPROVED: { label: 'Đã duyệt', color: 'success' as const },
  REJECTED: { label: 'Từ chối', color: 'error' as const },
}[status])
</script>
