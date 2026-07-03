<template>
  <div class="sv-card sv-card-hover p-5 flex flex-col md:flex-row gap-5 items-center justify-between bg-white">
    <div class="flex flex-col sm:flex-row gap-5 items-center w-full min-w-0">
      <!-- Thumbnail representation exactly like mockup (pink lollipop on blue plate aesthetic) -->
      <div class="size-24 rounded-2xl shrink-0 bg-gradient-to-tr from-pink-100 to-blue-200 flex items-center justify-center border border-slate-100 overflow-hidden shadow-inner">
        <!-- Inside shape similar to the lollipop graphic -->
        <div class="relative size-14 rounded-full bg-blue-100 flex items-center justify-center shadow-sm">
          <div class="size-6 rounded-full bg-pink-400 border border-white animate-bounce" />
          <div class="absolute bottom-1 w-0.5 h-6 bg-[#075EA8]" />
        </div>
      </div>

      <!-- Record Meta Info -->
      <div class="min-w-0 flex-1 text-center sm:text-left">
        <h3 class="text-lg font-extrabold text-slate-800 line-clamp-1 leading-snug">
          {{ record.campaignName }}
        </h3>

        <p class="mt-1 text-sm text-slate-500 font-semibold">
          {{ record.note || 'Sinh viên 5 Tốt cá nhân cấp trường.' }}
        </p>

        <div class="mt-3 flex flex-wrap justify-center sm:justify-start gap-2">
          <span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-blue-50 text-blue-700 ring-1 ring-blue-100">
            {{ record.isGroup ? 'Tập thể' : 'Cá nhân' }}
          </span>
          <span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-indigo-50 text-indigo-700 ring-1 ring-indigo-100">
            {{ levelLabel }}
          </span>
        </div>
      </div>
    </div>

    <!-- Actions & Status right align -->
    <div class="shrink-0 flex flex-row md:flex-col items-center md:items-end justify-between md:justify-center gap-3 w-full md:w-auto border-t md:border-t-0 pt-3 md:pt-0 mt-2 md:mt-0">
      <div class="flex items-center gap-1.5">
        <span class="text-xs font-black text-slate-800 uppercase tracking-wider">TRẠNG THÁI:</span>
        <span :class="['inline-flex items-center rounded-full px-3 py-1 text-xs font-black ring-1', statusMeta.class]">
          {{ statusMeta.label }}
        </span>
      </div>

      <div class="flex items-center gap-2">
        <UButton
          size="sm"
          label="+ Truy cập"
          class="cursor-pointer font-bold px-4 py-2 rounded-lg bg-[#075EA8] hover:bg-[#064f8d] text-white shadow-sm transition-all"
          @click="$emit('continue', record)"
        />
        <UButton
          size="sm"
          label="Xóa"
          variant="outline"
          class="cursor-pointer font-bold px-4 py-2 rounded-lg border border-slate-200 text-slate-500 hover:bg-slate-50 transition-all"
          @click="$emit('delete', record.publicId)"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ApplicationRecordResponse } from '~/types/applicationRecord'

const props = defineProps<{
  record: ApplicationRecordResponse
}>()

defineEmits<{
  continue: [record: ApplicationRecordResponse]
  delete: [publicId: string]
}>()

const levelLabel = computed(() => {
  switch (props.record.level) {
    case 'CITY': return 'Cấp Thành phố'
    case 'NATION': return 'Cấp Trung ương'
    default: return 'Cấp Trường'
  }
})

const statusMeta = computed(() => {
  switch (props.record.status) {
    case 'DRAFT':
      return { label: 'Chưa hoàn thành', class: 'bg-orange-50 text-orange-700 ring-orange-100' }
    case 'SUBMITTED':
      return { label: 'Đang chờ duyệt', class: 'bg-blue-50 text-blue-700 ring-blue-100' }
    case 'APPROVED':
      return { label: 'Đã duyệt', class: 'bg-emerald-50 text-emerald-700 ring-emerald-100' }
    case 'REJECTED':
      return { label: 'Đã trả lại', class: 'bg-red-50 text-red-700 ring-red-100' }
    default:
      return { label: 'Chưa tạo', class: 'bg-slate-50 text-slate-600 ring-slate-100' }
  }
})
</script>
