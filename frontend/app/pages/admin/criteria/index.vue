<template>
  <div class="space-y-4">
    <!-- Hàng 1: Tiêu đề + Đợt xét duyệt + Thư viện mẫu -->
    <div class="flex flex-wrap items-center justify-between gap-3">
      <h1 class="text-xl font-bold text-slate-900 tracking-tight whitespace-nowrap">
        Quản lý Tiêu chí
      </h1>
      <div class="flex items-center gap-3 flex-1 justify-end min-w-0">
        <USelect
          v-model="selectedCampaignPublicId"
          :items="campaignOptions"
          placeholder="Chọn đợt xét duyệt..."
          class="w-full min-w-[220px] max-w-[340px]"
          :ui="{ base: 'h-10 rounded-xl bg-slate-50/50 border-slate-200' }"
        />
        <UButton
          color="info"
          icon="i-lucide-book-open"
          label="Thư viện mẫu"
          class="cursor-pointer font-semibold rounded-xl whitespace-nowrap shrink-0"
          @click="templateLibraryOpen = true"
        />
      </div>
    </div>

    <div
      v-if="campaignLoading"
      class="space-y-3"
    >
      <USkeleton class="h-10 w-48 rounded-xl" />
      <USkeleton class="h-10 w-full rounded-xl" />
      <USkeleton class="h-[420px] w-full rounded-2xl" />
    </div>

    <template v-else-if="campaign">
      <!-- Hàng 2: Tab Cá nhân / Tập thể -->
      <div
        v-if="campaign.isGroup === 'BOTH'"
        class="flex"
      >
        <div class="flex gap-1 p-1 bg-slate-100/80 rounded-xl">
          <button
            type="button"
            class="flex items-center gap-1.5 px-3.5 py-1.5 text-xs font-semibold rounded-lg transition-all cursor-pointer"
            :class="!activeTabIsGroup
              ? 'bg-white text-blue-600 shadow-sm'
              : 'text-slate-600 hover:text-slate-900'"
            @click="activeTabIsGroup = false"
          >
            <UIcon
              name="i-lucide-user"
              class="size-3.5"
            />
            Tiêu chuẩn Cá nhân
          </button>
          <button
            type="button"
            class="flex items-center gap-1.5 px-3.5 py-1.5 text-xs font-semibold rounded-lg transition-all cursor-pointer"
            :class="activeTabIsGroup
              ? 'bg-white text-blue-600 shadow-sm'
              : 'text-slate-600 hover:text-slate-900'"
            @click="activeTabIsGroup = true"
          >
            <UIcon
              name="i-lucide-users"
              class="size-3.5"
            />
            Tiêu chuẩn Tập thể
          </button>
        </div>
      </div>

      <!-- Hàng 3: Filter cấp + các pill nhóm tiêu chuẩn -->
      <div class="flex items-center gap-3">
        <USelect
          v-if="isCampaignMultiLevel && standards.length > 0"
          v-model="selectedLevelFilter"
          :items="filterOptions"
          placeholder="Lọc theo cấp..."
          class="w-[150px] shrink-0"
          :ui="{ base: 'h-9 rounded-lg bg-slate-50/50 border-slate-200 text-xs' }"
        />

        <div
          v-if="isCampaignMultiLevel && standards.length > 0"
          class="h-6 w-px bg-slate-200 shrink-0"
        />

        <div class="flex items-center gap-2 overflow-x-auto flex-1 min-w-0 py-0.5">
          <button
            v-for="standard in filteredStandards"
            :key="standard.publicId"
            type="button"
            class="group flex items-center gap-1.5 rounded-full px-3.5 py-1.5 text-xs font-semibold transition cursor-pointer shrink-0"
            :class="selectedStandardId === standard.publicId
              ? 'bg-blue-600 text-white'
              : 'bg-white border border-slate-200 text-slate-600 hover:border-slate-300 hover:text-slate-900'"
            @click="selectedStandardId = standard.publicId"
          >
            <span class="truncate max-w-[140px]">{{ standard.name }}</span>
            <span
              v-if="isCampaignMultiLevel && standard.level"
              class="text-[9px] font-bold px-1 py-0.5 rounded-sm shrink-0"
              :class="selectedStandardId === standard.publicId ? 'bg-white/20' : 'bg-slate-100 text-slate-500'"
            >
              {{ getLevelLabel(standard.level) }}
            </span>
          </button>

          <UButton
            color="neutral"
            variant="soft"
            icon="i-lucide-plus"
            class="size-8 rounded-full cursor-pointer shrink-0"
            aria-label="Thêm nhóm tiêu chuẩn"
            @click="openCreateStandard"
          />
        </div>
      </div>

      <CommonPageEmpty
        v-if="standards.length && !filteredStandards.length"
        title="Không tìm thấy kết quả"
        desc="Không có nhóm tiêu chuẩn nào ở cấp này."
        class="py-6 border border-dashed border-slate-200 rounded-xl"
      />

      <CommonPageEmpty
        v-if="!standards.length"
        title="Chưa có nhóm tiêu chuẩn"
        desc="Tạo standard trước để thêm các tiêu chí con."
        class="py-6 border border-dashed border-slate-200 rounded-xl"
      >
        <UButton
          color="info"
          icon="i-lucide-plus"
          label="Tạo standard mới"
          class="cursor-pointer rounded-lg text-xs"
          @click="openCreateStandard"
        />
      </CommonPageEmpty>

      <!-- Hàng: "Tiêu chí thuộc nhóm X" + Sửa nhóm + Xóa nhóm + Thêm tiêu chí (đẩy phải) -->
      <div
        v-if="selectedStandardId"
        class="flex flex-wrap items-center justify-between gap-3 rounded-2xl bg-slate-50 px-4 py-3"
      >
        <h2 class="text-sm text-slate-700">
          Tiêu chí thuộc nhóm <span class="font-semibold text-blue-600">{{ selectedStandard?.name }}</span>
        </h2>
        <div class="flex items-center gap-1.5 ml-auto">
          <UButton
            color="neutral"
            variant="ghost"
            icon="i-lucide-pen-line"
            size="xs"
            label="Sửa nhóm"
            class="rounded-lg cursor-pointer text-slate-500 hover:text-blue-600"
            @click="openEditStandard(selectedStandard)"
          />
          <UButton
            color="error"
            variant="ghost"
            icon="i-lucide-trash-2"
            size="xs"
            label="Xóa nhóm"
            class="rounded-lg cursor-pointer text-slate-500 hover:text-red-600"
            @click="askDeleteStandard(selectedStandard)"
          />
          <div class="h-5 w-px bg-slate-200 mx-1" />
          <UButton
            color="info"
            icon="i-lucide-plus"
            label="Thêm tiêu chí"
            size="sm"
            class="cursor-pointer font-semibold rounded-xl"
            @click="openCreateCriterion"
          />
        </div>
      </div>

      <!-- Criteria grid -->
      <div v-if="selectedStandardId">
        <div
          v-if="criteriaLoading"
          class="grid gap-3 sm:grid-cols-2"
        >
          <USkeleton class="h-28 w-full rounded-2xl" />
          <USkeleton class="h-28 w-full rounded-2xl" />
          <USkeleton class="h-28 w-full rounded-2xl" />
          <USkeleton class="h-28 w-full rounded-2xl" />
        </div>

        <CommonPageEmpty
          v-else-if="!criteria.length"
          title="Nhóm này chưa có tiêu chí"
          desc="Thêm các tiêu chí cụ thể để cấu hình điều kiện xét duyệt."
          class="py-12 border border-dashed border-slate-200 rounded-2xl"
        >
          <UButton
            color="info"
            icon="i-lucide-plus"
            label="Tạo tiêu chí mới"
            class="cursor-pointer rounded-xl"
            @click="openCreateCriterion"
          />
        </CommonPageEmpty>

        <div
          v-else
          class="grid gap-3 sm:grid-cols-2"
        >
          <article
            v-for="(criterion, index) in criteria"
            :key="criterion.publicId"
            class="rounded-2xl border border-slate-200 bg-white p-4 hover:border-slate-300 transition"
          >
            <div class="flex items-start justify-between gap-2">
              <div class="flex items-center gap-2.5 min-w-0">
                <span class="flex size-7 shrink-0 items-center justify-center rounded-lg bg-blue-50 text-[11px] font-bold text-blue-600">
                  {{ criterion.orderIndex }}
                </span>
                <h3 class="font-bold text-slate-800 text-sm tracking-tight truncate">
                  {{ criterion.name }}
                </h3>
              </div>
              <div class="flex items-center gap-0.5 shrink-0">
                <UButton
                  color="neutral"
                  variant="ghost"
                  icon="i-lucide-arrow-up"
                  :disabled="index === 0 || criterionSaving"
                  class="size-7 rounded-lg cursor-pointer text-slate-400 hover:text-blue-600 disabled:opacity-30 disabled:pointer-events-none"
                  aria-label="Đưa tiêu chí lên"
                  @click="moveCriterion(criterion, -1)"
                />
                <UButton
                  color="neutral"
                  variant="ghost"
                  icon="i-lucide-arrow-down"
                  :disabled="index === criteria.length - 1 || criterionSaving"
                  class="size-7 rounded-lg cursor-pointer text-slate-400 hover:text-blue-600 disabled:opacity-30 disabled:pointer-events-none"
                  aria-label="Đưa tiêu chí xuống"
                  @click="moveCriterion(criterion, 1)"
                />
              </div>
            </div>

            <p
              v-if="criterion.description"
              class="mt-2 ml-9 text-xs text-slate-400 leading-relaxed line-clamp-2"
            >
              {{ criterion.description }}
            </p>

            <p
              v-if="criterion.requiredChildrenCount > 0"
              class="mt-2 ml-9 text-[10px] font-semibold text-slate-400 flex items-center gap-1.5"
            >
              <UIcon
                name="i-lucide-info"
                class="size-3.5 text-blue-500"
              />
              Cần đạt tối thiểu {{ criterion.requiredChildrenCount }} tiêu chí con.
            </p>

            <div class="mt-3 ml-9 flex items-center justify-between">
              <div class="flex flex-wrap gap-1.5">
                <UBadge
                  :color="criterion.mandatory ? 'success' : 'neutral'"
                  variant="subtle"
                  class="rounded-md font-bold text-[9px] px-1.5"
                >
                  {{ criterion.mandatory ? 'Bắt buộc' : 'Không bắt buộc' }}
                </UBadge>
                <UBadge
                  v-if="criterion.evidenceType !== 'NONE'"
                  color="info"
                  variant="subtle"
                  class="rounded-md font-bold text-[9px] px-1.5"
                >
                  Cần minh chứng
                </UBadge>
              </div>
              <div class="flex items-center gap-0.5">
                <UButton
                  color="neutral"
                  variant="ghost"
                  icon="i-lucide-shield-alert"
                  size="xs"
                  class="size-7 rounded-lg cursor-pointer text-slate-400 hover:text-amber-600"
                  aria-label="Yêu cầu minh chứng"
                  @click="openConfigureRequirement(criterion)"
                />
                <UButton
                  color="info"
                  variant="ghost"
                  icon="i-lucide-pen-line"
                  size="xs"
                  class="size-7 rounded-lg cursor-pointer text-slate-400 hover:text-green-600"
                  aria-label="Sửa tiêu chí"
                  @click="openEditCriterion(criterion)"
                />
                <UButton
                  color="error"
                  variant="ghost"
                  icon="i-lucide-trash-2"
                  size="xs"
                  class="size-7 rounded-lg cursor-pointer text-slate-400 hover:text-red-600"
                  aria-label="Xóa tiêu chí"
                  @click="askDeleteCriterion(criterion)"
                />
              </div>
            </div>
          </article>

          <button
            type="button"
            class="rounded-2xl border-2 border-dashed border-slate-200 flex items-center justify-center gap-2 text-slate-400 text-sm font-semibold hover:border-slate-300 hover:text-slate-600 cursor-pointer min-h-[110px]"
            @click="openCreateCriterion"
          >
            <UIcon
              name="i-lucide-plus"
              class="size-4"
            />
            Thêm tiêu chí mới
          </button>
        </div>
      </div>

      <CommonPageEmpty
        v-else-if="standards.length > 0"
        title="Chọn nhóm tiêu chuẩn"
        desc="Vui lòng nhấp chọn một nhóm tiêu chuẩn ở trên."
        class="py-16"
      />

      <!-- Bảng báo cáo nhỏ ở cuối trang -->
      <div class="rounded-xl border border-slate-200 overflow-hidden">
        <div class="px-4 py-2 bg-slate-50 border-b border-slate-200">
          <span class="text-[10px] font-bold uppercase tracking-wider text-slate-400">
            Thống kê đợt xét duyệt
          </span>
        </div>
        <div class="grid grid-cols-[1fr_auto] items-center gap-2 px-4 py-2.5 border-b border-slate-100">
          <span class="flex items-center gap-2 text-sm text-slate-600">
            <UIcon
              name="i-lucide-activity"
              class="size-4 text-slate-400"
            />
            Trạng thái đợt
          </span>
          <UBadge
            class="rounded-md font-bold text-[10px] px-2 py-0.5"
            :color="campaign.status === 'ACTIVE' ? 'success' : 'neutral'"
            variant="subtle"
          >
            {{ campaign.status === 'ACTIVE' ? 'Đang mở' : campaign.status }}
          </UBadge>
        </div>
        <div class="grid grid-cols-[1fr_auto] items-center gap-2 px-4 py-2.5 border-b border-slate-100">
          <span class="flex items-center gap-2 text-sm text-slate-600">
            <UIcon
              name="i-lucide-layers-2"
              class="size-4 text-slate-400"
            />
            Nhóm tiêu chuẩn
          </span>
          <span class="text-sm font-semibold text-slate-900">{{ standards.length }}</span>
        </div>
        <div class="grid grid-cols-[1fr_auto] items-center gap-2 px-4 py-2.5">
          <span class="flex items-center gap-2 text-sm text-slate-600">
            <UIcon
              name="i-lucide-list-checks"
              class="size-4 text-slate-400"
            />
            Tổng số tiêu chí
          </span>
          <span class="text-sm font-semibold text-slate-900">{{ campaign.criteriaCount }}</span>
        </div>
      </div>
    </template>

    <CommonPageEmpty
      v-else
      title="Chưa chọn đợt xét chọn"
      desc="Vui lòng lựa chọn một đợt xét chọn ở trên để hiển thị và quản lý cấu hình bộ tiêu chí."
      class="py-16"
    />

    <!-- Modals -->
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
          <UFormField
            v-if="isCampaignMultiLevel"
            label="Cấp của nhóm tiêu chuẩn"
            name="level"
          >
            <USelect
              v-model="standardForm.level"
              :items="standardLevelOptions"
              placeholder="Chọn cấp xét duyệt..."
              class="w-full"
              :ui="{ base: 'h-10 rounded-xl bg-slate-50/50 border-slate-200' }"
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
            class="cursor-pointer"
            @click="standardFormOpen = false"
          />
          <UButton
            color="info"
            :loading="standardSaving"
            label="Lưu standard"
            type="submit"
            form="standard-form"
            class="cursor-pointer"
          />
        </div>
      </template>
    </UModal>

    <UModal
      :open="criterionFormOpen"
      :title="editingCriterion ? 'Cập nhật tiêu chí' : 'Thêm tiêu chí'"
      class="sm:max-w-3xl w-[90vw]"
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
            label="Chọn nhanh từ tiêu chí mẫu"
            name="selectedTemplatePublicId"
            class="sm:col-span-2"
          >
            <USelect
              v-model="selectedTemplatePublicId"
              :items="templateOptions"
              placeholder="-- Chọn tiêu chí mẫu để tự động điền nhanh --"
              class="w-full"
            />
          </UFormField>

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
            class="cursor-pointer"
            @click="criterionFormOpen = false"
          />
          <UButton
            color="info"
            :loading="criterionSaving"
            label="Lưu tiêu chí"
            type="submit"
            form="criterion-form"
            class="cursor-pointer"
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

    <AdminCriteriaTemplatesModal
      v-model:open="templateLibraryOpen"
      @change="handleTemplateLibraryChange"
    />
  </div>
