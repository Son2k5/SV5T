<template>
  <div class="space-y-6">
    <div class="flex flex-col justify-between gap-4 lg:flex-row lg:items-start">
      <div>
        <UButton
          to="/admin/campaigns"
          color="neutral"
          variant="link"
          icon="i-lucide-arrow-left"
          label="Quay lại danh sách"
          class="-ml-3"
        />
        <h1 class="text-2xl font-bold text-[#1E293B]">
          {{ campaign?.name || 'Đang tải đợt xét duyệt' }}
        </h1>
        <div
          v-if="campaign"
          class="mt-1.5 flex flex-wrap gap-2 text-xs font-semibold text-slate-500"
        >
          <span class="bg-slate-100 text-slate-700 px-2 py-0.5 rounded-md">Năm học: {{ campaign.academicYear }}</span>
          <span class="bg-blue-50 text-blue-700 px-2 py-0.5 rounded-md">{{ getLevelLabel(campaign.level) }}</span>
          <span class="bg-indigo-50 text-indigo-700 px-2 py-0.5 rounded-md">
            {{ campaign.isGroup === 'BOTH' ? 'Cả hai' : (campaign.isGroup === 'GROUP' ? 'Tập thể' : 'Cá nhân') }}
          </span>
        </div>
        <p class="mt-2.5 max-w-2xl text-sm text-[#64748B]">
          {{ campaign?.description || 'Chưa có mô tả cho đợt xét duyệt này.' }}
        </p>
      </div>

      <UButton
        v-if="campaign"
        color="info"
        variant="soft"
        icon="i-lucide-pen-line"
        label="Sửa thông tin đợt"
        @click="campaignFormOpen = true"
      />
    </div>

    <div
      v-if="loading"
      class="space-y-4"
    >
      <USkeleton class="h-36 w-full rounded-2xl" />
      <USkeleton class="h-[440px] w-full rounded-2xl" />
    </div>

    <template v-else-if="campaign">
      <section class="grid gap-4 sm:grid-cols-3">
        <article class="rounded-2xl border border-blue-100 bg-gradient-to-br from-blue-50 to-white p-4">
          <p class="text-xs font-semibold uppercase tracking-wider text-[#64748B]">
            Trạng thái
          </p>
          <UBadge
            class="mt-3"
            :color="campaign.status === 'ACTIVE' ? 'success' : 'neutral'"
            variant="subtle"
          >
            {{ campaign.status }}
          </UBadge>
        </article>

        <article class="rounded-2xl border border-blue-100 bg-gradient-to-br from-blue-50 to-white p-4">
          <p class="text-xs font-semibold uppercase tracking-wider text-[#64748B]">
            Standard
          </p>
          <p class="mt-2 text-2xl font-bold text-[#1E293B]">
            {{ standards.length }}
          </p>
        </article>

        <article class="rounded-2xl border border-blue-100 bg-gradient-to-br from-blue-50 to-white p-4">
          <p class="text-xs font-semibold uppercase tracking-wider text-[#64748B]">
            Tiêu chí
          </p>
          <p class="mt-2 text-2xl font-bold text-[#1E293B]">
            {{ campaign.criteriaCount }}
          </p>
        </article>
      </section>

      <section class="grid gap-6 xl:grid-cols-[290px_minmax(0,1fr)]">
        <aside class="rounded-2xl border border-[#E5E7EB] bg-white p-3 shadow-sm">
          <div class="mb-3 flex items-center justify-between px-2">
            <h2 class="font-bold text-[#1E293B]">
              Standard
            </h2>
            <UButton
              color="info"
              variant="ghost"
              icon="i-lucide-plus"
              aria-label="Thêm standard"
              @click="openCreateStandard"
            />
          </div>

          <div class="space-y-2">
            <button
              v-for="standard in standards"
              :key="standard.publicId"
              type="button"
              class="group flex w-full items-center gap-3 rounded-xl p-3 text-left transition"
              :class="selectedStandardId === standard.publicId
                ? 'bg-[#2563EB] text-white shadow-lg shadow-blue-200'
                : 'text-[#334155] hover:bg-blue-50'"
              @click="selectedStandardId = standard.publicId"
            >
              <span
                class="flex size-9 shrink-0 items-center justify-center rounded-lg"
                :class="selectedStandardId === standard.publicId ? 'bg-white/15' : 'bg-blue-100 text-[#2563EB]'"
              >
                <UIcon name="i-lucide-layers-2" />
              </span>
              <span class="min-w-0 flex-1">
                <span class="block truncate text-sm font-semibold">{{ standard.name }}</span>
                <span
                  v-if="standard.description"
                  class="mt-0.5 block truncate text-xs opacity-75"
                >{{ standard.description }}</span>
              </span>
              <div class="flex items-center gap-1">
                <UButton
                  color="neutral"
                  variant="ghost"
                  icon="i-lucide-pen-line"
                  size="xs"
                  class="opacity-0 transition group-hover:opacity-100"
                  :class="{ '!text-white/80': selectedStandardId === standard.publicId }"
                  aria-label="Sửa standard"
                  @click.stop="openEditStandard(standard)"
                />
                <UButton
                  color="error"
                  variant="ghost"
                  icon="i-lucide-trash-2"
                  size="xs"
                  class="opacity-0 transition group-hover:opacity-100"
                  :class="{ '!text-white/85 hover:!bg-red-700': selectedStandardId === standard.publicId }"
                  aria-label="Xóa standard"
                  @click.stop="askDeleteStandard(standard)"
                />
              </div>
            </button>

            <CommonPageEmpty
              v-if="!standards.length"
              title="Chưa có standard"
              desc="Tạo standard trước khi thêm tiêu chí."
            >
              <UButton
                color="info"
                icon="i-lucide-plus"
                label="Tạo standard mới"
                @click="openCreateStandard"
              />
            </CommonPageEmpty>
          </div>
        </aside>

        <main class="overflow-hidden rounded-2xl border border-[#E5E7EB] bg-white shadow-sm">
          <div class="flex flex-col justify-between gap-4 border-b border-[#E5E7EB] p-5 sm:flex-row sm:items-center">
            <div>
              <p class="text-xs font-semibold uppercase tracking-wider text-[#64748B]">
                Tiêu chí đạt / không đạt
              </p>
              <h2 class="mt-1 text-lg font-bold text-[#1E293B]">
                {{ selectedStandard?.name || 'Chọn một standard' }}
              </h2>
            </div>

            <UButton
              color="info"
              icon="i-lucide-plus"
              label="Thêm tiêu chí"
              :disabled="!selectedStandardId"
              @click="openCreateCriterion"
            />
          </div>

          <div
            v-if="criteriaLoading"
            class="space-y-3 p-5"
          >
            <USkeleton class="h-16 w-full rounded-xl" />
            <USkeleton class="h-16 w-full rounded-xl" />
            <USkeleton class="h-16 w-full rounded-xl" />
          </div>

          <CommonPageEmpty
            v-else-if="selectedStandardId && !criteria.length"
            title="Chưa có tiêu chí"
            desc="Thêm tiêu chí để cấu hình điều kiện đạt cho standard này."
          >
            <UButton
              color="info"
              icon="i-lucide-plus"
              label="Tạo tiêu chí mới"
              @click="openCreateCriterion"
            />
          </CommonPageEmpty>

          <div
            v-else-if="selectedStandardId"
            class="divide-y divide-[#E5E7EB]"
          >
            <article
              v-for="(criterion, index) in criteria"
              :key="criterion.publicId"
              class="flex flex-col gap-4 p-5 transition hover:bg-blue-50/40 sm:flex-row sm:items-center"
            >
              <div class="flex min-w-0 flex-1 items-start gap-3">
                <div class="flex size-9 shrink-0 items-center justify-center rounded-xl bg-blue-100 text-sm font-bold text-[#2563EB]">
                  {{ criterion.orderIndex }}
                </div>

                <div class="min-w-0">
                  <div class="flex flex-wrap items-center gap-2">
                    <h3 class="font-semibold text-[#1E293B]">
                      {{ criterion.name }}
                    </h3>
                    <UBadge
                      :color="criterion.mandatory ? 'success' : 'neutral'"
                      variant="subtle"
                    >
                      {{ criterion.mandatory ? 'Bắt buộc' : 'Không bắt buộc' }}
                    </UBadge>
                    <UBadge
                      v-if="criterion.evidenceType !== 'NONE'"
                      color="info"
                      variant="subtle"
                    >
                      Có minh chứng
                    </UBadge>
                  </div>

                  <p
                    v-if="criterion.description"
                    class="mt-1 text-sm text-[#64748B]"
                  >
                    {{ criterion.description }}
                  </p>

                  <p
                    v-if="criterion.requiredChildrenCount > 0"
                    class="mt-2 text-xs text-[#64748B]"
                  >
                    Cần đạt tối thiểu {{ criterion.requiredChildrenCount }} tiêu chí con.
                  </p>
                </div>
              </div>

              <div class="flex shrink-0 justify-end gap-1">
                <UButton
                  color="neutral"
                  variant="ghost"
                  icon="i-lucide-arrow-up"
                  :disabled="index === 0 || criterionSaving"
                  aria-label="Đưa tiêu chí lên"
                  @click="moveCriterion(criterion, -1)"
                />
                <UButton
                  color="neutral"
                  variant="ghost"
                  icon="i-lucide-arrow-down"
                  :disabled="index === criteria.length - 1 || criterionSaving"
                  aria-label="Đưa tiêu chí xuống"
                  @click="moveCriterion(criterion, 1)"
                />
                <UButton
                  color="neutral"
                  variant="ghost"
                  icon="i-lucide-shield-alert"
                  aria-label="Yêu cầu minh chứng"
                  @click="openConfigureRequirement(criterion)"
                />
                <UButton
                  color="info"
                  variant="ghost"
                  icon="i-lucide-pen-line"
                  aria-label="Sửa tiêu chí"
                  @click="openEditCriterion(criterion)"
                />
                <UButton
                  color="error"
                  variant="ghost"
                  icon="i-lucide-trash-2"
                  aria-label="Xóa tiêu chí"
                  @click="askDeleteCriterion(criterion)"
                />
              </div>
            </article>
          </div>

          <CommonPageEmpty
            v-else
            title="Chọn standard"
            desc="Danh sách tiêu chí sẽ hiện tại đây."
          />
        </main>
      </section>
    </template>

    <AdminCampaignsCampaignFormModal
      v-if="campaign"
      v-model:open="campaignFormOpen"
      :campaign="campaign"
      :loading="campaignSaving"
      @submit="saveCampaign"
    />

    <UModal
      :open="standardFormOpen"
      :title="editingStandard ? 'Cập nhật standard' : 'Tạo standard'"
      @update:open="standardFormOpen = $event"
    >
      <template #body>
        <UForm
          id="standard-form"
          :state="standardForm"
          :schema="standardSchema"
          class="space-y-4"
          @submit="saveStandard"
        >
          <UFormField
            label="Tên standard"
            name="name"
          >
            <UInput
              v-model="standardForm.name"
              placeholder="Ví dụ: Học tập"
            />
          </UFormField>
          <UFormField
            label="Mô tả"
            name="description"
          >
            <UTextarea
              v-model="standardForm.description"
              :rows="3"
            />
          </UFormField>
        </UForm>
      </template>
      <template #footer>
        <div class="flex w-full justify-end gap-3">
          <UButton
            color="neutral"
            variant="outline"
            label="Hủy"
            @click="standardFormOpen = false"
          />
          <UButton
            color="info"
            :loading="standardSaving"
            label="Lưu standard"
            type="submit"
            form="standard-form"
          />
        </div>
      </template>
    </UModal>

    <UModal
      :open="criterionFormOpen"
      :title="editingCriterion ? 'Cập nhật tiêu chí' : 'Thêm tiêu chí'"
      scrollable
      @update:open="criterionFormOpen = $event"
    >
      <template #body>
        <UForm
          id="criterion-form"
          :state="criterionForm"
          :schema="criterionSchema"
          class="grid gap-4 sm:grid-cols-2"
          @submit="saveCriterion"
        >
          <UFormField
            label="Tên tiêu chí"
            name="name"
            class="sm:col-span-2"
          >
            <UInput
              v-model="criterionForm.name"
              placeholder="Ví dụ: Điểm trung bình từ 3.2"
            />
          </UFormField>

          <UFormField
            label="Mô tả"
            name="description"
            class="sm:col-span-2"
          >
            <UTextarea
              v-model="criterionForm.description"
              :rows="3"
            />
          </UFormField>

          <UFormField
            label="Tiêu chí cha"
            name="parentPublicId"
          >
            <USelect
              v-model="criterionForm.parentPublicId"
              :items="parentOptions"
            />
          </UFormField>

          <UFormField
            label="Loại minh chứng"
            name="evidenceType"
          >
            <USelect
              v-model="criterionForm.evidenceType"
              :items="evidenceOptions"
            />
          </UFormField>

          <UFormField
            label="Thứ tự hiển thị"
            name="orderIndex"
          >
            <UInput
              v-model.number="criterionForm.orderIndex"
              type="number"
              min="1"
            />
          </UFormField>

          <UFormField
            label="Số tiêu chí con cần đạt"
            name="requiredChildrenCount"
          >
            <UInput
              v-model.number="criterionForm.requiredChildrenCount"
              type="number"
              min="0"
            />
          </UFormField>

          <div class="sm:col-span-2">
            <UCheckbox
              v-model="criterionForm.mandatory"
              label="Tiêu chí bắt buộc phải đạt"
            />
          </div>
        </UForm>
      </template>

      <template #footer>
        <div class="flex w-full justify-end gap-3">
          <UButton
            color="neutral"
            variant="outline"
            label="Hủy"
            @click="criterionFormOpen = false"
          />
          <UButton
            color="info"
            :loading="criterionSaving"
            label="Lưu tiêu chí"
            type="submit"
            form="criterion-form"
          />
        </div>
      </template>
    </UModal>

    <AdminSharedConfirmDeleteModal
      v-model:open="deleteCriterionOpen"
      title="Xóa tiêu chí?"
      :description="`Tiêu chí “${deletingCriterion?.name || ''}” sẽ bị xóa.`"
      :loading="criterionSaving"
      @confirm="confirmDeleteCriterion"
    />

    <AdminSharedConfirmDeleteModal
      v-model:open="deleteStandardOpen"
      title="Xóa nhóm tiêu chuẩn (Standard)?"
      :description="`Nhóm “${deletingStandard?.name || ''}” sẽ bị xóa vĩnh viễn.`"
      :loading="standardSaving"
      @confirm="confirmDeleteStandard"
    />

    <AdminCriteriaRequirementModal
      v-model:open="requirementOpen"
      :criterion="selectedCriterionForRequirement"
      :loading="requirementSaving"
      @submit="submitRequirement"
    />
  </div>
