<script setup lang="ts">
import type { ApplicationStatus } from '~/types/applicationRecord'

const props = defineProps<{
  status?: ApplicationStatus
}>()

const statusMeta = computed(() => {
  switch (props.status) {
    case 'DRAFT':
      return { label: 'Đang làm', class: 'bg-amber-50 text-amber-700 ring-amber-100' }
    case 'SUBMITTED':
      return { label: 'Đã nộp', class: 'bg-blue-50 text-blue-700 ring-blue-100' }
    case 'APPROVED':
      return { label: 'Đã duyệt', class: 'bg-emerald-50 text-emerald-700 ring-emerald-100' }
    case 'REJECTED':
      return { label: 'Cần bổ sung', class: 'bg-red-50 text-red-700 ring-red-100' }
    default:
      return { label: 'Chưa tạo', class: 'bg-slate-50 text-slate-600 ring-slate-100' }
  }
})
</script>

<template>
  <span :class="['inline-flex items-center rounded-full px-4 py-2 text-sm font-black ring-1', statusMeta.class]">
    {{ statusMeta.label }}
  </span>
</template>
