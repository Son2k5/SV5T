<template>
  <div class="group relative w-full">
    <slot />
    <div
      class="absolute z-[99] right-0 w-[80%] bottom-[130%] pointer-events-none opacity-0 group-focus-within:opacity-100 transition
        shadow-xl rounded-lg bg-info-300"
    >
      <div class="absolute -bottom-2 left-4 size-4 bg-info-300 rotate-45 rounded-sm z-[98]" />
      <div class="relative w-full bg-info-300 z-[99] p-2 rounded-lg">
        <slot name="content" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

</script>