</template>

<script setup lang="ts">
import { z } from 'zod'
import type {
  Campaign,
  CampaignForm,
  Criteria,
  CriteriaForm,
  EvidenceType,
  Standard,
} from '~/types/admin'
import { getErrorMessage } from '~/utils/errors'
import { useAdminCampaigns } from '~/composables/admin/useAdminCampaigns'

definePageMeta({ layout: 'admin', middleware: 'require-admin' })

const route = useRoute()
const toast = useToast()
const campaignPublicId = String(route.params.publicId)

const {
  fetchCampaign,
  updateCampaign,
  fetchStandards,
  createStandard,
  updateStandard,
  deleteStandard,
  fetchCriteria,
  createCriteria,
  updateCriteria,
  deleteCriteria,
  reorderCriteria,
  updateCriteriaRequirement,
} = useAdminCampaigns()

const campaign = ref<Campaign | null>(null)

const getLevelLabel = (level?: string) => {
  if (!level) return ''
  switch (level) {
    case 'UNIVERSITY':
      return 'Cấp trường'
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
      return level
  }
}

const standards = ref<Standard[]>([])
const selectedStandardId = ref<string | null>(null)
const criteria = ref<Criteria[]>([])

const loading = ref(true)
const criteriaLoading = ref(false)
const campaignSaving = ref(false)
const standardSaving = ref(false)
const criterionSaving = ref(false)

