<template>
  <main class="min-h-screen bg-[#F8FAFC] pb-12">
    <section class="mb-5 flex flex-col justify-between gap-4 border-b border-slate-100 pb-5 md:flex-row md:items-center">
      <div>
        <div class="flex items-center gap-2 text-sm font-bold text-slate-400">
          <NuxtLink
            to="/submitted-records"
            class="transition-colors hover:text-[#075EA8]"
          >
            Hồ sơ đã nộp
          </NuxtLink>
          <UIcon
            name="i-heroicons-chevron-right"
            class="size-3.5"
          />
          <span class="text-slate-600">Minh chứng đã nộp</span>
        </div>
        <h1 class="mt-2 flex items-center gap-2 text-2xl font-black text-slate-800">
          <UIcon
            name="i-lucide-eye"
            class="text-[#075EA8]"
          />
          Xem hồ sơ minh chứng
        </h1>
        <p class="mt-1 text-xs font-semibold text-slate-500">
          {{ application?.campaignName || 'Đợt xét chọn Sinh viên 5 Tốt' }}
        </p>
      </div>

      <div class="flex items-center gap-2 self-start md:self-center">
        <UBadge
          v-if="application"
          :color="statusMeta(application.status).color"
          variant="subtle"
          class="rounded-full px-2.5 py-0.5 text-xs font-bold"
        >
          {{ statusMeta(application.status).label }}
        </UBadge>
        <UButton
          to="/submitted-records"
          color="neutral"
          variant="outline"
          icon="i-lucide-arrow-left"
          label="Quay lại"
          class="rounded-xl font-bold"
        />
      </div>
    </section>

    <UserEvidenceDetailContent
      :application="application"
      :tree="criteriaTree"
      :loading="loading"
      :error="error"
    />
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import type { ApplicationRecordResponse, CampaignLevel, StandardDTO } from '~/types/applicationRecord'
import { useApplicationRecordApi } from '~/composables/useApplicationRecord'
import { getErrorMessage } from '~/utils/errors'
import UserEvidenceDetailContent from '~/components/application/UserEvidenceDetailContent.vue'

definePageMeta({
  layout: 'default',
})

const route = useRoute()
const api = useApplicationRecordApi()

const application = ref<ApplicationRecordResponse | null>(null)
const criteriaTree = ref<StandardDTO[]>([])
const loading = ref(true)
const error = ref('')

const recordPublicId = computed(() => String(route.params.recordPublicId || ''))
const campaignPublicId = computed(() => String(route.query.campaignId || ''))
const isGroup = computed(() => route.query.mode === 'group' || route.query.isGroup === 'true')
const level = computed(() => {
  const rawLevel = String(route.query.level || '')
  return rawLevel ? rawLevel as CampaignLevel : undefined
})

const loadEvidenceDetail = async () => {
  loading.value = true
  error.value = ''

  if (!campaignPublicId.value) {
    error.value = 'Thiếu thông tin đợt xét chọn để tải hồ sơ minh chứng.'
    loading.value = false
    return
  }

  try {
    const [recordResponse, treeResponse] = await Promise.all([
      api.getMyApplicationRecord(campaignPublicId.value, isGroup.value, level.value),
      api.getCampaignCriteriaTree(campaignPublicId.value, isGroup.value, level.value),
    ])

    const record = recordResponse.data
    if (!record || record.publicId !== recordPublicId.value) {
      throw new Error('Không tìm thấy hồ sơ minh chứng phù hợp.')
    }

    application.value = record
    criteriaTree.value = treeResponse.data
  }
  catch (cause) {
    application.value = null
    criteriaTree.value = []
    error.value = getErrorMessage(cause, 'Không thể tải hồ sơ minh chứng.')
  }
  finally {
    loading.value = false
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

onMounted(loadEvidenceDetail)
</script>
