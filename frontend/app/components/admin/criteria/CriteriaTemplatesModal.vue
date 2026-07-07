<template>
  <UModal
    :open="open"
    title="Thư viện Tiêu chí mẫu"
    class="sm:max-w-5xl w-[92vw]"
    scrollable
    @update:open="emit('update:open', $event)"
  >
    <template #body>
      <div class="space-y-4 min-h-[400px]">
        <!-- List Mode -->
        <div
          v-if="mode === 'list'"
          class="space-y-4"
        >
          <!-- Controls -->
          <div class="flex flex-col md:flex-row gap-3 items-center justify-between w-full pb-2 border-b border-slate-100/50">
            <UInput
              v-model="searchQuery"
              placeholder="Tìm kiếm tiêu chí mẫu..."
              icon="i-lucide-search"
              class="w-full md:max-w-xs"
              :ui="{ base: 'h-9.5 rounded-xl bg-slate-50/50 border-slate-200' }"
            />
            <div class="flex flex-wrap items-center gap-3 w-full md:w-auto justify-end">
              <div class="flex items-center gap-1 text-xs font-semibold text-slate-500 mr-1 shrink-0">
                <UIcon
                  name="i-lucide-filter"
                  class="size-3.5"
                />
                <span>Bộ lọc:</span>
              </div>
              <USelect
                v-model="selectedLevelFilter"
                :items="levelFilterOptions"
                class="w-full sm:w-44"
                :ui="{ base: 'h-9.5 rounded-xl bg-slate-50/50' }"
              />
              <USelect
                v-model="selectedMandatoryFilter"
                :items="mandatoryFilterOptions"
                class="w-full sm:w-44"
                :ui="{ base: 'h-9.5 rounded-xl bg-slate-50/50' }"
              />
              <UButton
                color="info"
                icon="i-lucide-plus"
                label="Thêm mẫu mới"
                class="cursor-pointer font-semibold rounded-xl w-full sm:w-auto shrink-0"
                @click="enterAddMode"
              />
            </div>
          </div>

          <!-- Loading state -->
          <div
            v-if="loading"
            class="space-y-3"
          >
            <USkeleton class="h-16 w-full rounded-xl" />
            <USkeleton class="h-16 w-full rounded-xl" />
            <USkeleton class="h-16 w-full rounded-xl" />
          </div>

          <!-- Empty state -->
          <div
            v-else-if="filteredTemplates.length === 0"
            class="flex flex-col items-center justify-center py-12 border border-dashed border-slate-200 rounded-2xl text-center"
          >
            <UIcon
              name="i-lucide-book-open"
              class="size-10 text-slate-300 mb-2.5"
            />
            <h4 class="font-bold text-slate-800 text-sm">
              Không tìm thấy tiêu chí mẫu phù hợp
            </h4>
            <p class="text-xs text-slate-400 mt-1">
              Vui lòng thử điều chỉnh bộ lọc hoặc từ khóa tìm kiếm khác.
            </p>
          </div>

          <!-- List templates -->
          <div
            v-else
            class="divide-y divide-slate-100 max-h-[450px] overflow-y-auto pr-1"
          >
            <div
              v-for="template in filteredTemplates"
              :key="template.publicId"
              class="py-4 first:pt-0 last:pb-0 flex items-start justify-between gap-4 group"
            >
              <div class="min-w-0 flex-1">
                <div class="flex flex-wrap items-center gap-2">
                  <h4 class="font-bold text-slate-800 text-sm tracking-tight leading-snug">
                    {{ template.name }}
                  </h4>
                  <UBadge
                    :color="template.mandatory ? 'success' : 'neutral'"
                    variant="subtle"
                    class="rounded-md font-bold text-[9px] px-1.5 py-0.5 shrink-0"
                  >
                    {{ template.mandatory ? 'Bắt buộc' : 'Không bắt buộc' }}
                  </UBadge>
                  <UBadge
                    v-if="template.evidenceType !== 'NONE'"
                    color="info"
                    variant="subtle"
                    class="rounded-md font-bold text-[9px] px-1.5 py-0.5 shrink-0"
                  >
                    Yêu cầu minh chứng
                  </UBadge>
                  <UBadge
                    v-if="template.level"
                    color="warning"
                    variant="subtle"
                    class="rounded-md font-bold text-[9px] px-1.5 py-0.5 shrink-0"
                  >
                    {{ getLevelLabel(template.level) }}
                  </UBadge>
                </div>
                <p
                  v-if="template.description"
                  class="mt-1.5 text-xs text-slate-500 leading-relaxed line-clamp-2"
                  :title="template.description"
                >
                  {{ template.description }}
                </p>
              </div>

              <!-- Actions -->
              <div class="flex items-center gap-1 opacity-70 group-hover:opacity-100 transition-opacity">
                <UButton
                  color="neutral"
                  variant="ghost"
                  icon="i-lucide-pen-line"
                  size="xs"
                  class="size-8 rounded-lg cursor-pointer hover:bg-slate-100 text-slate-400 hover:text-slate-700"
                  aria-label="Sửa tiêu chí mẫu"
                  @click="enterEditMode(template)"
                />
                <UButton
                  color="error"
                  variant="ghost"
                  icon="i-lucide-trash-2"
                  size="xs"
                  class="size-8 rounded-lg cursor-pointer hover:bg-red-50 text-slate-400 hover:text-red-600"
                  aria-label="Xóa tiêu chí mẫu"
                  @click="askDelete(template)"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- Add/Edit Mode -->
        <div
          v-else-if="mode === 'form'"
          class="space-y-4"
        >
          <div class="flex items-center gap-2 text-slate-800 font-bold border-b border-slate-100 pb-2 mb-2">
            <UButton
              color="neutral"
              variant="ghost"
              icon="i-lucide-arrow-left"
              class="size-8 rounded-lg cursor-pointer"
              @click="mode = 'list'"
            />
            <span class="text-sm">{{ editingTemplate ? 'Cập nhật tiêu chí mẫu' : 'Thêm tiêu chí mẫu mới' }}</span>
          </div>

          <UForm
            id="template-form"
            :state="formState"
            :schema="formSchema"
            class="grid gap-4 sm:grid-cols-2"
            @submit="handleSave"
          >
            <UFormField
              label="Tên tiêu chí mẫu"
              name="name"
              class="sm:col-span-2"
            >
              <UInput
                v-model="formState.name"
                placeholder="Ví dụ: Điểm rèn luyện đạt từ 90 điểm trở lên..."
                class="w-full"
              />
            </UFormField>

            <UFormField
              label="Mô tả chi tiết"
              name="description"
              class="sm:col-span-2"
            >
              <UTextarea
                v-model="formState.description"
                placeholder="Mô tả tiêu chuẩn, cách tính hoặc yêu cầu cụ thể..."
                :rows="4"
                class="w-full"
              />
            </UFormField>

            <UFormField
              label="Cấp xét duyệt"
              name="level"
            >
              <USelect
                v-model="formState.level"
                :items="levelOptions"
                class="w-full"
              />
            </UFormField>

            <UFormField
              label="Yêu cầu minh chứng"
              name="evidenceType"
            >
              <USelect
                v-model="formState.evidenceType"
                :items="evidenceOptions"
                class="w-full"
              />
            </UFormField>

            <div class="pt-2 sm:col-span-2">
              <UCheckbox
                v-model="formState.mandatory"
                label="Tiêu chí bắt buộc phải đạt"
              />
            </div>
          </UForm>
        </div>
      </div>
    </template>

    <template #footer>
      <div class="flex w-full justify-between gap-3">
        <!-- Delete Confirmation Inline -->
        <div
          v-if="deletingTemplate"
          class="flex items-center gap-3 flex-1"
        >
          <span class="text-xs text-red-600 font-semibold truncate">Xác nhận xóa mẫu này?</span>
          <div class="flex gap-1 shrink-0">
            <UButton
              color="error"
              label="Xóa"
              size="xs"
              :loading="saving"
              class="cursor-pointer rounded-lg"
              @click="confirmDelete"
            />
            <UButton
              color="neutral"
              variant="outline"
              label="Hủy"
              size="xs"
              class="cursor-pointer rounded-lg"
              @click="deletingTemplate = null"
            />
          </div>
        </div>
        <div
          v-else
          class="flex-1"
        />

        <div class="flex gap-2.5 shrink-0">
          <div v-if="mode === 'list'">
            <UButton
              color="neutral"
              variant="outline"
              label="Đóng"
              class="cursor-pointer rounded-xl"
              @click="emit('update:open', false)"
            />
          </div>
          <div
            v-else
            class="flex gap-2"
          >
            <UButton
              color="neutral"
              variant="outline"
              label="Quay lại"
              class="cursor-pointer rounded-xl"
              @click="mode = 'list'"
            />
            <UButton
              color="info"
              :loading="saving"
              label="Lưu mẫu"
              type="submit"
              form="template-form"
              class="cursor-pointer font-semibold rounded-xl"
            />
          </div>
        </div>
      </div>
    </template>
  </UModal>