const campaignFormOpen = ref(route.query.edit === '1')
const standardFormOpen = ref(false)
const criterionFormOpen = ref(false)
const deleteCriterionOpen = ref(false)
const deleteStandardOpen = ref(false)
const requirementOpen = ref(false)

const editingStandard = ref<Standard | null>(null)
const deletingStandard = ref<Standard | null>(null)
const editingCriterion = ref<Criteria | null>(null)
const deletingCriterion = ref<Criteria | null>(null)
const selectedCriterionForRequirement = ref<Criteria | null>(null)
const requirementSaving = ref(false)

const standardSchema = z.object({
  name: z.string().trim().min(2, 'Tên standard phải có ít nhất 2 ký tự.').max(500, 'Tên standard không được vượt quá 500 ký tự'),
  description: z.string().max(1000).nullable().optional(),
})

const criterionSchema = z.object({
  name: z.string().trim().min(2, 'Tên tiêu chí phải có ít nhất 2 ký tự.').max(500, 'Tên tiêu chí không được vượt quá 500 ký tự'),
  description: z.string().max(1000).nullable().optional(),
  orderIndex: z.number().int().min(1),
  mandatory: z.boolean(),
  requiredChildrenCount: z.number().int().min(0),
  evidenceType: z.enum(['NONE', 'IMAGE', 'PDF', 'FILE', 'LINK']),
  parentPublicId: z.string().nullable().optional(),
})

