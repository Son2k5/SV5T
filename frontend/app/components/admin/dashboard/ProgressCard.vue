<template>
  <div class="sv-card p-6 flex flex-col h-full border border-slate-100 shadow-sm">
    <SectionTitle
      title="Tiến độ xét duyệt"
      icon="i-lucide-trello"
    />

    <div
      v-if="items && items.length"
      class="mt-2 space-y-5 flex-1"
    >
      <div
        v-for="item in items"
        :key="item.campaignId"
        class="space-y-2 group"
      >
        <div class="flex items-center justify-between gap-4">
          <p class="truncate text-sm font-bold text-slate-800 transition duration-150 group-hover:text-blue-600">
            {{ item.campaignName }}
          </p>
          <span class="shrink-0 text-xs font-extrabold text-blue-600 bg-blue-50 px-2 py-0.5 rounded-lg border border-blue-100/50">
            {{ item.percent }}%
          </span>
        </div>

        <!-- Progress track -->
        <div class="h-3 overflow-hidden rounded-full bg-slate-100 border border-slate-200/20">
          <div
            class="h-full rounded-full bg-gradient-to-r from-blue-500 via-blue-400 to-sky-400 transition-all duration-1000 ease-out"
            :style="{ width: `${item.percent}%` }"
          />
        </div>

        <div class="flex justify-between items-center text-[11px] text-slate-400 font-semibold">
          <span>{{ item.reviewed }} / {{ item.total }} minh chứng đã xử lý</span>
          <span
            v-if="item.percent === 100"
            class="text-emerald-500 flex items-center gap-1"
          >
            <UIcon
              name="i-lucide-check-circle-2"
              class="size-3"
            /> Hoàn thành
          </span>
        </div>
      </div>
    </div>

    <!-- Empty progress list fallback -->
    <div
      v-else
      class="flex-1 flex flex-col items-center justify-center py-10 text-center"
    >
      <div class="size-12 rounded-full bg-slate-50 border border-slate-200/60 flex items-center justify-center text-slate-400 mb-3 shadow-2xs">
        <UIcon
          name="i-lucide-calendar"
          class="size-6"
        />
      </div>
      <p class="text-sm font-semibold text-slate-600">
        Chưa có đợt xét chọn đang chạy
      </p>
      <p class="text-xs text-slate-400 mt-1">
        Kích hoạt đợt xét chọn để theo dõi tiến độ nộp/duyệt.
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { CampaignProgress } from '~/types/admin'
import SectionTitle from './SectionTitle.vue'

defineProps<{
  items: CampaignProgress[]
}>()
</script>
