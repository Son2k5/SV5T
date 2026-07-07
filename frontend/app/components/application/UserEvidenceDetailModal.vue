<template>
  <UModal
    :open="open"
    title="Xem Hồ Sơ Minh Chứng Đã Nộp"
    description="Xem nội dung chi tiết các tiêu chí và minh chứng đã nộp của bạn."
    class="sm:max-w-7xl w-[92vw]"
    :close-button="{ icon: 'i-lucide-x', color: 'neutral', variant: 'ghost' }"
    @update:open="emit('update:open', $event)"
  >
    <template #body>
      <!-- Loading State -->
      <div
        v-if="loading"
        class="flex flex-col items-center justify-center py-20 min-h-[450px]"
      >
        <UIcon
          name="i-lucide-loader-2"
          class="size-10 animate-spin text-blue-600"
        />
        <p class="mt-3 text-sm text-[#64748B] font-medium">
          Đang tải hồ sơ minh chứng...
        </p>
      </div>

      <!-- Error State -->
      <div
        v-else-if="error"
        class="py-12 text-center min-h-[300px] flex flex-col items-center justify-center"
      >
        <UIcon
          name="i-lucide-alert-triangle"
          class="size-12 text-rose-500 mb-2"
        />
        <p class="text-sm font-semibold text-rose-600">
          {{ error }}
        </p>
      </div>

      <!-- Split Layout content -->
      <div
        v-else
        class="space-y-4"
      >
        <!-- Student Information Card (Compact & Modern) -->
        <div class="rounded-xl bg-slate-50 border border-slate-100 p-4 shadow-2xs">
          <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
            <!-- Student metadata -->
            <div class="flex flex-wrap items-center gap-x-6 gap-y-2">
              <div>
                <p class="text-[10px] text-slate-400 font-bold uppercase tracking-wider">
                  Họ và tên
                </p>
                <p class="mt-0.5 font-bold text-sm text-[#1E293B]">
                  {{ profile?.fullName || 'Chưa cập nhật' }}
                </p>
              </div>
              <div class="h-8 w-px bg-slate-200 hidden md:block" />
              <div>
                <p class="text-[10px] text-slate-400 font-bold uppercase tracking-wider">
                  Mã số sinh viên
                </p>
                <p class="mt-0.5 font-mono font-semibold text-sm text-[#1E293B]">
                  {{ profile?.studentCode || 'N/A' }}
                </p>
              </div>
              <div class="h-8 w-px bg-slate-200 hidden md:block" />
              <div>
                <p class="text-[10px] text-slate-400 font-bold uppercase tracking-wider">
                  Đợt xét chọn
                </p>
                <p class="mt-0.5 text-xs text-[#334155] font-semibold truncate max-w-[200px]">
                  {{ application?.campaignName }}
                </p>
              </div>
              <div class="h-8 w-px bg-slate-200 hidden md:block" />
              <div>
                <p class="text-[10px] text-slate-400 font-bold uppercase tracking-wider">
                  Cấp hồ sơ
                </p>
                <p class="mt-0.5 text-xs text-[#334155] font-bold">
                  {{ getLevelLabel(application?.level) }}
                </p>
              </div>
            </div>

            <!-- Evidence Stats Badge Row -->
            <div class="flex flex-wrap gap-2 pt-2 md:pt-0 border-t border-slate-200/60 md:border-t-0">
              <span class="inline-flex items-center gap-1 text-[11px] px-2 py-0.5 rounded-full bg-slate-100 text-slate-600 font-bold">
                Tiêu chí: {{ stats.total }}
              </span>
              <span class="inline-flex items-center gap-1 text-[11px] px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 font-bold border border-emerald-100">
                Đạt: {{ stats.approved }}
              </span>
              <span class="inline-flex items-center gap-1 text-[11px] px-2 py-0.5 rounded-full bg-rose-50 text-rose-700 font-bold border border-rose-100">
                Lỗi: {{ stats.rejected }}
              </span>
              <span class="inline-flex items-center gap-1 text-[11px] px-2 py-0.5 rounded-full bg-amber-50 text-amber-700 font-bold border border-amber-100">
                Chờ duyệt: {{ stats.pending }}
              </span>
            </div>
          </div>
        </div>

        <!-- Main Body: Sidebar Tree & Detail Preview Side-by-Side -->
        <div class="flex flex-col md:flex-row h-[620px] border border-slate-200 rounded-xl overflow-hidden bg-white shadow-2xs">
          <!-- Left: Criteria tree (35% width) -->
          <div class="w-full md:w-[35%] border-b md:border-b-0 md:border-r border-slate-200 flex flex-col h-full min-h-0">
            <AdminEvidenceCriteriaTree
              :tree="tree"
              :selected-criteria-public-id="selectedCriteria?.publicId"
              @select-criteria="handleSelectCriteria"
            />
          </div>

          <!-- Right: Evidence detail & Actions (65% width) -->
          <div class="w-full md:w-[65%] flex flex-col h-full min-h-0 bg-white">
            <AdminEvidenceDetailPanel
              :criteria="selectedCriteria"
              :standard="selectedStandard"
              readonly
            />
          </div>
        </div>
      </div>
    </template>
  </UModal>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import type { ApplicationRecordResponse, CriteriaDTO, StandardDTO } from '~/types/applicationRecord'
import { useCurrentUser } from '~/composables/profile/useCurrentUser'
import type { ProfileData } from '~/types/profile'

const props = defineProps<{
  open: boolean
  application: ApplicationRecordResponse | null
  tree: StandardDTO[]
  loading?: boolean
  error?: string
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
}>()

const profile = ref<ProfileData | null>(null)

// Fetch current user details on mount
onMounted(async () => {
  try {
    const currentUser = await useCurrentUser()
    if (currentUser && currentUser.profile) {
      profile.value = currentUser.profile.value
    }
  }
  catch (err) {
    console.error('Failed to load user profile in read-only modal', err)
  }
})

const selectedCriteria = ref<CriteriaDTO | null>(null)
const selectedStandard = ref<StandardDTO | null>(null)

// Clear selection and select first available evidence on modal open
watch(
  () => props.open,
  (isOpen) => {
    if (!isOpen) {
      selectedCriteria.value = null
      selectedStandard.value = null
    }
    else {
      selectFirstEvidence()
    }
  },
)

// Auto-select first evidence item in the tree when tree finishes loading
watch(
  () => props.tree,
  () => {
    if (props.open && !selectedCriteria.value) {
      selectFirstEvidence()
    }
  },
  { deep: true },
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

// Watch tree updates to synchronize the local selected criteria item with refreshed state
watch(
  () => props.tree,
  (newTree) => {
    if (!selectedCriteria.value || !newTree) return

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
  },
  { deep: true },
)

function handleSelectCriteria(data: { criteria: CriteriaDTO, standard: StandardDTO }) {
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

// Compute tree criteria statistics
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