</template>

<script setup lang="ts">
import { ref, computed, watch, reactive } from 'vue'
import { z } from 'zod'
import type { CriteriaTemplate, EvidenceType, CampaignLevel } from '~/types/admin'
import { useAdminCriteria } from '~/composables/admin/useAdminCriteria'
import { getErrorMessage } from '~/utils/errors'

const props = defineProps<{
  open: boolean
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  'change': []
}>()

const toast = useToast()
const {
  fetchCriteriaTemplates,
  createCriteriaTemplate,
  updateCriteriaTemplate,
  deleteCriteriaTemplate,
} = useAdminCriteria()

const mode = ref<'list' | 'form'>('list')
const loading = ref(false)
const saving = ref(false)
const searchQuery = ref('')
const templates = ref<CriteriaTemplate[]>([])

// Filters
const selectedLevelFilter = ref<'ALL' | 'UNIVERSITY' | 'CITY' | 'NATION'>('ALL')
const selectedMandatoryFilter = ref<'ALL' | 'MANDATORY' | 'OPTIONAL'>('ALL')

const levelFilterOptions = [
  { label: 'Tất cả cấp xét', value: 'ALL' },
  { label: 'Cấp trường', value: 'UNIVERSITY' },
  { label: 'Cấp thành phố', value: 'CITY' },
  { label: 'Cấp trung ương', value: 'NATION' },
]

