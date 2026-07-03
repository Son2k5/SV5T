<template>
  <div class="sv-card sv-card-hover p-6 flex flex-col justify-between h-full bg-white relative overflow-hidden group">
    <!-- Active Status Glow Indicator -->
    <div
      v-if="campaign.status === 'ACTIVE'"
      class="absolute top-0 left-0 w-2 h-full bg-emerald-500"
    />
    <div
      v-else-if="campaign.status === 'COMING_SOON'"
      class="absolute top-0 left-0 w-2 h-full bg-amber-500"
    />
    <div
      v-else
      class="absolute top-0 left-0 w-2 h-full bg-slate-400"
    />

    <div>
      <div class="flex items-start justify-between gap-4">
        <h3 class="text-lg font-extrabold text-slate-800 leading-snug group-hover:text-[#075EA8] transition-colors duration-300">
          {{ campaign.name }}
        </h3>

        <!-- Status Badge -->
        <span
          :class="[
            'shrink-0 text-xs font-black px-2.5 py-1 rounded-full ring-1',
            statusMeta.class,
          ]"
        >
          {{ statusMeta.label }}
        </span>
      </div>

      <!-- Description -->
      <p class="mt-3 text-sm text-slate-500 leading-relaxed">
        {{ campaign.description || `Đợt xét duyệt chính thức danh hiệu Sinh viên 5 tốt cấp ${levelLabel} cho học kỳ này. Vui lòng theo dõi thời gian mở/đóng đợt xét để hoàn thiện hồ sơ.` }}
      </p>

      <!-- Time metadata -->
      <div class="mt-5 grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs font-semibold text-slate-400">
        <div class="flex items-center gap-2">
          <UIcon
            name="i-heroicons-calendar-days"
            class="size-4 text-slate-400"
          />
          <span>Thời gian: {{ formatDate(campaign.startDate) }} - {{ formatDate(campaign.endDate) }}</span>
        </div>
        <div class="flex items-center gap-2">
          <UIcon
            name="i-heroicons-academic-cap"
            class="size-4 text-slate-400"
          />
          <span>Năm học: {{ campaign.academicYear }} - {{ campaign.academicYear + 1 }}</span>
        </div>
      </div>
    </div>

    <!-- Footer metadata (No action buttons) -->
    <div class="mt-6 pt-4 border-t border-slate-50 flex items-center justify-between">
      <span class="text-xs font-bold text-slate-400">
        Đối tượng tham gia: <span class="text-slate-600 font-extrabold">{{ isGroupLabel }}</span>
      </span>
      <span class="text-xs font-bold text-[#075EA8] flex items-center gap-1">
        <UIcon
          name="i-heroicons-information-circle"
          class="size-4"
        />
        Sự kiện đang hoạt động
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { CampaignSummaryResponse } from '~/types/applicationRecord'

const props = defineProps<{
  campaign: CampaignSummaryResponse
}>()

const levelLabel = computed(() => {
  switch (props.campaign.level) {
    case 'CITY': return 'Thành phố'
    case 'NATION': return 'Trung ương'
    default: return 'Trường'
  }
})

const isGroupLabel = computed(() => {
  if (props.campaign.isGroup === 'GROUP') return 'Tập thể'
  if (props.campaign.isGroup === 'BOTH') return 'Cá nhân & Nhóm'
  return 'Cá nhân'
})

const statusMeta = computed(() => {
  if (props.campaign.status === 'ACTIVE') {
    return { label: 'Đang mở', class: 'bg-emerald-50 text-emerald-700 ring-emerald-100' }
  }
  if (props.campaign.status === 'COMING_SOON') {
    return { label: 'Sắp mở', class: 'bg-amber-50 text-amber-700 ring-amber-100' }
  }
  return { label: 'Đã đóng', class: 'bg-slate-50 text-slate-500 ring-slate-200' }
})

function formatDate(dateStr?: string) {
  if (!dateStr) return 'Chưa xác định'
  const date = new Date(dateStr)
  return date.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
}
</script>
