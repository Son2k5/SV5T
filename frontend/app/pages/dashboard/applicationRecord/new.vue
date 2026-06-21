<script setup lang="ts">
import type { CampaignLevel, CampaignSummaryResponse } from '~/types/applicationRecord'
import { getErrorMessage, getErrorStatus } from '~/utils/errors'

type EvidenceMode = 'individual' | 'group'

const levelOptions: Array<{ label: string, value: CampaignLevel }> = [
  { label: 'Cấp trường', value: 'UNIVERSITY' },
  { label: 'Cấp thành phố', value: 'CITY' },
  { label: 'Cấp trung ương', value: 'NATION' },
]

const modeOptions: Array<{
  label: string
  value: EvidenceMode
  desc: string
  icon: string
  accent: string
}> = [
  {
    label: 'Danh hiệu cá nhân',
    value: 'individual',
    desc: 'Xét danh hiệu cho từng sinh viên theo bộ tiêu chí cá nhân.',
    icon: 'i-heroicons-user-solid',
    accent: 'text-blue-600 bg-blue-50 ring-blue-100',
  },
  {
    label: 'Danh hiệu tập thể',
    value: 'group',
    desc: 'Xét danh hiệu cho lớp, câu lạc bộ hoặc đơn vị trực thuộc.',
    icon: 'i-heroicons-users-solid',
    accent: 'text-emerald-600 bg-emerald-50 ring-emerald-100',
  },
]

const selectedLevel = ref<CampaignLevel>('UNIVERSITY')
const selectedMode = ref<EvidenceMode>('individual')
const currentCampaign = ref<CampaignSummaryResponse | null>(null)
const loadingCampaign = ref(false)
const starting = ref(false)
const error = ref('')

const {
  createApplicationRecord,
  getCurrentCampaign,
  getMyApplicationRecord,
} = useApplicationRecordApi()

const selectedLevelLabel = computed(() =>
  levelOptions.find(option => option.value === selectedLevel.value)?.label ?? 'Cấp xét chọn',
)

const selectedModeLabel = computed(() =>
  modeOptions.find(option => option.value === selectedMode.value)?.label ?? 'Chế độ minh chứng',
)

async function loadCurrentCampaign() {
  error.value = ''
  loadingCampaign.value = true
  currentCampaign.value = null

  try {
    const res = await getCurrentCampaign(selectedLevel.value)
    currentCampaign.value = res.data
  }
  catch (errorResponse) {
    error.value = getErrorMessage(errorResponse, 'Chưa có đợt xét chọn đang mở cho cấp này.')
  }
  finally {
    loadingCampaign.value = false
  }
}

async function openEvidenceWorkspace() {
  error.value = ''

  if (!currentCampaign.value) {
    await loadCurrentCampaign()
  }

  if (!currentCampaign.value) {
    return
  }

  const campaignPublicId = currentCampaign.value.publicId

  try {
    starting.value = true

    const res = await createApplicationRecord({
      campaignPublicId,
      note: `${selectedModeLabel.value} - ${selectedLevelLabel.value}`,
    })

    await navigateTo({
      path: `/dashboard/applicationRecord/${res.data.campaignPublicId}/evidences`,
      query: {
        mode: selectedMode.value,
        level: selectedLevel.value,
      },
    })
  }
  catch (errorResponse) {
    const status = getErrorStatus(errorResponse)
    const message = getErrorMessage(errorResponse, '')

    if (status === 409 || message.includes('đã đăng ký') || message.includes('Ä‘Ã£ Ä‘Äƒng kÃ½')) {
      const res = await getMyApplicationRecord(campaignPublicId)
      await navigateTo({
        path: `/dashboard/applicationRecord/${res.data.campaignPublicId}/evidences`,
        query: {
          mode: selectedMode.value,
          level: selectedLevel.value,
        },
      })
      return
    }

    error.value = message || 'Không thể mở không gian nộp minh chứng.'
  }
  finally {
    starting.value = false
  }
}

watch(selectedLevel, () => {
  loadCurrentCampaign()
})

onMounted(() => {
  loadCurrentCampaign()
})
</script>