</template>

<script setup lang="ts">
import { z } from 'zod'
import { ref, computed, watch, onMounted, reactive } from 'vue'
import type {
  Campaign,
  CampaignLevel,
  Criteria,
  CriteriaForm,
  EvidenceType,
  Standard,
  CriteriaTemplate,
} from '~/types/admin'
import { getErrorMessage } from '~/utils/errors'
import { useAdminCampaigns } from '~/composables/admin/useAdminCampaigns'
import { useAdminCriteria } from '~/composables/admin/useAdminCriteria'

definePageMeta({ layout: 'admin', middleware: 'require-admin' })

const toast = useToast()
const {
  fetchCampaigns,
  fetchCampaign,
} = useAdminCampaigns()

const {
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
  fetchCriteriaTemplates,
} = useAdminCriteria()

// Campaign selection state
const campaignsList = ref<Campaign[]>([])
const selectedCampaignPublicId = ref<string | undefined>(undefined)
const campaignLoading = ref(false)

const getLevelLabel = (level: string) => {
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

const isCampaignMultiLevel = computed(() => {
  const lvl = campaign.value?.level
  return lvl === 'ALL' || lvl === 'UNI_CITY' || lvl === 'UNI_NATION' || lvl === 'CITY_NATION'
})

const standardLevelOptions = computed(() => {
  const lvl = campaign.value?.level
  const allOptions = [
    { label: 'Cấp trường', value: 'UNIVERSITY' },
    { label: 'Cấp thành phố', value: 'CITY' },
    { label: 'Cấp trung ương', value: 'NATION' },
  ]
  if (lvl === 'ALL') return allOptions
  if (lvl === 'UNI_CITY') return allOptions.filter(o => o.value === 'UNIVERSITY' || o.value === 'CITY')
  if (lvl === 'UNI_NATION') return allOptions.filter(o => o.value === 'UNIVERSITY' || o.value === 'NATION')
  if (lvl === 'CITY_NATION') return allOptions.filter(o => o.value === 'CITY' || o.value === 'NATION')
  return []
})

const campaignOptions = computed(() =>
  campaignsList.value.map(c => ({
    label: `${c.name} (${getLevelLabel(c.level)} - ${c.isGroup === 'BOTH' ? 'Cả hai' : (c.isGroup === 'GROUP' ? 'Tập thể' : 'Cá nhân')}) - ${c.academicYear}`,
    value: c.publicId,
  })),
)

const activeTabIsGroup = ref(false)
const campaign = ref<Campaign | null>(null)
const standards = ref<Standard[]>([])
const selectedStandardId = ref<string | null>(null)
const criteria = ref<Criteria[]>([])

// Level filter state
const selectedLevelFilter = ref<string>('ALL')

const filterOptions = computed(() => [
  { label: 'Tất cả các cấp', value: 'ALL' },
  ...standardLevelOptions.value,
])

const filteredStandards = computed(() => {
  if (!isCampaignMultiLevel.value || selectedLevelFilter.value === 'ALL') {
    return standards.value
  }
  return standards.value.filter(s => s.level === selectedLevelFilter.value)
})

const syncSelectedStandard = () => {
  const filtered = filteredStandards.value
  if (filtered.length > 0) {
    const isStillVisible = filtered.some(s => s.publicId === selectedStandardId.value)
    if (!isStillVisible) {
      selectedStandardId.value = filtered[0]?.publicId || null
    }
  }
  else {
    selectedStandardId.value = null
    criteria.value = []
  }
}

const criteriaLoading = ref(false)
const standardSaving = ref(false)
const criterionSaving = ref(false)

const standardFormOpen = ref(false)
const criterionFormOpen = ref(false)

// Template Library state
const templateLibraryOpen = ref(false)
const criteriaTemplates = ref<CriteriaTemplate[]>([])
const selectedTemplatePublicId = ref<string | undefined>(undefined)

const loadTemplatesForSelect = async () => {
  try {
    criteriaTemplates.value = await fetchCriteriaTemplates()
  }
  catch {
    // silently fail
  }
}

const templateOptions = computed(() =>
  criteriaTemplates.value.map(t => ({
    label: `${t.mandatory ? '[Bắt buộc]' : '[Tự chọn]'} ${t.name}`,
    value: t.publicId,
  })),
)

watch(selectedTemplatePublicId, (newPublicId) => {
  if (!newPublicId) return
  const tmpl = criteriaTemplates.value.find(t => t.publicId === newPublicId)
  if (tmpl) {
    criterionForm.name = tmpl.name
    criterionForm.description = tmpl.description || ''
    criterionForm.evidenceType = tmpl.evidenceType || 'NONE'
    criterionForm.mandatory = tmpl.mandatory ?? true
  }
  selectedTemplatePublicId.value = undefined
})

const handleTemplateLibraryChange = async () => {
  await loadTemplatesForSelect()
}
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
  level: z.string().optional().nullable(),
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
  level: 'UNIVERSITY' as CampaignLevel,
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

const loadCampaignsList = async () => {
  try {
    const response = await fetchCampaigns(0, undefined)
    campaignsList.value = response.data.content
    const firstCampaign = campaignsList.value[0]
    if (firstCampaign && !selectedCampaignPublicId.value) {
      selectedCampaignPublicId.value = firstCampaign.publicId
    }
  }
  catch {
    // left empty
  }
}

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
  if (!selectedCampaignPublicId.value) {
    campaign.value = null
    standards.value = []
    return
  }

  campaignLoading.value = true
  try {
    const campaignData = await fetchCampaign(selectedCampaignPublicId.value)
    campaign.value = campaignData

    if (campaignData.isGroup === 'GROUP') {
      activeTabIsGroup.value = true
    }
    else if (campaignData.isGroup === 'INDIVIDUAL') {
      activeTabIsGroup.value = false
    }

    const standardsData = await fetchStandards(selectedCampaignPublicId.value, activeTabIsGroup.value)
    standards.value = standardsData

    syncSelectedStandard()

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
    campaignLoading.value = false
  }
}

