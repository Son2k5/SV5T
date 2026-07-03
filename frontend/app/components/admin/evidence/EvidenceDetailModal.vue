<template>
  <UModal
    :open="open"
    title="Đánh giá chi tiết minh chứng"
    description="Xem nội dung minh chứng và thực hiện xét duyệt."
    size="md"
    :close-button="{ icon: 'i-lucide-x', color: 'neutral', variant: 'ghost' }"
    @update:open="emit('update:open', $event)"
  >
    <template #body>
      <div
        v-if="!criteria || !standard"
        class="py-12 text-center text-sm text-[#64748B]"
      >
        Không tìm thấy thông tin minh chứng.
      </div>

      <div
        v-else
        class="space-y-5"
      >
        <!-- Evidence Metadata -->
        <div class="grid gap-3 text-xs sm:grid-cols-2 bg-slate-50 p-4 rounded-xl border border-slate-100">
          <div>
            <p class="text-[#64748B] font-medium">
              Tiêu chuẩn
            </p>
            <p class="mt-0.5 font-semibold text-[#1E293B]">
              {{ standard.name }}
            </p>
          </div>
          <div>
            <p class="text-[#64748B] font-medium">
              Tiêu chí
            </p>
            <p class="mt-0.5 font-semibold text-[#1E293B]">
              {{ criteria.name }}
            </p>
          </div>
          <div>
            <p class="text-[#64748B] font-medium">
              Loại yêu cầu
            </p>
            <p class="mt-0.5 font-semibold text-[#1E293B]">
              {{ criteria.evidenceType || 'N/A' }}
            </p>
          </div>
          <div>
            <p class="text-[#64748B] font-medium">
              Trạng thái hiện tại
            </p>
            <div class="mt-0.5">
              <UBadge
                :color="statusMeta.color"
                variant="soft"
                class="rounded-full"
              >
                {{ statusMeta.label }}
              </UBadge>
            </div>
          </div>
          <div
            v-if="criteria.evidenceUrl"
            class="sm:col-span-2 border-t border-slate-200/50 pt-2 break-all"
          >
            <p class="text-[#64748B] font-medium">
              Đường dẫn tệp
            </p>
            <a
              :href="criteria.evidenceUrl"
              target="_blank"
              rel="noopener noreferrer"
              class="mt-0.5 inline-flex items-center gap-1 font-mono font-medium text-blue-600 hover:underline"
            >
              {{ criteria.evidenceUrl }}
              <UIcon
                name="i-lucide-external-link"
                class="size-3 shrink-0"
              />
            </a>
          </div>
        </div>

        <!-- Evidence Content Preview -->
        <div class="border border-[#E5E7EB] rounded-2xl overflow-hidden bg-slate-900/5 min-h-[180px] flex flex-col justify-center items-center p-2">
          <!-- IMAGE Preview -->
          <div
            v-if="detectedType === 'IMAGE'"
            class="w-full flex justify-center"
          >
            <img
              :src="criteria.evidenceUrl ?? undefined"
              alt="Minh chứng ảnh"
              class="max-h-[350px] object-contain rounded-xl shadow-sm border border-slate-200/40 bg-white"
            >
          </div>

          <!-- PDF Preview -->
          <div
            v-else-if="detectedType === 'PDF'"
            class="w-full h-[350px] flex flex-col"
          >
            <iframe
              :src="criteria.evidenceUrl ?? undefined"
              class="w-full h-full border-0 rounded-xl"
              title="PDF Minh chứng"
            />
            <div class="mt-2 text-center">
              <UButton
                color="info"
                variant="subtle"
                icon="i-lucide-download"
                label="Tải xuống PDF"
                size="xs"
                :to="criteria.evidenceUrl ?? undefined"
                target="_blank"
              />
            </div>
          </div>

          <!-- LINK Preview -->
          <div
            v-else-if="detectedType === 'LINK'"
            class="py-8 px-4 text-center"
          >
            <div class="mx-auto flex size-12 items-center justify-center rounded-full bg-blue-100 text-[#2563EB]">
              <UIcon
                name="i-lucide-link"
                class="size-6"
              />
            </div>
            <p class="mt-3 text-sm font-semibold text-[#1E293B]">
              Minh chứng là đường dẫn liên kết
            </p>
            <p class="mt-1 text-xs text-[#64748B]">
              Bấm nút bên dưới để mở liên kết trong tab mới.
            </p>
            <UButton
              color="info"
              icon="i-lucide-external-link"
              label="Mở liên kết minh chứng"
              class="mt-4 rounded-xl"
              :to="criteria.evidenceUrl ?? undefined"
              target="_blank"
            />
          </div>

          <!-- TEXT Preview -->
          <div
            v-else-if="detectedType === 'TEXT'"
            class="w-full max-h-[300px] overflow-y-auto bg-slate-950 text-slate-200 font-mono text-xs p-4 rounded-xl text-left whitespace-pre-wrap"
          >
            {{ criteria.evidenceUrl }}
          </div>

          <!-- FILE/FALLBACK Preview -->
          <div
            v-else
            class="py-8 px-4 text-center"
          >
            <div class="mx-auto flex size-12 items-center justify-center rounded-full bg-slate-100 text-slate-600">
              <UIcon
                name="i-lucide-file"
                class="size-6"
              />
            </div>
            <p class="mt-3 text-sm font-semibold text-[#1E293B]">
              Tệp đính kèm minh chứng
            </p>
            <p class="mt-1 text-xs text-[#64748B]">
              Tệp tin tải xuống để xem trực tiếp trên máy tính.
            </p>
            <UButton
              color="info"
              variant="outline"
              icon="i-lucide-download"
              label="Tải tệp minh chứng"
              class="mt-4 rounded-xl"
              :to="criteria.evidenceUrl ?? undefined"
              target="_blank"
            />
          </div>
        </div>

        <!-- Evaluation / Admin Review Comments -->
        <div class="space-y-3 pt-3 border-t border-[#E5E7EB]">
          <h4 class="font-bold text-[#1E293B] text-sm flex items-center gap-1.5">
            <UIcon
              name="i-lucide-clipboard-edit"
              class="text-blue-600"
            />
            Đánh giá của Quản trị viên
          </h4>

          <UFormField
            label="Ý kiến nhận xét"
            name="comment"
          >
            <UTextarea
              v-model="comment"
              placeholder="Nhập ý kiến nhận xét (Ví dụ: Minh chứng hợp lệ, Thiếu thông tin xác nhận, Cần bổ sung thêm...)"
              class="w-full"
              :rows="3"
            />
          </UFormField>

          <!-- Action buttons -->
          <div class="flex flex-wrap gap-2 pt-2 justify-between">
            <UButton
              color="neutral"
              variant="outline"
              icon="i-lucide-save"
              label="Lưu nhận xét"
              :loading="loading"
              @click="handleSaveComment"
            />
            <div class="flex gap-2">
              <UButton
                color="error"
                icon="i-lucide-x-circle"
                label="Từ chối"
                :loading="loading"
                @click="handleReject"
              />
              <UButton
                color="success"
                icon="i-lucide-check-circle"
                label="Chấp thuận"
                :loading="loading"
                @click="handleApprove"
              />
            </div>
          </div>
        </div>
      </div>
    </template>
  </UModal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { CriteriaResultDTO, StandardResultDTO } from '~/types/admin'

