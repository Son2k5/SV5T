<template>
  <CommonPageSection inner-class="flex flex-col justify-center items-center gap-4 py-16 text-center">
    <div class="sv-icon-tile size-14">
      <UIcon
        name="i-heroicons-inbox"
        class="size-7"
      />
    </div>
    <h1 class="text-2xl font-bold text-[#1E293B]">
      {{ title }}
    </h1>
    <p class="max-w-md text-base text-[#64748B]">
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
