<template>
  <UModal
    :open="open"
    :title="isEditing ? 'Cập nhật tiêu chí' : 'Tạo tiêu chí'"
    scrollable
    @update:open="emit('update:open', $event)"
  >
    <template #body>
      <form
        class="grid gap-4 sm:grid-cols-2"
        @submit.prevent="submit"
      >
        <UFormField
          label="Standard"
          name="standardPublicId"
          :error="errors.standardPublicId"
          class="sm:col-span-2"
        >
          <USelect
            v-model="form.standardPublicId"
            :items="standardOptions"
            :disabled="isEditing"
            placeholder="Chọn standard"
          />
        </UFormField>
        <UFormField
          label="Tên tiêu chí"
          name="name"
          :error="errors.name"
          class="sm:col-span-2"
        >
          <UInput
            v-model="form.name"
            placeholder="Nhập tên tiêu chí"
          />
        </UFormField>
        <UFormField
          label="Mô tả"
          name="description"
          class="sm:col-span-2"
        >
          <UTextarea
            v-model="form.description"
            placeholder="Mô tả ngắn cho tiêu chí"
            :rows="3"
          />
        </UFormField>
        <UFormField
          label="Tiêu chí cha"
          name="parentPublicId"
        >
          <USelect
            v-model="form.parentPublicId"
            :items="parentOptions"
            placeholder="Không có tiêu chí cha"
          />
        </UFormField>
        <UFormField
          label="Loại minh chứng"
          name="evidenceType"
        >
          <USelect
            v-model="form.evidenceType"
            :items="evidenceTypeOptions"
          />
        </UFormField>
        <UFormField
          label="Số tiêu chí con cần đạt"
          name="requiredChildrenCount"
          :error="errors.requiredChildrenCount"
        >
          <UInput
            v-model.number="form.requiredChildrenCount"
            type="number"
            min="0"
          />
        </UFormField>
        <div class="flex items-end pb-2">
          <UCheckbox
            v-model="form.mandatory"
            label="Tiêu chí bắt buộc"
          />
        </div>
      </form>
    </template>
    <template #footer>
      <div class="flex w-full justify-end gap-3">
        <UButton
          color="neutral"
          variant="outline"
          label="Hủy"
          :disabled="loading"
          @click="emit('update:open', false)"
        />
        <UButton
          color="info"
          :loading="loading"
          :label="isEditing ? 'Lưu thay đổi' : 'Tạo tiêu chí'"
          @click="submit"
        />
      </div>
    </template>
  </UModal>
</template>

<script setup lang="ts">
import type { CriteriaForm, CriteriaNode, EvidenceType, Standard } from '~/types/admin'

const props = defineProps<{
  open: boolean
  standards: Standard[]
  criteria?: CriteriaNode | null
  defaultStandardPublicId?: string | null
  defaultParentPublicId?: string | null
  parentCandidates: Array<{ publicId: string, name: string, standardPublicId: string }>
  loading?: boolean
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  'submit': [standardPublicId: string, form: CriteriaForm]
}>()

type FormState = Omit<CriteriaForm, 'parentPublicId'> & {
  standardPublicId: string | undefined
  parentPublicId: string | undefined
}
type Errors = Partial<Record<'standardPublicId' | 'name' | 'requiredChildrenCount', string>>

const createBlank = (): FormState => ({
  standardPublicId: undefined,
  name: '',
  description: '',
  orderIndex: 1,
  mandatory: true,
  requiredChildrenCount: 0,
  evidenceType: 'NONE',
  parentPublicId: undefined,
})

const form = reactive<FormState>(createBlank())
const errors = reactive<Errors>({})
const isEditing = computed(() => Boolean(props.criteria))

const standardOptions = computed(() => props.standards.map(standard => ({
  label: standard.name,
  value: standard.publicId,
})))

const parentOptions = computed(() => [
  { label: 'Không có tiêu chí cha', value: undefined },
  ...props.parentCandidates
    .filter(candidate => candidate.standardPublicId === form.standardPublicId && candidate.publicId !== props.criteria?.publicId)
    .map(candidate => ({ label: candidate.name, value: candidate.publicId })),
])

const evidenceTypeOptions: Array<{ label: string, value: EvidenceType }> = [
  { label: 'Không yêu cầu minh chứng', value: 'NONE' },
  { label: 'Yêu cầu nộp minh chứng', value: 'FILE' },
]

watch(() => [props.open, props.criteria, props.defaultStandardPublicId, props.defaultParentPublicId] as const, () => {
  Object.assign(form, props.criteria
    ? {
        standardPublicId: props.defaultStandardPublicId ?? undefined,
        name: props.criteria.name,
        description: props.criteria.description || '',
        orderIndex: props.criteria.orderIndex,
        mandatory: props.criteria.mandatory,
        requiredChildrenCount: props.criteria.requiredChildrenCount,
        evidenceType: props.criteria.evidenceType || 'NONE',
        parentPublicId: props.criteria.parentPublicId || undefined,
      }
    : {
        ...createBlank(),
        standardPublicId: props.defaultStandardPublicId ?? undefined,
        parentPublicId: props.defaultParentPublicId ?? undefined,
      })
  Object.assign(errors, { standardPublicId: undefined, name: undefined, requiredChildrenCount: undefined })
}, { immediate: true })

const submit = () => {
  Object.assign(errors, { standardPublicId: undefined, name: undefined, requiredChildrenCount: undefined })
  if (!form.standardPublicId) errors.standardPublicId = 'Vui lòng chọn standard.'
  if (!form.name.trim()) errors.name = 'Tên tiêu chí là bắt buộc.'
  if (!Number.isInteger(form.requiredChildrenCount) || form.requiredChildrenCount < 0) {
    errors.requiredChildrenCount = 'Giá trị phải là số nguyên không âm.'
  }
  if (Object.values(errors).some(Boolean) || !form.standardPublicId) return

  const { standardPublicId, ...payload } = form
  emit('submit', standardPublicId, {
    name: payload.name.trim(),
    description: payload.description,
    orderIndex: payload.orderIndex,
    mandatory: payload.mandatory,
    requiredChildrenCount: payload.requiredChildrenCount,
    evidenceType: payload.evidenceType,
    parentPublicId: payload.parentPublicId || null,
  })
}
</script>