watch(selectedCampaignPublicId, () => {
  selectedStandardId.value = null
  selectedLevelFilter.value = 'ALL'
  standards.value = []
  criteria.value = []
  load()
})

watch(selectedStandardId, loadCriteria)

watch(selectedLevelFilter, () => {
  syncSelectedStandard()
})

watch(activeTabIsGroup, async () => {
  if (campaignLoading.value) return
  if (!selectedCampaignPublicId.value) return
  try {
    const standardsData = await fetchStandards(selectedCampaignPublicId.value, activeTabIsGroup.value)
    standards.value = standardsData
    syncSelectedStandard()
  }
  catch (cause) {
    toast.add({
      title: 'Không thể tải nhóm tiêu chuẩn',
      description: getErrorMessage(cause, 'Vui lòng thử lại.'),
      color: 'error',
    })
  }
})

const openCreateStandard = () => {
  editingStandard.value = null
  Object.assign(standardForm, { name: '', description: '', level: 'UNIVERSITY' })
  standardFormOpen.value = true
}

const openEditStandard = (standard: Standard) => {
  editingStandard.value = standard
  Object.assign(standardForm, {
    name: standard.name,
    description: standard.description || '',
    level: standard.level || 'UNIVERSITY',
  })
  standardFormOpen.value = true
}

