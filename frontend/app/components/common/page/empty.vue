<template>
  <CommonPageSection inner-class="flex flex-col justify-center items-center gap-6 py-10">
    <h1 class="text-3xl font-bold">
      {{ title }}
    </h1>
    <p class="text-xl">
      {{ desc }}
    </p>
    <slot />
  </CommonPageSection>
</template>

<script lang="ts" setup>
withDefaults(defineProps<{
  title?: string
  desc?: string
}>(), {
  title: 'Không có dữ liệu',
  desc: 'Vui lòng thử lại sau!',
})
</script>
