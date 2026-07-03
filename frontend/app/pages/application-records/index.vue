<template>
  <main class="min-h-screen bg-[#F8FAFC] pb-12">
    <!-- SECTION 1 — Hero giới thiệu -->
    <section class="relative overflow-hidden rounded-3xl bg-gradient-to-br from-[#5B5FEF] via-[#5F8EE8] to-[#5BB9DD] px-6 py-12 text-center text-white shadow-lg md:px-12 md:py-16">
      <!-- Background subtle floating circles -->
      <div class="absolute -right-10 -top-10 size-40 rounded-full bg-white/10 blur-2xl" />
      <div class="absolute -left-10 -bottom-10 size-40 rounded-full bg-white/10 blur-2xl" />

      <p class="text-xs font-black uppercase tracking-[0.2em] text-white/90 md:text-sm">
        Hội Sinh viên Trường Đại học Hà Nội
      </p>

      <h1 class="mx-auto mt-4 max-w-3xl text-3xl font-black leading-tight uppercase tracking-normal md:text-5xl">
        Xét chọn danh hiệu<br>
        <span class="text-yellow-300">“Sinh viên 5 tốt”</span>
      </h1>

      <!-- Current academic year badge -->
      <div class="mx-auto mt-4 inline-flex items-center rounded-full bg-white/15 px-4 py-1.5 text-xs font-black tracking-wider text-white shadow-sm ring-1 ring-white/20">
        NĂM HỌC 2025 - 2026
      </div>

      <p class="mx-auto mt-5 max-w-xl text-sm font-medium text-white/90 leading-relaxed">
        Hệ thống xét duyệt minh chứng trực tuyến. Sinh viên chọn cấp xét chọn tương ứng để khai báo thông tin rèn luyện và tải hồ sơ minh chứng.
      </p>
    </section>

    <!-- Level Tab Controls: Floating card overlapping the banner -->
    <div class="relative z-20 mx-auto -mt-8 flex w-full max-w-2xl items-center justify-between gap-2 rounded-2xl border border-slate-100 bg-white p-2 shadow-[0_12px_30px_-4px_rgba(15,23,42,0.08)]">
      <button
        v-for="tab in levelTabs"
        :key="tab.value"
        type="button"
        :class="[
          'flex-1 h-11 rounded-xl text-xs font-black uppercase tracking-wider transition-all duration-300 cursor-pointer text-center',
          activeLevel === tab.value
            ? 'bg-[#075EA8] text-white shadow-sm'
            : 'text-slate-400 hover:bg-slate-50 hover:text-slate-600',
        ]"
        @click="changeLevel(tab.value)"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- SECTION 2 — Hướng dẫn xét minh chứng -->
    <section class="mt-12">
      <div class="mb-4 flex items-center justify-between">
        <h2 class="text-lg font-black text-slate-800 flex items-center gap-2">
          <UIcon
            name="i-heroicons-light-bulb"
            class="text-amber-500"
          />
          Các bước xét minh chứng
        </h2>
      </div>
      <EvidenceGuide @create="openModeSelector(null)" />
    </section>

    <!-- SECTION 3 — Đợt xét minh chứng đang mở (Full width, grid horizontal) -->
    <section class="mt-12 space-y-4">
      <div class="flex items-center justify-between border-b border-slate-100 pb-2">
        <h2 class="text-xl font-black text-slate-800 flex items-center gap-2">
          <UIcon
            name="i-heroicons-megaphone"
            class="text-blue-600"
          />
          Đợt xét minh chứng đang mở
        </h2>
        <span class="text-xs font-bold text-slate-400">Cấp: {{ activeLevelLabel }}</span>
      </div>

      <!-- Campaigns List -->
      <div
        v-if="isLoading"
        class="grid grid-cols-1 gap-4"
      >
        <div
          v-for="i in 2"
          :key="i"
          class="sv-card p-6 space-y-4 animate-pulse"
        >
          <div class="h-6 bg-slate-200 rounded w-3/4" />
          <div class="h-4 bg-slate-200 rounded w-1/2" />
        </div>
      </div>

      <div
        v-else-if="filteredCampaigns.length === 0"
        class="sv-card p-8 text-center text-slate-500"
      >
        <UIcon
          name="i-heroicons-inbox"
          class="size-10 text-slate-300 mx-auto"
        />
        <p class="mt-2 text-sm font-bold">
          Chưa có đợt xét minh chứng nào đang mở ở cấp này
        </p>
        <p class="text-xs text-slate-400 mt-1">
          Vui lòng quay lại sau hoặc liên hệ Văn phòng Hội Sinh viên.
        </p>
      </div>

      <div
        v-else
        class="grid grid-cols-1 gap-4"
      >
        <CampaignCard
          v-for="camp in filteredCampaigns"
          :key="camp.publicId"
          :campaign="camp"
        />
      </div>
    </section>

    <!-- SECTION 4 — Hồ sơ đang làm dở -->
    <section class="mt-12 space-y-4">
      <div class="flex items-center justify-between border-b border-slate-100 pb-2">
        <h2 class="text-xl font-black text-slate-800 flex items-center gap-2">
          <UIcon
            name="i-heroicons-folder-open"
            class="text-[#075EA8]"
          />
          Hồ sơ đang làm dở
        </h2>
        <span class="text-xs font-bold text-slate-400">Cấp: {{ activeLevelLabel }}</span>
      </div>

      <!-- Draft records -->
      <div>
        <!-- Loading State -->
        <div
          v-if="isLoading"
          class="grid grid-cols-1 gap-4"
        >
          <div
            v-for="i in 2"
            :key="i"
            class="sv-card p-5 space-y-3 animate-pulse"
          >
            <div class="h-5 bg-slate-200 rounded w-2/3" />
            <div class="h-4 bg-slate-100 rounded w-1/3" />
          </div>
        </div>

        <!-- Empty state or list -->
        <div v-else-if="allDraftRecords.length === 0">
          <EmptyApplication @create="openModeSelector(null)" />
        </div>

        <div
          v-else
          class="grid grid-cols-1 gap-4"
        >
          <DraftCard
            v-for="rec in allDraftRecords"
            :key="rec.publicId"
            :record="rec"
            @continue="continueRecord"
            @delete="handleDeleteRecord"
          />
        </div>
      </div>

    </section>

    <!-- Mode Selector Pop-up Modal -->
    <ModeSelector
      :open="isModeSelectorOpen"
      @update:open="isModeSelectorOpen = $event"
      @confirm="onModeSelected"
    />

  </main>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import type { CampaignLevel, CampaignSummaryResponse, ApplicationRecordResponse } from '~/types/applicationRecord'
