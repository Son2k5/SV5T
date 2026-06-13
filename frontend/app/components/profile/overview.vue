<template>
  <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 justify-between">
    <div
      v-for="(section, idx) in sections"
      :key="idx"
      class="bg-white w-full rounded-lg shadow-xl flex items-center justify-center px-2 py-6 gap-4"
    >
      <div class="rounded-lg bg-gray-200 size-10 min-w-10 flex items-center justify-center">
        <UIcon
          class="bg-info size-6"
          :name="section.icon"
        />
      </div>
      <div class="w-full">
        <p class="text-sm text-dimmed">
          {{ section.label }}
        </p>
        <h3 class="text-xl font-bold text-info">
          {{ section.desc }}
        </h3>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  sections: {
    label: string
    desc: string
    icon: string
  }[]
}>()
</script>
