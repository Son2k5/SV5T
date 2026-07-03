<template>
  <div
    v-if="maintenanceEnabled"
    class="border-b border-amber-200 bg-amber-50 px-3 py-3 text-amber-900"
  >
    <div class="mx-auto flex max-w-[1440px] items-start gap-3 text-sm font-medium">
      <UIcon
        name="i-lucide-construction"
        class="mt-0.5 size-5 shrink-0 text-amber-600"
      />
      <p class="leading-6">
        {{ maintenanceMessage }}
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
const { maintenanceEnabled, maintenanceMessage } = await usePublicSystemSettings()
</script>
