<template>
  <div class="sv-card p-6 flex flex-col h-full border border-slate-100 shadow-sm">
    <SectionTitle
      title="Thông báo hệ thống"
      icon="i-lucide-bell-ring"
    />

    <div class="mt-2 space-y-3.5 flex-1">
      <!-- Pending Evidences Alert -->
      <div
        v-if="pendingEvidences > 0"
        class="flex gap-3 bg-amber-50/70 border border-amber-100 rounded-xl p-3.5"
      >
        <div class="size-8 rounded-lg bg-amber-100 text-amber-700 flex items-center justify-center shrink-0">
          <UIcon
            name="i-lucide-clock"
            class="size-4.5"
          />
        </div>
        <div>
          <h5 class="text-xs font-bold text-amber-900">
            Hồ sơ & Minh chứng chờ duyệt
          </h5>
          <p class="text-[11px] text-amber-700 mt-0.5 leading-normal">
            Hiện đang có <span class="font-bold">{{ pendingEvidences }}</span> minh chứng chưa được chấm điểm. Vui lòng kiểm tra và xử lý.
          </p>
          <NuxtLink
            to="/admin/evidence"
            class="text-[10px] font-bold text-amber-800 underline mt-1.5 block hover:text-amber-950 transition"
          >
            Chuyển đến trang duyệt &rarr;
          </NuxtLink>
        </div>
      </div>

      <!-- Campaign Deadlines Alert -->
      <div
        v-if="endingCampaigns && endingCampaigns.length"
        class="space-y-2.5"
      >
        <div
          v-for="c in endingCampaigns"
          :key="c.publicId"
          class="flex gap-3 bg-rose-50/70 border border-rose-100 rounded-xl p-3.5"
        >
          <div class="size-8 rounded-lg bg-rose-100 text-rose-700 flex items-center justify-center shrink-0">
            <UIcon
              name="i-lucide-hourglass"
              class="size-4.5"
            />
          </div>
          <div class="min-w-0 flex-1">
            <h5 class="text-xs font-bold text-rose-900 truncate">
              {{ c.name }} sắp kết thúc!
            </h5>
            <p class="text-[11px] text-rose-700 mt-0.5 leading-normal">
              Đợt xét duyệt sẽ tự đóng vào ngày <span class="font-semibold">{{ formatDate(c.endDate) }}</span>.
            </p>
            <div class="mt-1 text-[10px] text-rose-600 font-bold">
              Còn lại: {{ getDaysRemaining(c.endDate) }}
            </div>
          </div>
        </div>
      </div>

      <!-- System Tip / Healthy State -->
      <div
        v-if="pendingEvidences === 0 && (!endingCampaigns || !endingCampaigns.length)"
        class="flex flex-col items-center justify-center h-full text-center py-6"
      >
        <div class="size-11 rounded-full bg-emerald-50 border border-emerald-100 text-emerald-600 flex items-center justify-center mb-3">
          <UIcon
            name="i-lucide-check-circle"
            class="size-5"
          />
        </div>
        <p class="text-xs font-bold text-slate-800">
          Hệ thống hoạt động tốt!
        </p>
        <p class="text-[11px] text-slate-400 mt-0.5 max-w-[200px] leading-normal">
          Không có cảnh báo hoặc công việc tồn đọng cần xử lý khẩn cấp.
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Campaign } from '~/types/admin'
import SectionTitle from './SectionTitle.vue'

const props = defineProps<{
  pendingEvidences: number
  campaigns: Campaign[]
}>()

// Active campaigns ending in the next 15 days
const endingCampaigns = computed(() => {
  if (!props.campaigns) return []
  const now = new Date()
  return props.campaigns
    .filter((c) => {
      if (c.status !== 'ACTIVE' || !c.endDate) return false
      const end = new Date(c.endDate)
      const diffTime = end.getTime() - now.getTime()
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
      return diffDays >= 0 && diffDays <= 15
    })
    .slice(0, 2)
})

const formatDate = (dateStr: string | null) => {
  if (!dateStr) return '--'
  return new Intl.DateTimeFormat('vi-VN', { dateStyle: 'short' }).format(new Date(dateStr))
}

const getDaysRemaining = (endDateStr: string | null) => {
  if (!endDateStr) return ''
  const end = new Date(endDateStr)
  const diffTime = end.getTime() - new Date().getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  if (diffDays === 0) return 'Hôm nay'
  return `${diffDays} ngày`
}
</script>
