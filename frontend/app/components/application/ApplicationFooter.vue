<template>
  <div class="sticky bottom-0 left-0 right-0 z-40 bg-white/80 border-t border-slate-100 backdrop-blur-md px-6 py-4 shadow-[0_-8px_24px_rgba(15,23,42,0.05)] -mx-8 -mb-8 mt-10">
    <div class="max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-4">
      <!-- Status Info -->
      <div class="flex items-center gap-2">
        <div class="size-2.5 rounded-full bg-[#075EA8] animate-ping" />
        <span class="text-xs font-bold text-slate-500">Chế độ biên tập hồ sơ nháp</span>
      </div>

      <!-- Action Buttons in order of Image 2: [Lưu và nộp (Blue)] [Lưu Nháp (Green)] [Xóa (White)] -->
      <div class="flex items-center gap-3 w-full sm:w-auto justify-end">
        <UButton
          size="md"
          label="Lưu và nộp"
          :loading="isSubmitting"
          class="cursor-pointer font-bold px-5 rounded-xl bg-[#075EA8] hover:bg-[#064f8d] shadow-sm text-white"
          @click="showSubmitModal = true"
        />

        <UButton
          size="md"
          label="Lưu Nháp"
          :loading="isSaving"
          class="cursor-pointer font-bold px-5 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white shadow-sm"
          @click="$emit('save')"
        />

        <UButton
          size="md"
          label="Xóa"
          variant="outline"
          class="cursor-pointer font-bold px-5 rounded-xl border border-slate-200 text-slate-500 bg-white hover:bg-slate-50 transition-all"
          @click="showCancelModal = true"
        />
      </div>
    </div>

    <!-- Confirm Cancel Modal -->
    <UModal
      :open="showCancelModal"
      title="Xác nhận thoát"
      @update:open="showCancelModal = $event"
    >
      <template #body>
        <div class="flex items-start gap-4 mt-2">
          <div class="flex size-12 shrink-0 items-center justify-center rounded-full bg-amber-50 text-amber-600 border border-amber-100">
            <UIcon
              name="i-heroicons-exclamation-triangle"
              class="size-6"
            />
          </div>
          <div>
            <p class="text-sm text-slate-500 leading-normal">
              Các thông tin bạn chưa bấm "Lưu tiêu chí" có thể bị mất. Bạn có chắc chắn muốn rời khỏi trang xét minh chứng này?
            </p>
          </div>
        </div>
      </template>
      <template #footer>
        <div class="flex items-center justify-end gap-3 w-full">
          <UButton
            color="neutral"
            variant="outline"
            label="Quay lại"
            class="cursor-pointer font-bold rounded-xl px-4"
            @click="showCancelModal = false"
          />
          <UButton
            color="error"
            label="Thoát và Hủy"
            class="cursor-pointer font-bold rounded-xl px-5"
            @click="confirmCancel"
          />
        </div>
      </template>
    </UModal>

    <!-- Confirm Submit Modal -->
    <UModal
      :open="showSubmitModal"
      title="Xác nhận gửi hồ sơ"
      @update:open="showSubmitModal = $event"
    >
      <template #body>
        <div class="flex items-start gap-4 mt-2">
          <div class="flex size-12 shrink-0 items-center justify-center rounded-full bg-emerald-50 text-emerald-600 border border-emerald-100">
            <UIcon
              name="i-heroicons-shield-check"
              class="size-6"
            />
          </div>
          <div>
            <p class="text-sm text-slate-500 leading-normal">
              Hệ thống sẽ gửi toàn bộ thông tin kê khai và minh chứng của bạn tới Admin để tổ chức chấm điểm xét duyệt Sinh viên 5 Tốt.
            </p>
            <p class="mt-2 text-xs font-bold text-red-500 bg-red-50 p-2.5 rounded-lg border border-red-100/50">
              Lưu ý: Sau khi gửi đi, bạn sẽ không thể chỉnh sửa thông tin hồ sơ này nữa.
            </p>
          </div>
        </div>
      </template>
      <template #footer>
        <div class="flex items-center justify-end gap-3 w-full">
          <UButton
            color="neutral"
            variant="outline"
            label="Quay lại"
            class="cursor-pointer font-bold rounded-xl px-4"
            @click="showSubmitModal = false"
          />
          <UButton
            color="success"
            label="Xác nhận gửi"
            class="cursor-pointer font-bold rounded-xl px-5 text-white"
            @click="confirmSubmit"
          />
        </div>
      </template>
    </UModal>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

defineProps<{
  isSaving?: boolean
  isSubmitting?: boolean
}>()

const emit = defineEmits<{
  save: []
  cancel: []
  submit: []
}>()

const showCancelModal = ref(false)
const showSubmitModal = ref(false)

const confirmCancel = () => {
  showCancelModal.value = false
  emit('cancel')
}

const confirmSubmit = () => {
  showSubmitModal.value = false
  emit('submit')
}
</script>
