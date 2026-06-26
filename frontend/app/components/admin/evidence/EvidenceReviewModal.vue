<template>
  <UModal
    :open="open"
    title="Xét duyệt minh chứng"
    scrollable
    @update:open="emit('update:open', $event)"
  >
    <template #body>
      <div
        v-if="evidence"
        class="space-y-5"
      >
        <div class="grid gap-3 rounded-2xl bg-[#F8FAFC] p-4 text-sm sm:grid-cols-2">
          <div>
            <p class="text-[#64748B]">
              Sinh viên
            </p>
            <p class="mt-1 font-semibold text-[#1E293B]">
              {{ evidence.studentName || evidence.userEmail }}
            </p>
          </div>
          <div>
            <p class="text-[#64748B]">
              Tiêu chí
            </p>
            <p class="mt-1 font-semibold text-[#1E293B]">
              {{ evidence.criteriaName }}
            </p>
          </div>
          <div class="sm:col-span-2">
            <p class="text-[#64748B]">
              Đợt xét chọn
            </p>
            <p class="mt-1 font-semibold text-[#1E293B]">
              {{ evidence.campaignName }}
            </p>
          </div>
        </div>

        <div class="overflow-hidden rounded-2xl border border-[#E5E7EB] bg-slate-50">
          <img
            v-if="isImage"
            :src="evidence.evidenceUrl"
            :alt="`Minh chứng của ${evidence.studentName || evidence.userEmail}`"
            class="max-h-100 w-full object-contain"
          >
          <iframe
            v-else-if="isPdf"
            :src="evidence.evidenceUrl"
            title="Xem trước minh chứng PDF"
            class="h-100 w-full bg-white"
          />
          <div
            v-else
            class="flex min-h-48 flex-col items-center justify-center gap-3 p-6 text-center"
          >
            <UIcon
              name="i-lucide-file"
              class="size-10 text-[#60A5FA]"
            />
            <a
              :href="evidence.evidenceUrl"
              target="_blank"
              rel="noopener noreferrer"
              class="font-semibold text-[#2563EB] hover:underline"
            >
              Mở tệp minh chứng
            </a>
          </div>
        </div>

        <UFormField
          label="Nhận xét"
          name="reviewerComment"
          :error="error"
        >
          <UTextarea
            v-model="comment"
            :rows="4"
            placeholder="Nhập nhận xét hoặc lý do từ chối"
          />
        </UFormField>
      </div>
    </template>
    <template #footer>
      <div class="flex w-full flex-col-reverse gap-2 sm:flex-row sm:justify-end">
        <UButton
          color="neutral"
          variant="outline"
          label="Đóng"
          :disabled="loading"
          @click="emit('update:open', false)"
        />
        <UButton
          color="error"
          variant="soft"
          label="Từ chối"
          :loading="loading"
          @click="review('REJECTED')"
        />
        <UButton
          color="success"
          label="Duyệt minh chứng"
          :loading="loading"
          @click="review('APPROVED')"
        />
      </div>
    </template>
  </UModal>
</template>

<script setup lang="ts">
import type { Evidence, EvidenceStatus } from '~/types/admin'

const props = defineProps<{ open: boolean, evidence: Evidence | null, loading?: boolean }>()
const emit = defineEmits<{
  'update:open': [value: boolean]
  'review': [status: EvidenceStatus, comment: string]
}>()

const comment = ref('')
const error = ref('')

const isImage = computed(() => {
  const source = `${props.evidence?.evidenceFormat || ''} ${props.evidence?.evidenceUrl || ''}`.toLowerCase()
  return /png|jpe?g|gif|webp/.test(source)
})
const isPdf = computed(() => `${props.evidence?.evidenceFormat || ''} ${props.evidence?.evidenceUrl || ''}`.toLowerCase().includes('pdf'))

watch(() => [props.open, props.evidence?.publicId] as const, () => {
  comment.value = props.evidence?.reviewerComment || ''
  error.value = ''
}, { immediate: true })

const review = (status: EvidenceStatus) => {
  if (status === 'REJECTED' && !comment.value.trim()) {
    error.value = 'Vui lòng nhập lý do từ chối.'
    return
  }
  error.value = ''
  emit('review', status, comment.value.trim())
}
</script>