const standardForm = reactive({
  name: '',
  description: '',
})

type CriterionFormState = Omit<CriteriaForm, 'parentPublicId'> & {
  parentPublicId?: string
}

const criterionForm = reactive<CriterionFormState>({
  name: '',
  description: '',
  orderIndex: 1,
  mandatory: true,
  requiredChildrenCount: 0,
  evidenceType: 'NONE',
  parentPublicId: undefined,
})

const buildCriteriaPayload = (): CriteriaForm => ({
  name: criterionForm.name,
  description: criterionForm.description,
  orderIndex: criterionForm.orderIndex,
  mandatory: criterionForm.mandatory,
  requiredChildrenCount: criterionForm.requiredChildrenCount,
  evidenceType: criterionForm.evidenceType || 'NONE',
  parentPublicId: criterionForm.parentPublicId || null,
})

const selectedStandard = computed(() =>
  standards.value.find(item => item.publicId === selectedStandardId.value) || null,
)

const parentOptions = computed(() => [
  { label: 'Không có tiêu chí cha', value: undefined },
  ...criteria.value
    .filter(item => item.publicId !== editingCriterion.value?.publicId)
    .map(item => ({
      label: item.name,
      value: item.publicId,
    })),
])

const evidenceOptions: Array<{ label: string, value: EvidenceType }> = [
  { label: 'Không yêu cầu minh chứng', value: 'NONE' },
  { label: 'Yêu cầu nộp minh chứng', value: 'FILE' },
]

