<template>
  <UModal
    :open="open"
    title="Cấu hình Yêu cầu minh chứng"
    @update:open="emit('update:open', $event)"
  >
    <template #body>
      <UForm
        :state="state"
        :schema="schema"
        class="space-y-4"
        @submit="submit"
      >
        <UFormField
          label="Loại minh chứng yêu cầu"
          name="evidenceType"
        >
          <USelect
            v-model="state.evidenceType"
            :items="evidenceOptions"
          />
        </UFormField>

        <UFormField
          label="Số lượng tiêu chí con tối thiểu cần đạt"
          name="requiredChildrenCount"
        >
          <UInput
            v-model.number="state.requiredChildrenCount"
            type="number"
            min="0"
          />
        </UFormField>

        <div class="pt-2">
          <UCheckbox
            v-model="state.mandatory"
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
          :disabled="loading"
          @click="emit('update:open', false)"
        />
        <UButton
          color="info"
          icon="i-lucide-save"
          :loading="loading"
          label="Lưu cấu hình"
          @click="submit"
        />
      </div>
    </template>
  </UModal>
</template>

<script setup lang="ts">
import { z } from 'zod'
import type { Criteria, EvidenceType } from '~/types/admin'

const props = defineProps<{
  open: boolean
  criterion: Criteria | null
  loading?: boolean
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  'submit': [form: { mandatory: boolean, requiredChildrenCount: number, evidenceType: EvidenceType }]
}>()

const schema = z.object({
  evidenceType: z.enum(['NONE', 'IMAGE', 'PDF', 'FILE', 'LINK']),
  requiredChildrenCount: z.number().int().min(0),
  mandatory: z.boolean(),
})

const state = reactive({
  evidenceType: 'NONE' as EvidenceType,
  requiredChildrenCount: 0,
  mandatory: true,
})

watch(() => [props.open, props.criterion] as const, () => {
  if (props.criterion) {
    state.evidenceType = props.criterion.evidenceType || 'NONE'
    state.requiredChildrenCount = props.criterion.requiredChildrenCount || 0
    state.mandatory = props.criterion.mandatory ?? true
  }
}, { immediate: true })

const evidenceOptions: Array<{ label: string, value: EvidenceType }> = [
  { label: 'Không yêu cầu minh chứng', value: 'NONE' },
  { label: 'Yêu cầu nộp minh chứng', value: 'FILE' },
]

const submit = () => {
  emit('submit', { ...state })
}
</script>
