<template>
  <main class="min-h-screen bg-[#F8FAFC] pb-12">
    <section class="mb-6 rounded-2xl border border-slate-100 bg-white p-6 shadow-xs">
      <div class="flex flex-col justify-between gap-4 md:flex-row md:items-end">
        <div>
          <p class="text-xs font-black uppercase tracking-[0.18em] text-[#075EA8]">
            Sinh viên 5 Tốt
          </p>
          <h1 class="mt-2 flex items-center gap-2 text-2xl font-black text-slate-800">
            <UIcon
              name="i-heroicons-folder-open-solid"
              class="text-[#075EA8]"
            />
            Hồ sơ đã nộp
          </h1>
          <p class="mt-1 max-w-2xl text-sm font-medium text-slate-500">
            Lưu trữ các hồ sơ đã gửi xét duyệt, tách riêng khỏi màn hình xét minh chứng đang mở.
          </p>
        </div>

        <div class="flex items-center gap-2 rounded-xl border border-slate-200 bg-slate-50 p-1">
          <button
            v-for="tab in levelTabs"
            :key="tab.value"
            type="button"
            :class="[
              'h-9 rounded-lg px-3 text-xs font-black uppercase tracking-wider transition cursor-pointer',
              activeLevel === tab.value
                ? 'bg-[#075EA8] text-white shadow-xs'
                : 'text-slate-500 hover:bg-white hover:text-slate-800',
            ]"
            @click="changeLevel(tab.value)"
          >
            {{ tab.shortLabel }}
          </button>
        </div>
      </div>
    </section>

    <section class="space-y-4">
      <div class="flex flex-col justify-between gap-3 border-b border-slate-100 pb-3 md:flex-row md:items-center">
        <div>
          <h2 class="flex items-center gap-2 text-xl font-black text-slate-800">
            <UIcon
              name="i-lucide-archive"
              class="text-blue-600"
            />
            Danh sách hồ sơ
          </h2>
          <p class="mt-1 text-xs font-bold text-slate-400">
            Cấp: {{ activeLevelLabel }} · Tổng: {{ allSubmittedRecords.length }}
          </p>
        </div>

        <UButton
          to="/application-records"
          color="neutral"
          variant="outline"
          icon="i-lucide-clipboard-check"
          label="Xét minh chứng"
          class="rounded-xl font-bold"
        />
      </div>

      <div
        v-if="isLoading"
        class="grid grid-cols-1 gap-4"
      >
        <div
          v-for="i in 3"
          :key="i"
          class="rounded-2xl border border-slate-200/80 bg-white p-5 shadow-xs"
        >
          <div class="h-5 w-2/3 animate-pulse rounded bg-slate-200" />
          <div class="mt-3 h-4 w-1/3 animate-pulse rounded bg-slate-100" />
        </div>
      </div>

      <div
        v-else-if="allSubmittedRecords.length === 0"
        class="rounded-2xl border border-slate-200/60 bg-white p-10 text-center text-slate-500 shadow-xs"
      >
        <UIcon
          name="i-heroicons-inbox"
          class="mx-auto size-11 text-slate-300"
        />
        <p class="mt-3 text-sm font-bold">
          Chưa có hồ sơ đã nộp nào ở cấp này
        </p>
        <p class="mt-1 text-xs text-slate-400">
          Sau khi gửi hồ sơ xét duyệt, hồ sơ sẽ được lưu tại đây để bạn xem lại minh chứng.
        </p>
      </div>

      <div
        v-else
        class="grid grid-cols-1 gap-4"
      >
        <div
          v-for="rec in allSubmittedRecords"
          :key="rec.publicId"
          class="flex flex-col justify-between gap-4 rounded-2xl border border-slate-200/80 bg-white p-5 shadow-xs md:flex-row md:items-center"
        >
          <div>
            <h3 class="text-base font-bold text-slate-800">
              {{ rec.campaignName }}
            </h3>
            <div class="mt-2 flex flex-wrap items-center gap-x-3 gap-y-1 text-xs font-semibold text-slate-500">
              <span class="rounded border border-blue-100 bg-blue-50 px-2 py-0.5 text-blue-600">
                {{ rec.isGroup ? 'Tập thể' : 'Cá nhân' }}
              </span>
              <span>·</span>
              <span>Cấp: {{ getLevelLabel(rec.level) }}</span>
              <span>·</span>
              <span>Cập nhật: {{ formatDate(rec.updatedAt || rec.createdAt) }}</span>
            </div>
          </div>

          <div class="flex items-center gap-3 self-end md:self-center">
            <UBadge
              :color="statusMeta(rec.status).color"
              variant="subtle"
              class="rounded-full px-2.5 py-0.5 text-xs font-bold"
            >
              {{ statusMeta(rec.status).label }}
            </UBadge>
            <UButton
              color="info"
              variant="solid"
              icon="i-lucide-eye"
              label="Xem minh chứng"
              size="xs"
              class="rounded-xl font-bold shadow-xs transition-transform hover:scale-102"
              :to="submittedRecordRoute(rec)"
            />
          </div>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import type { ApplicationRecordResponse, CampaignLevel } from '~/types/applicationRecord'