const loadCriteria = async () => {
  if (!selectedStandardId.value) {
    criteria.value = []
    return
  }

  criteriaLoading.value = true
  try {
    criteria.value = await fetchCriteria(selectedStandardId.value)
  }
  catch (cause) {
    toast.add({
      title: 'Không thể tải tiêu chí',
      description: getErrorMessage(cause, 'Vui lòng thử lại.'),
      color: 'error',
    })
  }
  finally {
    criteriaLoading.value = false
  }
}

const load = async () => {
  loading.value = true
  try {
    const [campaignData, standardsData] = await Promise.all([
      fetchCampaign(campaignPublicId),
      fetchStandards(campaignPublicId),
    ])

    campaign.value = campaignData
    standards.value = standardsData

    if (!selectedStandardId.value && standardsData.length) {
      selectedStandardId.value = standardsData[0]?.publicId || null
    }

    await loadCriteria()
  }
  catch (cause) {
    toast.add({
      title: 'Không thể tải đợt xét duyệt',
      description: getErrorMessage(cause, 'Vui lòng thử lại.'),
      color: 'error',
    })
  }
  finally {
    loading.value = false
  }
}

watch(selectedStandardId, loadCriteria)

const saveCampaign = async (form: CampaignForm) => {
  campaignSaving.value = true
  try {
    campaign.value = (await updateCampaign(campaignPublicId, form)).data
    campaignFormOpen.value = false
    toast.add({ title: 'Đã cập nhật đợt xét duyệt', color: 'success' })
  }
  catch (cause) {
    toast.add({
      title: 'Không thể cập nhật đợt xét duyệt',
      description: getErrorMessage(cause, 'Vui lòng thử lại.'),
      color: 'error',
    })
  }
  finally {
    campaignSaving.value = false
  }
}

