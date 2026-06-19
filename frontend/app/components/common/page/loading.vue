<template>
  <CommonPageSection inner-class="justify-center items-center size-full min-h-60">
    <div
      class="flex flex-col items-center gap-4"
      :class="spinnerClass"
    >
      <div class="size-14 rounded-full border-4 border-blue-100 border-t-[#3B82F6] animate-spin" />
      <div class="space-y-2">
        <div class="h-3 w-40 rounded-full bg-slate-100" />
        <div class="h-3 w-28 rounded-full bg-slate-100 mx-auto" />
      </div>
    </div>
  </CommonPageSection>
</template>

<script setup lang="ts">
defineProps<{
  spinnerClass?: string
}>()
</script>
