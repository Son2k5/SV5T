<template>
  <div class="group relative w-full">
    <slot />
    <div
      class="pointer-events-none absolute right-0 bottom-[130%] z-[99] w-[80%] rounded-2xl border border-[#E5E7EB] bg-white text-[#334155] opacity-0 shadow-[0_18px_48px_rgba(15,23,42,0.14)] transition duration-200 group-focus-within:opacity-100"
    >
      <div class="absolute -bottom-2 left-4 z-[98] size-4 rotate-45 rounded-sm border-r border-b border-[#E5E7EB] bg-white" />
      <div class="relative z-[99] w-full rounded-2xl bg-white p-3 text-sm leading-6">
        <slot name="content" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

</script>
