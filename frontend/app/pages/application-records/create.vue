<template>
  <main class="min-h-screen bg-[#F8FAFC] pb-24">
    <!-- Header Navigation -->
    <section class="mb-6 flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-slate-100 pb-5">
      <div>
        <div class="flex items-center gap-2 text-sm font-bold text-slate-400">
          <NuxtLink
            to="/application-records"
            class="hover:text-[#075EA8] transition-colors"
          >Hồ sơ xét chọn</NuxtLink>
          <UIcon
            name="i-heroicons-chevron-right"
            class="size-3.5"
          />
          <span class="text-slate-600">Khai báo hồ sơ</span>
        </div>
        <h1 class="mt-2 text-2xl font-black text-slate-800 flex items-center gap-2">
          <UIcon
            name="i-heroicons-clipboard-document-list"
            class="text-[#075EA8]"
          />
          Khai báo minh chứng Sinh viên 5 Tốt
        </h1>
        <p class="text-xs text-slate-500 font-semibold mt-1">
          Chế độ: <span class="text-slate-800 font-bold">{{ isGroup ? 'Tập thể' : 'Cá nhân' }}</span> ·
          Chiến dịch: <span class="text-[#075EA8] font-bold">{{ activeCampaignName }}</span>
        </p>
      </div>

      <!-- Level Tabs inside editor -->
      <div class="flex items-center gap-1 rounded-xl bg-slate-100 p-1 self-start md:self-center">
        <button
          v-for="tab in levelTabs"
          :key="tab.value"
          type="button"
          :class="[
            'px-4 h-9 rounded-lg text-xs font-black uppercase tracking-wider transition-all cursor-pointer',
            activeLevel === tab.value
              ? 'bg-[#075EA8] text-white shadow-sm'
              : 'text-slate-500 hover:text-slate-800 hover:bg-slate-200/50',
          ]"
          @click="changeLevel(tab.value)"
        >
          {{ tab.label }}
        </button>
      </div>
    </section>

    <!-- Main Content Area -->
    <div
      v-if="isLoading"
      class="space-y-6"
    >
      <div
        v-for="i in 3"
        :key="i"
        class="sv-card p-6 space-y-4 animate-pulse"
      >
        <div class="h-6 bg-slate-200 rounded w-1/4" />
        <div class="h-16 bg-slate-100 rounded-xl" />
        <div class="h-20 bg-slate-50 rounded-xl" />
      </div>
    </div>

    <div v-else-if="!activeCriteriaTree || activeCriteriaTree.length === 0">
      <!-- Empty Criteria State -->
      <div class="sv-card p-12 text-center text-slate-500 bg-white">
        <div class="flex size-16 items-center justify-center rounded-full bg-slate-50 border border-slate-100/50 mx-auto mb-4 text-slate-400">
          <UIcon
            name="i-heroicons-clipboard"
            class="size-8"
          />
        </div>
        <h3 class="text-lg font-black text-slate-800">
          Không có tiêu chí xét duyệt
        </h3>
        <p class="text-sm text-slate-400 mt-2 max-w-sm mx-auto">
          Chiến dịch xét chọn danh hiệu ở cấp này chưa được công bố hoặc không hỗ trợ các tiêu chí đánh giá cho nhóm của bạn.
        </p>
        <div class="mt-6">
          <UButton
            label="Quay lại danh sách"
            icon="i-heroicons-arrow-left"
            variant="outline"
            class="cursor-pointer font-bold px-4 rounded-xl border border-slate-200 text-slate-700 hover:bg-slate-50"
            @click="cancelEditing"
          />
        </div>
      </div>
    </div>

    <div
      v-else
      class="space-y-6"
    >
      <div class="bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-100/50 rounded-2xl p-4 flex gap-3 items-start">
        <UIcon
          name="i-heroicons-light-bulb"
          class="size-5 text-[#075EA8] shrink-0 mt-0.5"
        />
        <div class="text-xs font-semibold text-slate-600 leading-relaxed">
          <p class="font-extrabold text-[#075EA8]">
            Hướng dẫn kê khai:
          </p>
          <p class="mt-1">
            Hãy nhấp vào từng mục tiêu chuẩn bên dưới để mở rộng các tiêu chí con. Điền đầy đủ thông tin rèn luyện, tải tệp tin minh chứng (PDF, ảnh hoặc DOCX) và nhấn <span class="font-bold">"Lưu tiêu chí"</span> ở mỗi phần. Sau khi hoàn thành toàn bộ, bấm <span class="font-bold text-[#075EA8]">"Lưu và nộp"</span> ở thanh điều hướng chân trang.
          </p>
        </div>
      </div>

      <!-- Standards Navigation Tabs (Visily style) -->
      <div class="bg-white border border-slate-100 rounded-2xl p-2 flex flex-wrap items-center justify-between gap-1 shadow-[0_8px_30px_rgb(0,0,0,0.03)]">
        <button
          v-for="(tab, idx) in standardTabs"
          :key="idx"
          type="button"
          :class="[
            'flex items-center justify-center gap-2 px-5 py-3 rounded-xl text-xs font-black transition-all duration-300 cursor-pointer select-none flex-1 min-w-[140px] text-center border-0 focus:outline-none',
            activeStandardIndex === idx
              ? 'bg-[#075EA8] text-white shadow-md shadow-blue-500/10 scale-[1.02]'
              : 'text-slate-500 hover:text-slate-800 hover:bg-slate-50',
          ]"
          @click="activeStandardIndex = idx"
        >
          <UIcon
            :name="tab.icon"
            :class="['size-4 shrink-0', activeStandardIndex === idx ? 'text-white' : 'text-slate-400']"
          />
          <span>{{ tab.label }}</span>
        </button>
      </div>

      <!-- Criteria Accordion Tree (Only show selected standard) -->
      <CriteriaAccordion
        v-if="currentStandards.length > 0"
        ref="accordionRef"
        :standards="currentStandards"
        :disabled="isReadonly"
        :level="activeLevel"
        @save="onCriteriaSaved"
      />

      <div
        v-else
        class="sv-card p-12 text-center text-slate-500 bg-white border border-slate-100 rounded-2xl shadow-[0_8px_30px_rgb(0,0,0,0.03)]"
      >
        <div class="flex size-14 items-center justify-center rounded-full bg-slate-50 border border-slate-100/50 mx-auto mb-4 text-slate-400">
          <UIcon
            name="i-heroicons-inbox"
            class="size-7"
          />
        </div>
        <h3 class="text-sm font-bold text-slate-800">
          Không có dữ liệu tiêu chí
        </h3>
        <p class="text-xs text-slate-400 mt-1 max-w-xs mx-auto font-medium">
          Tiêu chuẩn này không có yêu cầu khai báo minh chứng trong đợt xét chọn hiện tại.
        </p>
      </div>
    </div>

    <!-- Sticky footer actions bar -->
    <ApplicationFooter
      :is-saving="isSaving"
      :is-submitting="isSubmitting"
      @save="saveDraft"
      @cancel="cancelEditing"
      @submit="submitRecordToServer"
    />
  </main>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import type { CampaignLevel } from '~/types/applicationRecord'
