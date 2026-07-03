<template>
  <div class="mt-6 flex justify-end border-t border-[#E5E7EB] pt-5">
    <UButton
      color="info"
      icon="i-lucide-save"
      label="Lưu cấu hình"
      :loading="loading"
      @click="emit('save')"
    />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  loading?: boolean
}>()

const emit = defineEmits<{
  save: []
}>()
</script>