import { isCampaignMatchLevel } from '~/utils/formatters'
import { useApplicationRecord } from '~/composables/useApplicationRecord'
import EvidenceGuide from '~/components/application/EvidenceGuide.vue'
import CampaignCard from '~/components/application/CampaignCard.vue'
import DraftCard from '~/components/application/DraftCard.vue'
import EmptyApplication from '~/components/application/EmptyApplication.vue'
import ModeSelector from '~/components/application/ModeSelector.vue'

definePageMeta({
  layout: 'default',
})

const {
  draft,
  rejected,
  isLoading,
  campaigns,
  fetchRecords,
  createRecord,
  deleteRecord,
} = useApplicationRecord()

// Active level state (UNIVERSITY, CITY, NATION)
const activeLevel = ref<CampaignLevel>('UNIVERSITY')
const isModeSelectorOpen = ref(false)
const selectedCampaignForCreation = ref<CampaignSummaryResponse | null>(null)

const levelTabs = [
  { label: 'Cấp trường', value: 'UNIVERSITY' as CampaignLevel },
  { label: 'Cấp thành phố', value: 'CITY' as CampaignLevel },
  { label: 'Cấp trung ương', value: 'NATION' as CampaignLevel },
]

const activeLevelLabel = computed(() => {
  const match = levelTabs.find(t => t.value === activeLevel.value)
  return match ? match.label : 'Cấp trường'
})

// Filter campaigns based on current active tab
const filteredCampaigns = computed(() => {
  return campaigns.value.filter(c => isCampaignMatchLevel(c.level, activeLevel.value))
})

// Combine draft and other pending records as "Hồ sơ đang làm dở" filtered by active level
const allDraftRecords = computed(() => {
  const records = [...draft.value, ...rejected.value]
  return records.filter(r => r.level === activeLevel.value)
})

const changeLevel = (level: CampaignLevel) => {
  activeLevel.value = level
}

// Open mode selector modal
const openModeSelector = (campaign: CampaignSummaryResponse | null) => {
  selectedCampaignForCreation.value = campaign
  isModeSelectorOpen.value = true
}

// Confirm mode (individual vs group)
const onModeSelected = async (mode: 'individual' | 'group') => {
  // If no campaign selected, pick the first open campaign of the active level
  let targetCampaign = selectedCampaignForCreation.value
  if (!targetCampaign) {
    targetCampaign = filteredCampaigns.value.find(c => c.status === 'ACTIVE') || null
  }

  if (!targetCampaign) {
    useToast().add({
      title: 'Không thể tạo hồ sơ',
      description: 'Chưa có chiến dịch xét chọn đang mở ở cấp này.',
      color: 'error',
    })
    return
  }

  try {
    const isGroup = mode === 'group'
    const newRecord = await createRecord(targetCampaign.publicId, isGroup, activeLevel.value)

    // Redirect to create page with query details
    await navigateTo({
      path: '/application-records/create',
      query: {
        recordId: newRecord.publicId,
        campaignId: targetCampaign.publicId,
        mode,
        level: activeLevel.value,
      },
    })
  }
  catch {
    useToast().add({
      title: 'Tạo hồ sơ thất bại',
      color: 'error',
    })
  }
}

// Continue editing draft record
const continueRecord = async (record: ApplicationRecordResponse) => {
  await navigateTo({
    path: '/application-records/create',
    query: {
      recordId: record.publicId,
      campaignId: record.campaignPublicId,
      mode: record.isGroup ? 'group' : 'individual',
      level: record.level,
    },
  })
}

// Delete draft record
const handleDeleteRecord = async (publicId: string) => {
  await deleteRecord(publicId)
  await fetchRecords()
}

onMounted(async () => {
  await fetchRecords()
})
</script>