import { isCampaignMatchLevel } from '~/utils/formatters'
import { useApplicationRecord } from '~/composables/useApplicationRecord'
import CriteriaAccordion from '~/components/application/CriteriaAccordion.vue'
import ApplicationFooter from '~/components/application/ApplicationFooter.vue'

definePageMeta({
  layout: 'default',
})

const route = useRoute()
const toast = useToast()

const {
  isLoading,
  isSaving,
  isSubmitting,
  activeCriteriaTree,
  campaigns,
  fetchCriteriaTree,
  submitRecord,
  draft,
  rejected,
  fetchRecords,
  createRecord,
} = useApplicationRecord()

// Route state parameters
const recordId = ref<string>(String(route.query.recordId || ''))
const isGroup = computed(() => route.query.mode === 'group')
const activeLevel = ref<CampaignLevel>((route.query.level as CampaignLevel) || 'UNIVERSITY')
const campaignId = ref<string>(String(route.query.campaignId || ''))

const levelTabs = [
  { label: 'Cấp trường', value: 'UNIVERSITY' as CampaignLevel },
  { label: 'Cấp thành phố', value: 'CITY' as CampaignLevel },
  { label: 'Cấp trung ương', value: 'NATION' as CampaignLevel },
]

// Standards tabs definition (Visily mockup alignment)
const activeStandardIndex = ref(0)

const standardTabs = [
  { label: 'Đạo đức tốt', icon: 'i-heroicons-heart' },
  { label: 'Học tập tốt', icon: 'i-heroicons-book-open' },
  { label: 'Thể lực tốt', icon: 'i-heroicons-bolt' },
  { label: 'Tình nguyện tốt', icon: 'i-heroicons-users' },
  { label: 'Hội nhập tốt', icon: 'i-heroicons-globe-alt' },
]

const currentStandards = computed(() => {
  if (!activeCriteriaTree.value || activeCriteriaTree.value.length === 0) return []
  const activeTab = standardTabs[activeStandardIndex.value]
  if (!activeTab) return []

  return activeCriteriaTree.value.filter((std) => {
    const stdName = std.name.toLowerCase()
    const label = activeTab.label.toLowerCase()
    if (label.includes('đạo đức')) return stdName.includes('đạo đức') || stdName.includes('dao duc')
    if (label.includes('học tập')) return stdName.includes('học tập') || stdName.includes('hoc tap')
    if (label.includes('thể lực')) return stdName.includes('thể lực') || stdName.includes('the luc')
    if (label.includes('tình nguyện')) return stdName.includes('tình nguyện') || stdName.includes('tinh nguyen')
    if (label.includes('hội nhập')) return stdName.includes('hội nhập') || stdName.includes('hoi nhap')
    return stdName.includes(label)
  })
})