const mandatoryFilterOptions = [
  { label: 'Tất cả tính chất', value: 'ALL' },
  { label: 'Tiêu chí bắt buộc', value: 'MANDATORY' },
  { label: 'Tiêu chí tự chọn', value: 'OPTIONAL' },
]

const levelOptions = [
  { label: 'Áp dụng chung (Không phân cấp)', value: null },
  { label: 'Cấp trường', value: 'UNIVERSITY' },
  { label: 'Cấp thành phố', value: 'CITY' },
  { label: 'Cấp trung ương', value: 'NATION' },
]

const getLevelLabel = (level: string | null | undefined) => {
  if (!level) return 'Chung'
  switch (level) {
    case 'UNIVERSITY':
      return 'Cấp trường'
    case 'CITY':
      return 'Cấp thành phố'
    case 'NATION':
      return 'Cấp trung ương'
    default:
      return level
  }
}

const editingTemplate = ref<CriteriaTemplate | null>(null)
const deletingTemplate = ref<CriteriaTemplate | null>(null)

const evidenceOptions = [
  { label: 'Không yêu cầu minh chứng', value: 'NONE' },
  { label: 'Yêu cầu nộp minh chứng (File)', value: 'FILE' },
]

const formSchema = z.object({
  name: z.string().trim().min(2, 'Tên mẫu phải có ít nhất 2 ký tự.').max(500, 'Tên mẫu không được quá 500 ký tự.'),
  description: z.string().max(2000).nullable().optional(),
  evidenceType: z.enum(['NONE', 'IMAGE', 'PDF', 'FILE', 'LINK']),
  mandatory: z.boolean(),
  level: z.enum(['UNIVERSITY', 'CITY', 'NATION']).nullable().optional(),
})

