<template>
  <div
    class="sv-card w-full flex flex-col overflow-hidden"
    :class="props.containerClass"
  >
    <div
      v-if="title"
      class="flex items-center gap-3 border-b border-[#E5E7EB] bg-white px-5 py-4"
    >
      <div
        v-if="titleIcon"
        class="sv-icon-tile"
      >
        <UIcon
          :name="titleIcon"
          class="size-5"
        />
      </div>
      <h2 class="text-xl font-bold text-[#1E293B]">
        {{ title }}
      </h2>
      <slot name="title" />
    </div>
    <div
      class="w-full flex items-center p-5"
      :class="props.innerClass"
    >
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  containerClass?: string
  innerClass?: string
  title?: string
  titleIcon?: string
}>()
</script>
