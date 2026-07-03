<template>
  <div class="rounded-xl border border-slate-100 bg-slate-50/50 p-4 mt-3">
    <!-- Header with status badge -->
    <div class="flex flex-wrap items-center justify-between gap-3 border-b border-slate-100 pb-3">
      <div class="flex items-center gap-2">
        <span class="text-xs font-bold text-slate-500 uppercase tracking-wider">Đánh giá của Mentor:</span>
        <span
          :class="[
            'text-xs font-black px-2.5 py-1 rounded-full ring-1',
            statusMeta.class,
          ]"
        >
          {{ statusMeta.label }}
        </span>
      </div>

      <span
        v-if="time"
        class="text-xs font-semibold text-slate-400"
      >
        {{ time }}
      </span>
    </div>

    <!-- Body contents -->
    <div class="mt-4 flex gap-4 items-start">
      <!-- Mentor avatar representation -->
      <div class="size-10 rounded-full bg-slate-200 shrink-0 flex items-center justify-center font-bold text-slate-500 shadow-sm">
        {{ mentorName ? mentorName.charAt(0).toUpperCase() : 'M' }}
      </div>

      <div class="flex-1 min-w-0">
        <p class="text-sm font-extrabold text-slate-800">
          {{ mentorName || 'Mentor Ban cán sự' }}
        </p>
        <p class="text-xs text-slate-400 font-semibold">
          Cố vấn chuyên môn
        </p>

        <!-- Feedback content -->
        <div class="mt-3 text-sm text-slate-600 leading-relaxed font-medium bg-white border border-slate-100 p-3 rounded-lg shadow-sm">
          {{ feedback || 'Chưa có ý kiến nhận xét nào cho tiêu chí này. Vui lòng khai báo thông tin và tải lên minh chứng để được duyệt.' }}
        </div>
      </div>
    </div>

    <!-- Edit and resubmit action if rejected -->
    <div
      v-if="status === 'REJECTED' && !disabled"
      class="mt-4 pt-3 border-t border-slate-100/50 flex justify-end"
    >
      <UButton
        size="xs"
        icon="i-heroicons-arrow-path"
        label="Chỉnh sửa và gửi lại"
        class="cursor-pointer font-bold px-3 rounded-lg bg-red-600 hover:bg-red-700 text-white"
        @click="$emit('edit')"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  status?: 'APPROVED' | 'REJECTED' | 'PENDING' | string | null
  mentorName?: string
  time?: string
  feedback?: string | null
  disabled?: boolean
}>()

defineEmits<{
  edit: []
}>()

const statusMeta = computed(() => {
  if (props.status === 'APPROVED') {
    return { label: 'Đạt', class: 'bg-emerald-50 text-emerald-700 ring-emerald-100' }
  }
  if (props.status === 'REJECTED') {
    return { label: 'Không đạt', class: 'bg-red-50 text-red-700 ring-red-100' }
  }
  return { label: 'Chưa nhận xét', class: 'bg-slate-100 text-slate-500 ring-slate-200' }
})
</script>
