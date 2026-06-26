<script setup lang="ts">
import type { ApplicationRecordResponse, CampaignLevel, StandardDTO } from '~/types/applicationRecord'
import { getErrorMessage } from '~/utils/errors'

type EvidenceMode = 'individual' | 'group'

const route = useRoute()
const campaignPublicId = String(route.params.campaignPublicId)

const {
  getMyApplicationRecord,
  getCampaignCriteriaTree,
  saveEvidence,
  saveEvidenceFile,
  submitApplicationRecord,
} = useApplicationRecordApi()

const applicationRecord = ref<ApplicationRecordResponse | null>(null)
const standards = ref<StandardDTO[]>([])
const loading = ref(true)
const error = ref('')
const message = ref('')
const savingId = ref<string | null>(null)
const submitting = ref(false)

const mode = computed<EvidenceMode>(() => route.query.mode === 'group' ? 'group' : 'individual')
const level = computed<CampaignLevel>(() => {
  const value = String(route.query.level || applicationRecord.value?.level || 'UNIVERSITY')
  return ['UNIVERSITY', 'CITY', 'NATION', 'UNI_CITY', 'UNI_NATION', 'CITY_NATION', 'ALL'].includes(value) ? value as CampaignLevel : 'UNIVERSITY'
})

const modeLabel = computed(() => mode.value === 'group' ? 'Danh hiệu tập thể' : 'Danh hiệu cá nhân')
const levelLabel = computed(() => {
  switch (level.value) {
    case 'CITY':
      return 'Cấp thành phố'
    case 'NATION':
      return 'Cấp trung ương'
    case 'UNI_CITY':
      return 'Trường & Thành phố'
    case 'UNI_NATION':
      return 'Trường & Trung ương'
    case 'CITY_NATION':
      return 'Thành phố & Trung ương'
    case 'ALL':
      return 'Cả ba cấp'
    default:
      return 'Cấp trường'
  }
})

const isReadonly = computed(() => applicationRecord.value?.status !== 'DRAFT')

async function loadData() {
  error.value = ''
  loading.value = true

  try {
    const [recordRes, criteriaRes] = await Promise.all([
      getMyApplicationRecord(campaignPublicId, mode.value === 'group', level.value),
      getCampaignCriteriaTree(campaignPublicId, mode.value === 'group', level.value),
    ])

    applicationRecord.value = recordRes.data
    standards.value = criteriaRes.data || []
  }
  catch (errorResponse) {
    error.value = getErrorMessage(errorResponse, 'Không thể tải dữ liệu minh chứng.')
  }
  finally {
    loading.value = false
  }
}

async function handleSave(criteriaPublicId: string, evidenceUrl: string) {
  error.value = ''
  message.value = ''

  try {
    savingId.value = criteriaPublicId

    await saveEvidence(campaignPublicId, {
      criteriaPublicId,
      evidenceUrl,
    })

    message.value = 'Lưu minh chứng thành công.'
    await loadData()
  }
  catch (errorResponse) {
    error.value = getErrorMessage(errorResponse, 'Lưu minh chứng thất bại.')
  }
  finally {
    savingId.value = null
  }
}

async function handleSaveFile(criteriaPublicId: string, file: File) {
  error.value = ''
  message.value = ''

  try {
    savingId.value = criteriaPublicId

    await saveEvidenceFile(campaignPublicId, criteriaPublicId, file)

    message.value = 'Upload minh chứng thành công.'
    await loadData()
  }
  catch (errorResponse) {
    error.value = getErrorMessage(errorResponse, 'Upload minh chứng thất bại.')
  }
  finally {
    savingId.value = null
  }
}

async function handleSubmit() {
  error.value = ''
  message.value = ''

  try {
    submitting.value = true

    const res = await submitApplicationRecord(campaignPublicId, mode.value === 'group', level.value)
    applicationRecord.value = res.data
    message.value = 'Nộp hồ sơ minh chứng thành công.'
  }
  catch (errorResponse) {
    error.value = getErrorMessage(errorResponse, 'Nộp hồ sơ thất bại.')
  }
  finally {
    submitting.value = false
  }
}

await loadData()
</script>

