<template>
  <div class="space-y-5">
    <!-- Header Page -->
    <div class="px-1">
      <h1 class="text-2xl font-bold text-[#1E293B] tracking-tight">
        {{ isGroup ? 'Quản lý hồ sơ tập thể' : 'Quản lý hồ sơ cá nhân' }}
      </h1>
      <p class="mt-1 text-sm text-[#64748B]">
        {{ isGroup ? 'Xem và đánh giá minh chứng tập thể lớp, câu lạc bộ hoặc đơn vị.' : 'Xem và đánh giá minh chứng sinh viên theo từng tiêu chí.' }}
      </p>
    </div>

    <slot
      name="mode-switcher"
      :loading="loading"
      :refresh="load"
    />

    <section class="overflow-hidden rounded-2xl border border-slate-200/80 bg-white shadow-sm">
      <!-- Filters Section (Compact, single-row layout) -->
      <div class="border-b border-slate-200/70 bg-slate-50/70 p-4">
        <div class="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
          <!-- Search Keyword -->
          <UInput
            v-model="keyword"
            icon="i-lucide-search"
            placeholder="Tìm kiếm họ tên, MSSV, email..."
            class="w-full"
          />

          <!-- Campaign Dropdown -->
          <USelect
            v-model="campaignPublicId"
            :items="campaignOptions"
            placeholder="Chọn đợt xét duyệt"
            class="w-full"
          />

          <!-- Application Status Dropdown -->
          <USelect
            v-model="status"
            :items="statusOptions"
            placeholder="Tất cả trạng thái hồ sơ"
            class="w-full"
          />

          <!-- Level Dropdown -->
          <USelect
            v-model="selectedLevel"
            :items="supportedLevelOptions"
            placeholder="Chọn cấp xét tuyển"
            class="w-full"
          />
        </div>
      </div>

      <!-- Error message banner -->
      <div
        v-if="error"
        class="m-4 flex flex-wrap items-center justify-between gap-3 rounded-xl border border-rose-100 bg-rose-50 p-4 text-sm text-rose-700"
      >
        <div class="flex items-center gap-2">
          <UIcon
            name="i-lucide-alert-circle"
            class="size-5 shrink-0 text-rose-500"
          />
          <span>{{ error }}</span>
        </div>
        <UButton
          color="error"
          variant="soft"
          label="Thử lại"
          class="rounded-lg"
          @click="load"
        />
      </div>

      <!-- Main Applications List / Loading / Empty states -->
      <div class="p-5">
        <CommonPageLoading v-if="loading && !pageData" />

        <CommonPageEmpty
          v-else-if="pageData && !pageData.content.length"
          :title="isGroup ? 'Chưa có hồ sơ tập thể' : 'Chưa có hồ sơ sinh viên'"
          :desc="isGroup ? 'Không tìm thấy tập thể nào tham gia hoặc nộp minh chứng phù hợp.' : 'Không tìm thấy sinh viên nào tham gia hoặc nộp minh chứng phù hợp.'"
        />

        <template v-else>
          <!-- Custom student table component -->
          <div class="mx-auto w-full">
            <AdminEvidenceStudentApplicationTable
              :applications="pageData?.content || []"
              :loading="loading"
              :is-group="isGroup"
              @view-details="openApplicationDetails"
            />
          </div>

          <!-- Pagination -->
          <div
            v-if="pageData"
            class="mt-4 flex flex-col items-center justify-center gap-3 rounded-xl border border-slate-200/70 bg-slate-50/80 px-4 py-3 text-center sm:flex-row"
          >
            <p class="text-xs font-semibold text-slate-500">
              Tổng cộng <span class="font-bold text-slate-800">{{ pageData.totalElements }}</span> hồ sơ {{ isGroup ? 'tập thể' : 'sinh viên' }}
            </p>
            <UPagination
              v-model:page="uiPage"
              :total="pageData.totalElements"
              :items-per-page="pageData.size"
              class="scale-90"
            />
          </div>
        </template>
      </div>
    </section>

    <!-- Split Criteria & Evidences Review Modal -->
    <AdminEvidenceStudentApplicationDetailModal
      v-model:open="detailOpen"
      :application="selectedApplication"
      :tree="criteriaTree"
      :loading="treeLoading"
      :error="treeError"
      :action-loading="evaluating"
      @approve="handleApprove"
      @reject="handleReject"
      @save-comment="handleSaveComment"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import type { AdminPage, Campaign, StudentApplication, StandardResultDTO, CriteriaResultDTO } from '~/types/admin'