const props = defineProps<{
  open: boolean
  criteria: CriteriaResultDTO | null
  standard: StandardResultDTO | null
  loading?: boolean
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  'approve': [data: { criteria: CriteriaResultDTO, comment: string }]
  'reject': [data: { criteria: CriteriaResultDTO, reason: string }]
  'save-comment': [data: { criteria: CriteriaResultDTO, comment: string }]
}>()

const toast = useToast()
const comment = ref('')

// Watch and set initial reviewer comment when criteria changes
watch(
  () => props.criteria,
  (newCriteria) => {
    if (newCriteria) {
      comment.value = newCriteria.reviewerComment || ''
    }
    else {
      comment.value = ''
    }
  },
  { immediate: true },
)

const detectedType = computed(() => {
  if (!props.criteria || !props.criteria.evidenceUrl) return 'NONE'
  const url = props.criteria.evidenceUrl.toLowerCase().trim()

  // If evidenceType is explicit, respect it
  const type = props.criteria.evidenceType?.toUpperCase()
  if (type === 'IMAGE') return 'IMAGE'
  if (type === 'PDF') return 'PDF'
  if (type === 'LINK') return 'LINK'

  // Guess based on URL extensions
  if (url.match(/\.(jpeg|jpg|gif|png|webp|svg)/)) {
    return 'IMAGE'
  }
  if (url.endsWith('.pdf')) {
    return 'PDF'
  }
  if (url.startsWith('http://') || url.startsWith('https://')) {
    // If it's a URL but not an image/pdf, treat as link
    return 'LINK'
  }
  if (url.length > 200) {
    // Very long string without url structure, might be text
    return 'TEXT'
  }
  return 'FILE'
})

const statusMeta = computed(() => {
  if (!props.criteria || !props.criteria.evidenceUrl) {
    return { label: 'Chưa nộp', color: 'neutral' as const }
  }
  switch (props.criteria.evidenceStatus?.toUpperCase()) {
    case 'APPROVED':
      return { label: 'Đã duyệt', color: 'success' as const }
    case 'REJECTED':
      return { label: 'Từ chối', color: 'error' as const }
    case 'PENDING':
    default:
      return { label: 'Chờ duyệt', color: 'warning' as const }
  }
})

function handleSaveComment() {
  if (!props.criteria) return
  emit('save-comment', { criteria: props.criteria, comment: comment.value.trim() })
}

function handleApprove() {
  if (!props.criteria) return
  emit('approve', { criteria: props.criteria, comment: comment.value.trim() })
}

function handleReject() {
  if (!props.criteria) return
  const cleanComment = comment.value.trim()
  if (!cleanComment) {
    toast.add({
      title: 'Lỗi kiểm tra dữ liệu',
      description: 'Vui lòng điền ý kiến nhận xét (lý do từ chối) trước khi bấm Từ chối.',
      color: 'error',
    })
    return
  }
  emit('reject', { criteria: props.criteria, reason: cleanComment })
}
</script>