<template>
  <main class="min-h-screen bg-[#F8FAFC]">
    <section class="mx-auto max-w-7xl px-4 py-6 md:px-6">
      <div class="rounded-lg bg-gradient-to-r from-[#075EA8] to-[#39A2D7] p-6 text-white shadow-sm md:p-8">
        <div class="flex flex-col gap-5 md:flex-row md:items-center md:justify-between">
          <div>
            <p class="text-sm font-black uppercase tracking-[0.16em] text-white/75">
              {{ modeLabel }} · {{ levelLabel }}
            </p>
            <h1 class="mt-2 text-3xl font-black md:text-4xl">
              Chọn tiêu chí và nộp minh chứng
            </h1>
            <p class="mt-2 text-sm font-medium text-white/85 md:text-base">
              {{ applicationRecord?.campaignName || 'Đợt xét chọn đang mở' }}
            </p>
          </div>

          <ApplicationStatusBadge :status="applicationRecord?.status" />
        </div>
      </div>

      <div class="mt-6 grid gap-3 md:grid-cols-3">
        <div class="rounded-lg border border-[#E5E7EB] bg-white p-4">
          <p class="text-sm font-bold text-[#64748B]">
            1. Chọn chế độ
          </p>
          <p class="mt-1 font-black text-[#1E293B]">
            {{ modeLabel }}
          </p>
        </div>
        <div class="rounded-lg border border-[#E5E7EB] bg-white p-4">
          <p class="text-sm font-bold text-[#64748B]">
            2. Nộp minh chứng
          </p>
          <p class="mt-1 font-black text-[#1E293B]">
            Theo từng tiêu chí
          </p>
        </div>
        <div class="rounded-lg border border-[#E5E7EB] bg-white p-4">
          <p class="text-sm font-bold text-[#64748B]">
            3. Gửi xét duyệt
          </p>
          <p class="mt-1 font-black text-[#1E293B]">
            Hoàn tất hồ sơ
          </p>
        </div>
      </div>

      <div
        v-if="loading"
        class="mt-6 rounded-lg border border-[#E5E7EB] bg-white p-8 text-[#64748B] shadow-sm"
      >
        Đang tải dữ liệu minh chứng...
      </div>

      <div
        v-else-if="error"
        class="mt-6 rounded-lg border border-red-100 bg-red-50 p-5 text-red-600"
      >
        {{ error }}
      </div>

      <div
        v-else-if="!standards.length"
        class="mt-6 rounded-lg border border-[#E5E7EB] bg-white p-8 text-[#64748B] shadow-sm"
      >
        Chưa có tiêu chí nào trong đợt xét chọn này.
      </div>

      <div
        v-else
        class="mt-6 space-y-5"
      >
        <div
          v-for="standard in standards"
          :key="standard.publicId"
          class="rounded-lg border border-[#E5E7EB] bg-white p-5 shadow-sm"
        >
          <div class="mb-5">
            <h2 class="text-xl font-black text-[#1E293B]">
              {{ standard.name }}
            </h2>
            <p
              v-if="standard.description"
              class="mt-1 text-sm leading-6 text-[#64748B]"
            >
              {{ standard.description }}
            </p>
          </div>

          <div class="space-y-4">
            <EvidenceCard
              v-for="criteria in standard.criteriaDTOList"
              :key="criteria.publicId"
              :criteria="criteria"
              :disabled="isReadonly"
              :loading="savingId === criteria.publicId"
              @save="handleSave"
              @save-file="handleSaveFile"
            />
          </div>
        </div>

        <p
          v-if="message"
          class="rounded-lg bg-emerald-50 p-4 text-sm font-black text-emerald-700"
        >
          {{ message }}
        </p>

        <div class="sticky bottom-4 z-10 flex justify-end">
          <button
            v-if="applicationRecord?.status === 'DRAFT'"
            type="button"
            :disabled="submitting"
            class="inline-flex h-12 items-center justify-center rounded-md bg-[#16A34A] px-7 text-sm font-black text-white shadow-lg transition hover:bg-[#15803D] disabled:cursor-not-allowed disabled:opacity-60"
            @click="handleSubmit"
          >
            <UIcon
              :name="submitting ? 'i-lucide-loader-circle' : 'i-lucide-send'"
              :class="['mr-2 size-5', submitting && 'animate-spin']"
            />
            {{ submitting ? 'Đang nộp...' : 'Nộp hồ sơ minh chứng' }}
          </button>
        </div>
      </div>
    </section>
  </main>
</template>