import { useAdminCriteria } from '~/composables/admin/useAdminCriteria'
import { useAdminEvidence } from '~/composables/admin/useAdminEvidence'
import { getErrorMessage } from '~/utils/errors'

const props = defineProps<{
  isGroup: boolean
}>()

const toast = useToast()
const { fetchResults, fetchUserCriteriaTree, approveEvidence, rejectEvidence, updateEvidenceComment } = useAdminEvidence()
const { fetchCampaigns } = useAdminCriteria()

// Query State
const keyword = ref('')
const status = ref<string | undefined>()
const campaignPublicId = ref<string | undefined>()
const selectedLevel = ref<string | undefined>()
const campaigns = ref<Campaign[]>([])
const uiPage = ref(1)

// Table Data State
const pageData = ref<AdminPage<StudentApplication> | null>(null)
const loading = ref(false)
const error = ref('')

// Modal View Details State
const detailOpen = ref(false)
const selectedApplication = ref<StudentApplication | null>(null)
const criteriaTree = ref<StandardResultDTO[]>([])
const treeLoading = ref(false)
const treeError = ref('')

// Evaluating state for modal buttons
const evaluating = ref(false)

const statusOptions = [
  { label: 'Tất cả trạng thái hồ sơ', value: undefined },
  { label: 'Nháp (Đang làm)', value: 'DRAFT' },
  { label: 'Đã nộp', value: 'SUBMITTED' },
  { label: 'Đạt yêu cầu', value: 'APPROVED' },
  { label: 'Không đạt', value: 'REJECTED' },
]

const filteredCampaigns = computed(() => {
  return campaigns.value.filter((campaign) => {
    if (props.isGroup) {
      return campaign.isGroup === 'GROUP' || campaign.isGroup === 'BOTH'
    }
    else {
      return campaign.isGroup === 'INDIVIDUAL' || campaign.isGroup === 'BOTH'
    }
  })
})

const activeCampaign = computed(() => {
  return campaigns.value.find(c => c.publicId === campaignPublicId.value)
})

const supportedLevelOptions = computed(() => {
  const lvl = activeCampaign.value?.level
  const allOptions = [
    { label: 'Tất cả các cấp', value: undefined },
    { label: 'Cấp trường', value: 'UNIVERSITY' },
    { label: 'Cấp thành phố', value: 'CITY' },
    { label: 'Cấp trung ương', value: 'NATION' },
  ]
  if (!lvl || lvl === 'ALL') return allOptions
  if (lvl === 'UNI_CITY') return allOptions.filter(o => o.value === undefined || o.value === 'UNIVERSITY' || o.value === 'CITY')
  if (lvl === 'UNI_NATION') return allOptions.filter(o => o.value === undefined || o.value === 'UNIVERSITY' || o.value === 'NATION')
  if (lvl === 'CITY_NATION') return allOptions.filter(o => o.value === undefined || o.value === 'CITY' || o.value === 'NATION')
  return allOptions.filter(o => o.value === undefined || o.value === lvl)
})

const campaignOptions = computed(() => [
  { label: 'Tất cả đợt xét chọn', value: undefined },
  ...filteredCampaigns.value.map(campaign => ({ label: campaign.name, value: campaign.publicId })),
])

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await fetchResults(campaignPublicId.value, {
      page: uiPage.value - 1,
      size: 10,
      status: status.value,
      keyword: keyword.value,
      isGroup: props.isGroup,
      level: selectedLevel.value,
    })
    pageData.value = response.data
  }
  catch (cause) {
    error.value = getErrorMessage(cause, 'Không thể tải danh sách hồ sơ.')
  }
  finally {
    loading.value = false
  }
}