import { useApplicationRecord } from '~/composables/useApplicationRecord'

definePageMeta({
  layout: 'default',
})

const {
  submitted,
  reviewing,
  approved,
  isLoading,
  fetchRecords,
} = useApplicationRecord()

const activeLevel = ref<CampaignLevel>('UNIVERSITY')

const levelTabs = [
  { label: 'Cấp trường', shortLabel: 'Trường', value: 'UNIVERSITY' as CampaignLevel },
  { label: 'Cấp thành phố', shortLabel: 'Thành phố', value: 'CITY' as CampaignLevel },
  { label: 'Cấp trung ương', shortLabel: 'Trung ương', value: 'NATION' as CampaignLevel },
]

const activeLevelLabel = computed(() => {
  const match = levelTabs.find(t => t.value === activeLevel.value)
  return match ? match.label : 'Cấp trường'
})

const allSubmittedRecords = computed(() => {
  const records = [...submitted.value, ...reviewing.value, ...approved.value]
  return records.filter(r => r.level === activeLevel.value)
})

const changeLevel = (level: CampaignLevel) => {
  activeLevel.value = level
}

const submittedRecordRoute = (rec: ApplicationRecordResponse) => ({
  path: `/submitted-records/${rec.publicId}`,
  query: {
    campaignId: rec.campaignPublicId,
    mode: rec.isGroup ? 'group' : 'individual',
    level: rec.level,
  },
})

const getLevelLabel = (level?: string) => {
  switch (level?.toUpperCase()) {
    case 'UNIVERSITY':
      return 'Cấp trường'
    case 'CITY':
      return 'Cấp thành phố'
    case 'NATION':
      return 'Cấp trung ương'
    default:
      return level || 'N/A'
  }
}

const formatDate = (dateStr?: string) => {
  if (!dateStr) return 'N/A'
  try {
    return new Date(dateStr).toLocaleString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    })
  }
  catch {
    return dateStr
  }
}

const statusMeta = (status: string) => {
  switch (status?.toUpperCase()) {
    case 'APPROVED':
    case 'PASS':
      return { label: 'Đạt yêu cầu', color: 'success' as const }
    case 'REJECTED':
    case 'FAIL':
      return { label: 'Cần bổ sung', color: 'error' as const }
    case 'SUBMITTED':
      return { label: 'Đã nộp hồ sơ', color: 'info' as const }
    case 'REVIEWING':
    case 'PENDING':
      return { label: 'Đang xét duyệt', color: 'warning' as const }
    case 'DRAFT':
    default:
      return { label: 'Nháp', color: 'neutral' as const }
  }
}

onMounted(async () => {
  await fetchRecords()
})
</script>