const formState = reactive({
  name: '',
  description: '',
  evidenceType: 'NONE' as EvidenceType,
  mandatory: true,
  level: null as CampaignLevel | null,
})

const loadTemplates = async () => {
  loading.value = true
  try {
    templates.value = await fetchCriteriaTemplates()
  }
  catch (error) {
    toast.add({
      title: 'Không thể tải tiêu chí mẫu',
      description: getErrorMessage(error, 'Vui lòng thử lại sau.'),
      color: 'error',
    })
  }
  finally {
    loading.value = false
  }
}

watch(() => props.open, (newVal) => {
  if (newVal) {
    mode.value = 'list'
    searchQuery.value = ''
    selectedLevelFilter.value = 'ALL'
    selectedMandatoryFilter.value = 'ALL'
    editingTemplate.value = null
    deletingTemplate.value = null
    loadTemplates()
  }
})

const filteredTemplates = computed(() => {
  let result = templates.value

  const q = searchQuery.value.trim().toLowerCase()
  if (q) {
    result = result.filter(t =>
      t.name.toLowerCase().includes(q)
      || (t.description && t.description.toLowerCase().includes(q)),
    )
  }

  if (selectedLevelFilter.value !== 'ALL') {
    result = result.filter(t => t.level === selectedLevelFilter.value)
  }

  if (selectedMandatoryFilter.value !== 'ALL') {
    const isMandatory = selectedMandatoryFilter.value === 'MANDATORY'
    result = result.filter(t => t.mandatory === isMandatory)
  }

  return result
})

const enterAddMode = () => {
  editingTemplate.value = null
  Object.assign(formState, {
    name: '',
    description: '',
    evidenceType: 'NONE',
    mandatory: true,
    level: null,
  })
  mode.value = 'form'
}

const enterEditMode = (template: CriteriaTemplate) => {
  editingTemplate.value = template
  Object.assign(formState, {
    name: template.name,
    description: template.description || '',
    evidenceType: template.evidenceType || 'NONE',
    mandatory: template.mandatory ?? true,
    level: template.level || null,
  })
  mode.value = 'form'
}

const askDelete = (template: CriteriaTemplate) => {
  deletingTemplate.value = template
}

const confirmDelete = async () => {
  if (!deletingTemplate.value) return
  saving.value = true
  try {
    await deleteCriteriaTemplate(deletingTemplate.value.publicId)
    toast.add({ title: 'Đã xóa tiêu chí mẫu', color: 'success' })
    deletingTemplate.value = null
    await loadTemplates()
    emit('change')
  }
  catch (error) {
    toast.add({
      title: 'Không thể xóa tiêu chí mẫu',
      description: getErrorMessage(error, 'Vui lòng thử lại sau.'),
      color: 'error',
    })
  }
  finally {
    saving.value = false
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    const payload = {
      name: formState.name,
      description: formState.description,
      evidenceType: formState.evidenceType,
      mandatory: formState.mandatory,
      level: formState.level,
    }
    if (editingTemplate.value) {
      await updateCriteriaTemplate(editingTemplate.value.publicId, payload)
      toast.add({ title: 'Đã cập nhật tiêu chí mẫu', color: 'success' })
    }
    else {
      await createCriteriaTemplate(payload)
      toast.add({ title: 'Đã thêm tiêu chí mẫu mới', color: 'success' })
    }
    mode.value = 'list'
    await loadTemplates()
    emit('change')
  }
  catch (error) {
    toast.add({
      title: 'Không thể lưu tiêu chí mẫu',
      description: getErrorMessage(error, 'Vui lòng kiểm tra lại thông tin.'),
      color: 'error',
    })
  }
  finally {
    saving.value = false
  }
}
</script>