// Find record to check read-only states (if record status is SUBMITTED or APPROVED, it cannot be edited)
const isReadonly = computed(() => {
  // Check if this record is in drafts or rejected lists. If not found in those, it's submitted or approved (Readonly)
  if (!recordId.value) return false
  const editableRecord = [...draft.value, ...rejected.value].find(r => r.publicId === recordId.value)
  return !editableRecord
})

const activeCampaignName = computed(() => {
  const activeCamp = campaigns.value.find(c => isCampaignMatchLevel(c.level, activeLevel.value))
  return activeCamp ? activeCamp.name : 'Đợt xét minh chứng mới'
})

// Load criteria tree for active campaign
const loadCriteriaData = async () => {
  const camp = campaigns.value.find(c => isCampaignMatchLevel(c.level, activeLevel.value))
  let targetCampaignId = camp ? camp.publicId : ''

  if (!targetCampaignId) {
    targetCampaignId = campaignId.value
  }

  if (targetCampaignId) {
    await fetchCriteriaTree(targetCampaignId, isGroup.value, activeLevel.value)
  }
  else {
    activeCriteriaTree.value = []
  }
}

const changeLevel = async (level: CampaignLevel) => {
  activeLevel.value = level

  // Find active campaign and existing draft/rejected record for this level
  const camp = campaigns.value.find(c => isCampaignMatchLevel(c.level, level))
  const records = [...draft.value, ...rejected.value]
  const matchingRecord = camp ? records.find(r => r.campaignPublicId === camp.publicId && r.level === level) : null

  let newRecordId = matchingRecord ? matchingRecord.publicId : ''
  if (!newRecordId && camp) {
    try {
      const isGrp = route.query.mode === 'group'
      const newRec = await createRecord(camp.publicId, isGrp, level)
      if (newRec) {
        newRecordId = newRec.publicId
        await fetchRecords()
      }
    }
    catch (err) {
      console.error('Failed to auto-create record for level', level, err)
    }
  }

  const newCampaignId = camp ? camp.publicId : ''

  campaignId.value = newCampaignId
  recordId.value = newRecordId

  // Update query parameters without reloading page
  await navigateTo({
    path: '/application-records/create',
    query: {
      ...route.query,
      recordId: newRecordId,
      campaignId: newCampaignId,
      level,
    },
  })

  // Reset active standard tab to first standard
  activeStandardIndex.value = 0
  await loadCriteriaData()
}

// Emitted when a single criteria card saves
const onCriteriaSaved = () => {
  // Can perform post-save actions here if needed
}

const accordionRef = ref<{ saveAll: () => Promise<void> } | null>(null)

// Save overall draft
const saveDraft = async () => {
  isSaving.value = true
  try {
    if (accordionRef.value) {
      await accordionRef.value.saveAll()
    }
    toast.add({
      title: 'Lưu hồ sơ thành công',
      description: 'Hồ sơ nháp đã được lưu trữ trên hệ thống.',
      color: 'success',
    })
  }
  catch (e) {
    console.error(e)
    toast.add({
      title: 'Lưu hồ sơ thất bại',
      description: 'Đã xảy ra lỗi khi lưu trữ hồ sơ.',
      color: 'error',
    })
  }
  finally {
    isSaving.value = false
  }
}

// Exit page
const cancelEditing = async () => {
  await navigateTo('/application-records')
}

// Submit records
const submitRecordToServer = async () => {
  if (!recordId.value) {
    toast.add({
      title: 'Lỗi nộp hồ sơ',
      description: 'Không tìm thấy ID hồ sơ để nộp.',
      color: 'error',
    })
    return
  }

  try {
    await submitRecord(recordId.value)
    await navigateTo('/application-records')
  }
  catch {
    toast.add({
      title: 'Nộp hồ sơ thất bại',
      color: 'error',
    })
  }
}

onMounted(async () => {
  await fetchRecords()

  const queryCampaignId = String(route.query.campaignId || '')
  const queryLevel = (route.query.level as CampaignLevel) || 'UNIVERSITY'
  const isGrp = route.query.mode === 'group'

  if (queryCampaignId && !route.query.recordId) {
    const records = [...draft.value, ...rejected.value]
    const matchingRecord = records.find(r => r.campaignPublicId === queryCampaignId && r.level === queryLevel && r.isGroup === isGrp)
    if (matchingRecord) {
      recordId.value = matchingRecord.publicId
      await navigateTo({
        path: '/application-records/create',
        query: {
          ...route.query,
          recordId: matchingRecord.publicId,
        },
      })
    }
    else {
      try {
        const newRec = await createRecord(queryCampaignId, isGrp, queryLevel)
        if (newRec) {
          recordId.value = newRec.publicId
          await fetchRecords()
          await navigateTo({
            path: '/application-records/create',
            query: {
              ...route.query,
              recordId: newRec.publicId,
            },
          })
        }
      }
      catch (err) {
        console.error('Failed to auto-create record on mount', err)
      }
    }
  }

  await loadCriteriaData()
})
</script>