<template>
  <main class="min-h-screen bg-[#F8FAFC]">
    <section class="overflow-hidden rounded-lg bg-gradient-to-br from-[#5B5FEF] via-[#5F8EE8] to-[#5BB9DD] px-4 py-16 text-center text-white md:px-8">
      <p class="text-sm font-black uppercase tracking-[0.16em] text-white/80">
        Hội Sinh viên Trường Đại học Hà Nội
      </p>

      <h1 class="mx-auto mt-5 max-w-4xl text-4xl font-black leading-tight md:text-5xl">
        Xét chọn danh hiệu Sinh viên 5 Tốt
      </h1>

      <p class="mx-auto mt-4 max-w-2xl text-base font-medium text-white/85">
        Chọn cấp xét chọn và chế độ minh chứng. Đợt xét chọn do admin mở sẽ được hệ thống tự lấy, sinh viên chỉ cần chọn tiêu chí và nộp minh chứng.
      </p>

      <div class="mx-auto mt-8 grid max-w-xl grid-cols-1 gap-2 rounded-lg border border-white/20 bg-white p-2 text-[#1E293B] shadow-xl md:grid-cols-3">
        <button
          v-for="option in levelOptions"
          :key="option.value"
          type="button"
          :class="[
            'h-11 rounded-md px-4 text-sm font-bold transition',
            selectedLevel === option.value ? 'bg-[#075EA8] text-white shadow-sm' : 'text-slate-500 hover:bg-slate-50',
          ]"
          @click="selectedLevel = option.value"
        >
          {{ option.label }}
        </button>
      </div>
    </section>

    <section class="mx-auto max-w-6xl px-4 py-10 md:px-6">
      <div class="mb-8 text-center">
        <h2 class="text-3xl font-black text-[#1E293B]">
          Chế độ minh chứng
        </h2>
        <p class="mt-2 text-sm text-[#64748B]">
          Chọn loại danh hiệu trước khi vào bộ tiêu chí.
        </p>
      </div>

      <div class="grid gap-5 md:grid-cols-2">
        <button
          v-for="option in modeOptions"
          :key="option.value"
          type="button"
          :class="[
            'group rounded-lg border bg-white p-6 text-left shadow-sm transition hover:-translate-y-0.5 hover:shadow-lg',
            selectedMode === option.value ? 'border-blue-300 ring-2 ring-blue-100' : 'border-[#E5E7EB]',
          ]"
          @click="selectedMode = option.value"
        >
          <div :class="['flex size-16 items-center justify-center rounded-full ring-1', option.accent]">
            <UIcon
              :name="option.icon"
              class="size-8"
            />
          </div>

          <h3 class="mt-5 text-2xl font-black text-[#1E293B]">
            {{ option.label }}
          </h3>
          <p class="mt-2 text-sm leading-6 text-[#64748B]">
            {{ option.desc }}
          </p>
        </button>
      </div>

      <div class="mt-8 rounded-lg border border-[#E5E7EB] bg-white p-5 shadow-sm">
        <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div>
            <p class="text-sm font-bold uppercase tracking-[0.14em] text-[#64748B]">
              Đợt xét chọn đang mở
            </p>
            <h3 class="mt-1 text-xl font-black text-[#1E293B]">
              <span v-if="loadingCampaign">Đang tải đợt xét chọn...</span>
              <span v-else-if="currentCampaign">{{ currentCampaign.name }}</span>
              <span v-else>Chưa có đợt xét chọn</span>
            </h3>
            <p v-if="currentCampaign" class="mt-1 text-sm text-[#64748B]">
              Năm học {{ currentCampaign.academicYear }} · {{ selectedLevelLabel }} · {{ selectedModeLabel }}
            </p>
          </div>

          <button
            type="button"
            :disabled="loadingCampaign || starting || !currentCampaign"
            class="inline-flex h-12 items-center justify-center rounded-md bg-[#075EA8] px-6 text-sm font-black text-white shadow-sm transition hover:bg-[#064f8d] disabled:cursor-not-allowed disabled:opacity-60"
            @click="openEvidenceWorkspace"
          >
            <UIcon
              :name="starting ? 'i-lucide-loader-circle' : 'i-lucide-arrow-right'"
              :class="['mr-2 size-5', starting && 'animate-spin']"
            />
            {{ starting ? 'Đang mở...' : 'Vào nộp minh chứng' }}
          </button>
        </div>

        <p v-if="error" class="mt-4 rounded-md bg-red-50 p-3 text-sm font-semibold text-red-600">
          {{ error }}
        </p>
      </div>
    </section>
  </main>
</template>