const saveStandard = async () => {
  if (!selectedCampaignPublicId.value) return
  standardSaving.value = true
  try {
    const levelVal = isCampaignMultiLevel.value ? standardForm.level : undefined
    if (editingStandard.value) {
      await updateStandard(editingStandard.value.publicId, {
        name: standardForm.name,
        description: standardForm.description,
        level: levelVal,
        isGroup: activeTabIsGroup.value,
      })
      toast.add({ title: 'Đã cập nhật standard', color: 'success' })
    }
    else {
      const response = await createStandard(selectedCampaignPublicId.value, {
        name: standardForm.name,
        description: standardForm.description,
        level: levelVal,
        isGroup: activeTabIsGroup.value,
      })
      selectedStandardId.value = response.data.publicId
      toast.add({ title: 'Đã tạo standard', color: 'success' })
    }

    standards.value = await fetchStandards(selectedCampaignPublicId.value, activeTabIsGroup.value)
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
  if (!deletingStandard.value || !selectedCampaignPublicId.value) return
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
    standards.value = await fetchStandards(selectedCampaignPublicId.value, activeTabIsGroup.value)
    if (selectedStandardId.value === deletingStandard.value.publicId) {
      syncSelectedStandard()
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

onMounted(async () => {
  await loadCampaignsList()
  await load()
  await loadTemplatesForSelect()
})
</script>
