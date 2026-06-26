<template>
  <UModal
    :open="open"
    :title="campaign ? 'Cập nhật đợt xét duyệt' : 'Tạo đợt xét duyệt'"
    scrollable
    @update:open="emit('update:open', $event)"
  >
    <template #body>
      <UForm
        :state="state"
        :schema="schema"
        class="grid gap-4 sm:grid-cols-2"
        @submit="submit"
      >
        <UFormField
          label="Tên đợt xét duyệt"
          name="name"
          class="sm:col-span-2"
        >
          <UInput
            v-model="state.name"
            placeholder="Ví dụ: Đợt xét duyệt Sinh viên 5 Tốt 2026"
          />
        </UFormField>

        <UFormField
          label="Mô tả"
          name="description"
          class="sm:col-span-2"
        >
          <UTextarea
            v-model="state.description"
            :rows="4"
            placeholder="Mô tả mục tiêu và phạm vi của đợt xét duyệt"
          />
        </UFormField>

        <UFormField
          label="Năm học"
          name="academicYear"
        >
          <UInput
            v-model.number="state.academicYear"
            type="number"
            min="2020"
            max="2100"
          />
        </UFormField>

        <UFormField
          label="Cấp xét duyệt"
          name="level"
        >
          <USelect
            v-model="state.level"
            :items="levelOptions"
            class="w-55"
          />
        </UFormField>

        <UFormField
          label="Đối tượng xét duyệt"
          name="isGroup"
        >
          <USelect
            v-model="state.isGroup"
            :items="isGroupOptions"
          />
        </UFormField>

        <UFormField
          label="Ngày bắt đầu"
          name="startDate"
        >
          <UInput
            v-model="state.startDate"
            type="date"
          />
        </UFormField>

        <UFormField
          label="Ngày kết thúc"
          name="endDate"
        >
          <UInput
            v-model="state.endDate"
            type="date"
          />
        </UFormField>

        <UFormField
          label="Trạng thái"
          name="status"
        >
          <USelect
            v-model="state.status"
            :items="statusOptions"
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
          :disabled="loading"
          @click="emit('update:open', false)"
        />
        <UButton
          color="info"
          icon="i-lucide-save"
          :loading="loading"
          :label="campaign ? 'Lưu thay đổi' : 'Tạo đợt xét duyệt'"
          @click="submit"
        />
      </div>
    </template>
  </UModal>
</template>

<script setup lang="ts">
import { z } from 'zod'
import type {
  Campaign,
  CampaignForm,
  CampaignLevel,
  CampaignStatus,
} from '~/types/admin'

const props = defineProps<{
  open: boolean
  campaign?: Campaign | null
  loading?: boolean
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  'submit': [form: CampaignForm]
}>()

const currentYear = new Date().getFullYear()

const schema = z.object({
  name: z.string().trim().min(3, 'Tên đợt phải có ít nhất 3 ký tự.').max(150),
  description: z.string().max(2000, 'Mô tả tối đa 2.000 ký tự.'),
  academicYear: z.number().int().min(2020).max(2100),
  level: z.enum(['UNIVERSITY', 'CITY', 'NATION', 'UNI_CITY', 'UNI_NATION', 'CITY_NATION', 'ALL']),
  startDate: z.string().min(1, 'Vui lòng chọn ngày bắt đầu.'),
  endDate: z.string().min(1, 'Vui lòng chọn ngày kết thúc.'),
  status: z.enum(['DRAFT', 'ACTIVE', 'CLOSED', 'ARCHIVED']),
  isGroup: z.any(),
}).refine(
  value => value.endDate >= value.startDate,
  { path: ['endDate'], message: 'Ngày kết thúc phải sau ngày bắt đầu.' },
)

const blank = (): CampaignForm => ({
  name: '',
  description: '',
  academicYear: currentYear,
  level: 'UNIVERSITY',
  startDate: '',
  endDate: '',
  status: 'DRAFT',
  isGroup: 'INDIVIDUAL',
})

const state = reactive<CampaignForm>(blank())

watch(() => [props.open, props.campaign] as const, () => {
  Object.assign(state, props.campaign
    ? {
        name: props.campaign.name,
        description: props.campaign.description || '',
        academicYear: props.campaign.academicYear,
        level: props.campaign.level,
        startDate: props.campaign.startDate || '',
        endDate: props.campaign.endDate || '',
        status: props.campaign.status,
        isGroup: props.campaign.isGroup || 'INDIVIDUAL',
      }
    : blank())
}, { immediate: true })

const levelOptions: Array<{ label: string, value: CampaignLevel }> = [
  { label: 'Cấp trường', value: 'UNIVERSITY' },
  { label: 'Cấp thành phố', value: 'CITY' },
  { label: 'Cấp trung ương', value: 'NATION' },
  { label: 'Trường & Thành phố', value: 'UNI_CITY' },
  { label: 'Trường & Trung ương', value: 'UNI_NATION' },
  { label: 'Thành phố & Trung ương', value: 'CITY_NATION' },
  { label: 'Cả ba cấp', value: 'ALL' },
]

const isGroupOptions = [
  { label: 'Xét duyệt cá nhân', value: 'INDIVIDUAL' },
  { label: 'Xét duyệt tập thể', value: 'GROUP' },
  { label: 'Cả hai (Cá nhân & Tập thể)', value: 'BOTH' },
]

const statusOptions: Array<{ label: string, value: CampaignStatus }> = [
  { label: 'Nháp', value: 'DRAFT' },
  { label: 'Đang mở xét duyệt', value: 'ACTIVE' },
  { label: 'Đã đóng', value: 'CLOSED' },
  { label: 'Lưu trữ', value: 'ARCHIVED' },
]

const submit = () => {
  emit('submit', {
    ...state,
    name: state.name.trim(),
    description: state.description.trim(),
    isGroup: state.isGroup,
  })
}
</script>