const loadCampaigns = async () => {
  try {
    const response = await fetchCampaigns()
    campaigns.value = response.data.content
    // Default to the first active/closed campaign of the correct type if exists to display content immediately
    if (filteredCampaigns.value.length > 0 && !campaignPublicId.value) {
      campaignPublicId.value = filteredCampaigns.value[0]?.publicId
    }
  }
  catch {
    // Left empty since campaign selection is required to trigger load
  }
}

// Watch filters to trigger reloading list
const debouncedLoad = useDebounceFn(() => {
  uiPage.value = 1
  load()
}, 350)

watch(keyword, debouncedLoad)

watch(campaignPublicId, () => {
  if (selectedLevel.value === undefined) {
    uiPage.value = 1
    load()
  }
  else {
    selectedLevel.value = undefined
  }
})

watch([status, selectedLevel], () => {
  uiPage.value = 1
  load()
})

watch(uiPage, load)

// Modal 1: Open student's criteria tree
const openApplicationDetails = async (app: StudentApplication) => {
  selectedApplication.value = app
  detailOpen.value = true
  await refreshCriteriaTree()
}

const refreshCriteriaTree = async () => {
  if (!selectedApplication.value) return
  treeLoading.value = true
  treeError.value = ''
  try {
    const response = await fetchUserCriteriaTree(
      selectedApplication.value.userPublicId,
      selectedApplication.value.campaignPublicId,
      props.isGroup,
      selectedApplication.value.level,
    )
    criteriaTree.value = response.data
  }
  catch (cause) {
    treeError.value = getErrorMessage(cause, 'Không thể tải cây tiêu chí.')
  }
  finally {
    treeLoading.value = false
  }
}

// Evaluation handlers in the split screen modal
const handleApprove = async (data: { criteria: CriteriaResultDTO, comment: string }) => {
  if (!data.criteria.evidencePublicId) return
  evaluating.value = true
  try {
    await approveEvidence(data.criteria.evidencePublicId, data.comment)
    toast.add({ title: 'Đã chấp thuận minh chứng', color: 'success' })

    // Refresh trees
    await refreshCriteriaTree()
    await load()
  }
  catch (cause) {
    toast.add({
      title: 'Lỗi khi chấp thuận',
      description: getErrorMessage(cause, 'Vui lòng thử lại.'),
      color: 'error',
    })
  }
  finally {
    evaluating.value = false
  }
}

const handleReject = async (data: { criteria: CriteriaResultDTO, reason: string }) => {
  if (!data.criteria.evidencePublicId) return
  evaluating.value = true
  try {
    await rejectEvidence(data.criteria.evidencePublicId, data.reason)
    toast.add({ title: 'Đã từ chối minh chứng', color: 'success' })

    // Refresh trees
    await refreshCriteriaTree()
    await load()
  }
  catch (cause) {
    toast.add({
      title: 'Lỗi khi từ chối',
      description: getErrorMessage(cause, 'Vui lòng thử lại.'),
      color: 'error',
    })
  }
  finally {
    evaluating.value = false
  }
}

const handleSaveComment = async (data: { criteria: CriteriaResultDTO, comment: string }) => {
  if (!data.criteria.evidencePublicId) return
  evaluating.value = true
  try {
    await updateEvidenceComment(data.criteria.evidencePublicId, data.comment)
    toast.add({ title: 'Đã cập nhật nhận xét minh chứng', color: 'success' })

    await refreshCriteriaTree()
    await load()
  }
  catch (cause) {
    toast.add({
      title: 'Lỗi khi cập nhật nhận xét',
      description: getErrorMessage(cause, 'Vui lòng thử lại.'),
      color: 'error',
    })
  }
  finally {
    evaluating.value = false
  }
}

onMounted(async () => {
  await loadCampaigns()
  await load()
})
</script>
