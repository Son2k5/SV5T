<template>
  <CommonPageSection inner-class="justify-center items-center size-full">
    <UButton
      color="info"
      loading
      variant="ghost"
      :class="spinnerClass"
      :ui="{
        leadingIcon: 'size-20',
      }"
    />
  </CommonPageSection>
</template>

<script setup lang="ts">
defineProps<{
  spinnerClass?: string
}>()
</script>