const openCreateStandard = () => {
  editingStandard.value = null
  Object.assign(standardForm, { name: '', description: '' })
  standardFormOpen.value = true
}

const openEditStandard = (standard: Standard) => {
  editingStandard.value = standard
  Object.assign(standardForm, {
    name: standard.name,
    description: standard.description || '',
  })
  standardFormOpen.value = true
}

const saveStandard = async () => {
  standardSaving.value = true
  try {
    if (editingStandard.value) {
      await updateStandard(editingStandard.value.publicId, standardForm)
      toast.add({ title: 'Đã cập nhật standard', color: 'success' })
    }
    else {
      const response = await createStandard(campaignPublicId, standardForm)
      selectedStandardId.value = response.data.publicId
      toast.add({ title: 'Đã tạo standard', color: 'success' })
    }

    standards.value = await fetchStandards(campaignPublicId)
    standardFormOpen.value = false
  }
  catch (cause) {
    toast.add({
      title: 'Không thể lưu standard',
      description: getErrorMessage(cause, 'Vui lòng kiểm tra lại dữ liệu.'),
      color: 'error',
    })
  }
  finally {
    standardSaving.value = false
  }
}

const openCreateCriterion = () => {
  editingCriterion.value = null
  Object.assign(criterionForm, {
    name: '',
    description: '',
    orderIndex: criteria.value.length + 1,
    mandatory: true,
    requiredChildrenCount: 0,
    evidenceType: 'NONE',
    parentPublicId: undefined,
  })
  criterionFormOpen.value = true
}

const openEditCriterion = (criterion: Criteria) => {
  editingCriterion.value = criterion
  Object.assign(criterionForm, {
    name: criterion.name,
    description: criterion.description || '',
    orderIndex: criterion.orderIndex,
    mandatory: criterion.mandatory,
    requiredChildrenCount: criterion.requiredChildrenCount,
    evidenceType: criterion.evidenceType || 'NONE',
    parentPublicId: criterion.parentPublicId || undefined,
  })
  criterionFormOpen.value = true
}

const saveCriterion = async () => {
  if (!selectedStandardId.value) return

  criterionSaving.value = true
  try {
    const payload = buildCriteriaPayload()
    if (editingCriterion.value) {
      await updateCriteria(editingCriterion.value.publicId, payload)
      toast.add({ title: 'Đã cập nhật tiêu chí', color: 'success' })
    }
    else {
      await createCriteria(selectedStandardId.value, payload)
      toast.add({ title: 'Đã thêm tiêu chí', color: 'success' })
    }

    criterionFormOpen.value = false
    await loadCriteria()
    await load()
  }
  catch (cause) {
    toast.add({
      title: 'Không thể lưu tiêu chí',
      description: getErrorMessage(cause, 'Tên tiêu chí có thể đã tồn tại trong đợt xét duyệt.'),
      color: 'error',
    })
  }
  finally {
    criterionSaving.value = false
  }
}

