<template>
  <div class="flex items-center gap-2 mb-4">
    <div
      v-if="icon"
      class="flex size-8 items-center justify-center rounded-lg bg-slate-50 text-slate-500 border border-slate-200/60 shadow-2xs"
    >
      <UIcon
        :name="icon"
        class="size-4"
      />
    </div>
    <h2 class="text-base font-bold text-slate-800 tracking-tight">
      {{ title }}
    </h2>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  icon?: string
}>()
</script>
