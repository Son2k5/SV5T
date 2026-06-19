<template>
  <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
    <div
      v-for="(section, idx) in sections"
      :key="idx"
      class="sv-card sv-card-hover group flex w-full items-center gap-4 p-5"
    >
      <div class="flex size-12 shrink-0 items-center justify-center rounded-2xl bg-gradient-to-br from-blue-50 to-sky-100 text-[#3B82F6] transition duration-200 group-hover:scale-105">
        <UIcon
          class="size-6"
          :name="section.icon"
        />
      </div>
      <div class="min-w-0">
        <p class="truncate text-xs font-semibold uppercase tracking-wide text-[#64748B]">
          {{ section.label }}
        </p>
        <h3 class="mt-1 text-2xl font-bold text-[#2563EB]">
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