const moveCriterion = async (criterion: Criteria, offset: number) => {
  const currentIndex = criteria.value.findIndex(item => item.publicId === criterion.publicId)
  const targetIndex = currentIndex + offset

  if (targetIndex < 0 || targetIndex >= criteria.value.length) return

  criterionSaving.value = true
  try {
    await reorderCriteria(criterion.publicId, targetIndex + 1)
    await loadCriteria()
    toast.add({ title: 'Đã đổi thứ tự tiêu chí', color: 'success' })
  }
  catch (cause) {
    toast.add({
      title: 'Không thể đổi thứ tự',
      description: getErrorMessage(cause, 'Vui lòng thử lại.'),
      color: 'error',
    })
  }
  finally {
    criterionSaving.value = false
  }
}

const askDeleteCriterion = (criterion: Criteria) => {
  deletingCriterion.value = criterion
  deleteCriterionOpen.value = true
}

const confirmDeleteCriterion = async () => {
  if (!deletingCriterion.value) return

  criterionSaving.value = true
  try {
    await deleteCriteria(deletingCriterion.value.publicId)
    deleteCriterionOpen.value = false
    await loadCriteria()
    await load()
    toast.add({ title: 'Đã xóa tiêu chí', color: 'success' })
  }
  catch (cause) {
    toast.add({
      title: 'Không thể xóa tiêu chí',
      description: getErrorMessage(cause, 'Tiêu chí có thể có tiêu chí con hoặc minh chứng.'),
      color: 'error',
    })
  }
  finally {
    criterionSaving.value = false
  }
}

const askDeleteStandard = (standard: Standard) => {
  deletingStandard.value = standard
  deleteStandardOpen.value = true
}

const confirmDeleteStandard = async () => {
  if (!deletingStandard.value) return
  standardSaving.value = true
  try {
    const criteriaList = await fetchCriteria(deletingStandard.value.publicId)
    if (criteriaList && criteriaList.length > 0) {
      toast.add({
        title: 'Không thể xóa standard',
        description: 'Standard này đang chứa tiêu chí. Vui lòng xóa hết các tiêu chí bên trong trước.',
        color: 'error',
      })
      deleteStandardOpen.value = false
      return
    }

    await deleteStandard(deletingStandard.value.publicId)
    toast.add({ title: 'Đã xóa standard thành công', color: 'success' })
    standards.value = await fetchStandards(campaignPublicId)
    if (selectedStandardId.value === deletingStandard.value.publicId) {
      selectedStandardId.value = standards.value[0]?.publicId || null
    }
    deleteStandardOpen.value = false
  }
  catch (cause) {
    toast.add({
      title: 'Không thể xóa standard',
      description: getErrorMessage(cause, 'Vui lòng thử lại sau.'),
      color: 'error',
    })
  }
  finally {
    standardSaving.value = false
  }
}

const openConfigureRequirement = (criterion: Criteria) => {
  selectedCriterionForRequirement.value = criterion
  requirementOpen.value = true
}

const submitRequirement = async (form: { mandatory: boolean, requiredChildrenCount: number, evidenceType: EvidenceType }) => {
  if (!selectedCriterionForRequirement.value) return
  requirementSaving.value = true
  try {
    await updateCriteriaRequirement(selectedCriterionForRequirement.value.publicId, form)
    toast.add({ title: 'Đã cập nhật yêu cầu minh chứng thành công', color: 'success' })
    requirementOpen.value = false
    await loadCriteria()
  }
  catch (cause) {
    toast.add({
      title: 'Không thể cập nhật yêu cầu',
      description: getErrorMessage(cause, 'Vui lòng thử lại.'),
      color: 'error',
    })
  }
  finally {
    requirementSaving.value = false
  }
}

onMounted(load)
</script>
