<template>
  <UModal
    :open="open"
    title="Xác nhận thao tác"
    @update:open="emit('update:open', $event)"
  >
    <template #body>
      <div class="space-y-4">
        <div class="flex size-12 items-center justify-center rounded-2xl bg-red-50 text-red-600">
          <UIcon
            name="i-lucide-triangle-alert"
            class="size-6"
          />
        </div>
        <div>
          <h2 class="text-lg font-bold text-[#1E293B]">
            {{ title }}
          </h2>
          <p class="mt-2 text-sm leading-6 text-[#64748B]">
            {{ description }}
          </p>
        </div>
      </div>
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
          color="error"
          :loading="loading"
          :label="confirmLabel"
          @click="emit('confirm')"
        />
      </div>
    </template>
  </UModal>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  open: boolean
  title?: string
  description?: string
  confirmLabel?: string
  loading?: boolean
}>(), {
  title: 'Bạn có chắc chắn?',
  description: 'Thao tác này không thể hoàn tác.',
  confirmLabel: 'Xác nhận',
})

const emit = defineEmits<{
  'update:open': [value: boolean]
  'confirm': []
}>()
</script>
