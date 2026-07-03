<template>
  <div>
    <div
      v-if="loading"
      class="flex min-h-[450px] flex-col items-center justify-center py-20"
    >
      <UIcon
        name="i-lucide-loader-2"
        class="size-10 animate-spin text-blue-600"
      />
      <p class="mt-3 text-sm font-medium text-[#64748B]">
        Đang tải hồ sơ minh chứng...
      </p>
    </div>

    <div
      v-else-if="error"
      class="flex min-h-[300px] flex-col items-center justify-center rounded-2xl border border-rose-100 bg-white py-12 text-center"
    >
      <UIcon
        name="i-lucide-alert-triangle"
        class="mb-2 size-12 text-rose-500"
      />
      <p class="text-sm font-semibold text-rose-600">
        {{ error }}
      </p>
    </div>

    <div
      v-else-if="!application"
      class="flex min-h-[300px] flex-col items-center justify-center rounded-2xl border border-slate-100 bg-white py-12 text-center"
    >
      <UIcon
        name="i-lucide-file-question"
        class="mb-2 size-12 text-slate-300"
      />
      <p class="text-sm font-semibold text-slate-500">
        Không tìm thấy thông tin hồ sơ minh chứng.
      </p>
    </div>

    <div
      v-else
      class="space-y-4"
    >
      <div class="rounded-xl border border-slate-100 bg-slate-50 p-4 shadow-2xs">
        <div class="flex flex-col justify-between gap-4 md:flex-row md:items-center">
          <div class="flex flex-wrap items-center gap-x-6 gap-y-2">
            <div>
              <p class="text-[10px] font-bold uppercase tracking-wider text-slate-400">
                Họ và tên
              </p>
              <p class="mt-0.5 text-sm font-bold text-[#1E293B]">
                {{ profile?.fullName || 'Chưa cập nhật' }}
              </p>
            </div>
            <div class="hidden h-8 w-px bg-slate-200 md:block" />
            <div>
              <p class="text-[10px] font-bold uppercase tracking-wider text-slate-400">
                Mã số sinh viên
              </p>
              <p class="mt-0.5 font-mono text-sm font-semibold text-[#1E293B]">
                {{ profile?.studentCode || 'N/A' }}
              </p>
            </div>
            <div class="hidden h-8 w-px bg-slate-200 md:block" />
            <div>
              <p class="text-[10px] font-bold uppercase tracking-wider text-slate-400">
                Đợt xét chọn
              </p>
              <p class="mt-0.5 max-w-[240px] truncate text-xs font-semibold text-[#334155]">
                {{ application.campaignName }}
              </p>
            </div>
            <div class="hidden h-8 w-px bg-slate-200 md:block" />
            <div>
              <p class="text-[10px] font-bold uppercase tracking-wider text-slate-400">
                Cấp hồ sơ
              </p>
              <p class="mt-0.5 text-xs font-bold text-[#334155]">
                {{ getLevelLabel(application.level) }}
              </p>
            </div>
          </div>

          <div class="flex flex-wrap gap-2 border-t border-slate-200/60 pt-2 md:border-t-0 md:pt-0">
            <span class="inline-flex items-center gap-1 rounded-full bg-slate-100 px-2 py-0.5 text-[11px] font-bold text-slate-600">
              Tiêu chí: {{ stats.total }}
            </span>
            <span class="inline-flex items-center gap-1 rounded-full border border-emerald-100 bg-emerald-50 px-2 py-0.5 text-[11px] font-bold text-emerald-700">
              Đạt: {{ stats.approved }}
            </span>
            <span class="inline-flex items-center gap-1 rounded-full border border-rose-100 bg-rose-50 px-2 py-0.5 text-[11px] font-bold text-rose-700">
              Lỗi: {{ stats.rejected }}
            </span>
            <span class="inline-flex items-center gap-1 rounded-full border border-amber-100 bg-amber-50 px-2 py-0.5 text-[11px] font-bold text-amber-700">
              Chờ duyệt: {{ stats.pending }}
            </span>
          </div>
        </div>
      </div>

      <div class="flex min-h-[620px] flex-col overflow-hidden rounded-xl border border-slate-200 bg-white shadow-2xs md:h-[calc(100vh-260px)] md:flex-row">
        <div class="flex h-full min-h-0 w-full flex-col border-b border-slate-200 md:w-[35%] md:border-b-0 md:border-r">
          <AdminEvidenceCriteriaTree
            :tree="tree"
            :selected-criteria-public-id="selectedCriteria?.publicId"
            @select-criteria="handleSelectCriteria"
          />
        </div>

        <div class="flex h-full min-h-0 w-full flex-col bg-white md:w-[65%]">
          <AdminEvidenceDetailPanel
            :criteria="selectedCriteria"
            :standard="selectedStandard"
            readonly
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import type { ApplicationRecordResponse, CriteriaDTO, StandardDTO } from '~/types/applicationRecord'
import { useCurrentUser } from '~/composables/profile/useCurrentUser'

