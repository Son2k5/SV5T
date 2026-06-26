<template>
  <CommonPageSection
    title="Tiến độ duyệt minh chứng theo đợt"
    title-icon="i-lucide-chart-no-axes-combined"
    inner-class="!block"
  >
    <div
      v-if="items.length"
      class="space-y-5"
      aria-label="Biểu đồ tiến độ duyệt minh chứng"
    >
      <div
        v-for="item in items"
        :key="item.campaignId"
        class="space-y-2"
      >
        <div class="flex items-center justify-between gap-4">
          <p class="truncate text-sm font-semibold text-[#1E293B]">
            {{ item.campaignName }}
          </p>
          <p class="shrink-0 text-sm font-bold text-[#2563EB]">
            {{ item.percent }}%
          </p>
        </div>
        <div class="h-3 overflow-hidden rounded-full bg-slate-100">
          <div
            class="h-full rounded-full bg-gradient-to-r from-[#2563EB] to-[#60A5FA] transition-all duration-500"
            :style="{ width: `${item.percent}%` }"
            :aria-label="`${item.percent}% minh chứng đã được duyệt hoặc từ chối`"
          />
        </div>
        <p class="text-xs text-[#64748B]">
          {{ item.reviewed }} / {{ item.total }} minh chứng đã được xử lý
        </p>
      </div>
    </div>

    <div
      v-else
      class="py-8 text-center text-sm text-[#64748B]"
    >
      Chưa có đợt xét chọn đang hoạt động.
    </div>
  </CommonPageSection>
</template>

<script setup lang="ts">
import type { CampaignProgress } from '~/types/admin'

defineProps<{ items: CampaignProgress[] }>()
</script>
