<template>
  <UModal
    :open="open"
    title="Chế độ minh chứng"
    @update:open="emit('update:open', $event)"
  >
    <template #body>
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-5 mt-2">
        <!-- Card 1: Individual -->
        <button
          type="button"
          :class="[
            'flex flex-col items-center text-center p-6 rounded-2xl border transition-all duration-300 cursor-pointer w-full focus:outline-none',
            selectedMode === 'individual'
              ? 'border-blue-500 bg-blue-50/50 shadow-md scale-[1.02] ring-2 ring-blue-100'
              : 'border-slate-100 bg-white hover:border-slate-200 hover:shadow-md hover:scale-[1.01]',
          ]"
          @click="selectedMode = 'individual'"
        >
          <div class="flex size-16 items-center justify-center rounded-full bg-blue-100 text-blue-600 shadow-sm transition-transform duration-300">
            <UIcon
              name="i-heroicons-user"
              class="size-8"
            />
          </div>
          <h4 class="mt-4 text-base font-extrabold text-slate-800">
            Danh hiệu cá nhân
          </h4>
          <p class="mt-2 text-[11px] text-slate-500 leading-normal">
            Xét danh hiệu đối với từng cá nhân sinh viên tự lập hồ sơ.
          </p>
        </button>

        <!-- Card 2: Group -->
        <button
          type="button"
          :class="[
            'flex flex-col items-center text-center p-6 rounded-2xl border transition-all duration-300 cursor-pointer w-full focus:outline-none',
            selectedMode === 'group'
              ? 'border-emerald-500 bg-emerald-50/50 shadow-md scale-[1.02] ring-2 ring-emerald-100'
              : 'border-slate-100 bg-white hover:border-slate-200 hover:shadow-md hover:scale-[1.01]',
          ]"
          @click="selectedMode = 'group'"
        >
          <div class="flex size-16 items-center justify-center rounded-full bg-emerald-100 text-emerald-600 shadow-sm transition-transform duration-300">
            <UIcon
              name="i-heroicons-users"
              class="size-8"
            />
          </div>
          <h4 class="mt-4 text-base font-extrabold text-slate-800">
            Danh hiệu tập thể
          </h4>
          <p class="mt-2 text-[11px] text-slate-500 leading-normal">
            Xét danh hiệu dưới dạng nhóm, lớp học hoặc đơn vị liên kết.
          </p>
        </button>
      </div>
    </template>

    <template #footer>
      <div class="flex items-center justify-end gap-3 w-full">
        <UButton
          color="neutral"
          variant="outline"
          label="Hủy"
          class="cursor-pointer font-bold px-4 rounded-xl"
          @click="handleCancel"
        />
        <UButton
          label="Tiếp tục"
          class="cursor-pointer font-bold px-5 rounded-xl bg-[#075EA8] hover:bg-[#064f8d] text-white shadow-sm"
          @click="handleConfirm"
        />
      </div>
    </template>
  </UModal>
</template>

<script setup lang="ts">
import { ref } from 'vue'

defineProps<{
  open: boolean
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  'confirm': [mode: 'individual' | 'group']
}>()

const selectedMode = ref<'individual' | 'group'>('individual')

const handleCancel = () => {
  emit('update:open', false)
}

const handleConfirm = () => {
  emit('confirm', selectedMode.value)
  emit('update:open', false)
}
</script>