const props = defineProps<{
  application: ApplicationRecordResponse | null
  tree: StandardDTO[]
  loading?: boolean
  error?: string
}>()

const profile = ref<any>(null)
const selectedCriteria = ref<any | null>(null)
const selectedStandard = ref<any | null>(null)

onMounted(async () => {
  try {
    const currentUser = await useCurrentUser()
    if (currentUser?.profile) {
      profile.value = currentUser.profile.value
    }
  }
  catch (err) {
    console.error('Failed to load user profile in evidence detail view', err)
  }
})

watch(
  () => props.application?.publicId,
  () => {
    selectedCriteria.value = null
    selectedStandard.value = null
    selectFirstEvidence()
  },
)

watch(
  () => props.tree,
  (newTree) => {
    if (!newTree || newTree.length === 0) {
      selectedCriteria.value = null
      selectedStandard.value = null
      return
    }

    if (selectedCriteria.value) {
      syncSelectedCriteria(newTree)
    }
    else {
      selectFirstEvidence()
    }
  },
  { deep: true, immediate: true },
)

function selectFirstEvidence() {
  if (!props.tree || props.tree.length === 0) return

  for (const std of props.tree) {
    if (std.criteriaDTOList) {
      for (const crit of std.criteriaDTOList) {
        if (crit.evidenceUrl) {
          selectedCriteria.value = crit
          selectedStandard.value = std
          return
        }
        if (crit.subCriteriaList) {
          for (const sub of crit.subCriteriaList) {
            if (sub.evidenceUrl) {
              selectedCriteria.value = sub
              selectedStandard.value = std
              return
            }
          }
        }
      }
    }
  }
}

function syncSelectedCriteria(newTree: StandardDTO[]) {
  if (!selectedCriteria.value) return

  for (const std of newTree) {
    if (std.criteriaDTOList) {
      for (const crit of std.criteriaDTOList) {
        if (crit.publicId === selectedCriteria.value.publicId) {
          selectedCriteria.value = crit
          selectedStandard.value = std
          return
        }
        if (crit.subCriteriaList) {
          for (const sub of crit.subCriteriaList) {
            if (sub.publicId === selectedCriteria.value.publicId) {
              selectedCriteria.value = sub
              selectedStandard.value = std
              return
            }
          }
        }
      }
    }
  }
}

function handleSelectCriteria(data: { criteria: any, standard: any }) {
  selectedCriteria.value = data.criteria
  selectedStandard.value = data.standard
}

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

const stats = computed(() => {
  let total = 0
  let approved = 0
  let rejected = 0
  let pending = 0
  let notSubmitted = 0

  const processCriteria = (c: CriteriaDTO) => {
    if (c.evidenceType !== 'NONE') {
      total++
      if (!c.evidenceUrl) {
        notSubmitted++
      }
      else if (c.evidenceStatus === 'APPROVED') {
        approved++
      }
      else if (c.evidenceStatus === 'REJECTED') {
        rejected++
      }
      else {
        pending++
      }
    }
    if (c.subCriteriaList && c.subCriteriaList.length > 0) {
      c.subCriteriaList.forEach(processCriteria)
    }
  }

  props.tree.forEach((std) => {
    if (std.criteriaDTOList) {
      std.criteriaDTOList.forEach(processCriteria)
    }
  })

  return { total, approved, rejected, pending, notSubmitted }
})
</script>
