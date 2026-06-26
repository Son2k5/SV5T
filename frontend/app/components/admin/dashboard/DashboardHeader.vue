<template>
  <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center bg-white p-6 rounded-2xl border border-slate-100 shadow-xs">
    <div>
      <h1 class="text-2xl font-bold text-slate-900 tracking-tight">
        Dashboard
      </h1>
      <p class="mt-1 text-sm text-slate-500">
        Tổng quan hệ thống Quản lý Sinh viên 5 Tốt
      </p>
    </div>

    <div class="flex flex-wrap items-center gap-3">
      <div
        v-if="lastUpdated"
        class="text-xs text-slate-400 font-medium sm:text-right"
      >
        <span class="block">Cập nhật lúc:</span>
        <span class="font-semibold text-slate-600">{{ formattedTime }}</span>
      </div>

      <UButton
        color="neutral"
        variant="outline"
        icon="i-lucide-download"
        label="Xuất báo cáo"
        :disabled="loading"
        class="cursor-pointer font-semibold rounded-xl px-4 py-2 hover:bg-slate-50"
        @click="$emit('export')"
      />

      <UButton
        color="info"
        icon="i-lucide-refresh-cw"
        label="Làm mới"
        :loading="loading"
        class="cursor-pointer font-semibold rounded-xl px-4 py-2 shadow-sm"
        @click="$emit('refresh')"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  loading?: boolean
  lastUpdated?: Date | null
}>()

defineEmits<{
  refresh: []
  export: []
}>()

const formattedTime = computed(() => {
  if (!props.lastUpdated) return '--'
  return new Intl.DateTimeFormat('vi-VN', {
    dateStyle: 'short',
    timeStyle: 'medium',
  }).format(props.lastUpdated)
})
</script>
