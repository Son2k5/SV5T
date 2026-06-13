<template>
  <div
    class="w-full bg-white rounded-lg shadow-xl flex flex-col"
    :class="props.containerClass"
  >
    <div
      v-if="title"
      class="bg-info-300 rounded-t-lg p-4 flex items-center gap-2"
    >
      <UIcon
        v-if="titleIcon"
        :name="titleIcon"
        class="bg-white size-6"
      />
      <h2 class="text-white text-2xl font-bold">
        {{ title }}
      </h2>
      <slot name="title" />
    </div>
    <div
      class="p-4 w-full flex items-center"
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
