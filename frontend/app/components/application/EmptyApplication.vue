<template>
  <div class="flex flex-col items-center justify-center rounded-2xl border border-dashed border-slate-200 bg-white/50 px-6 py-16 text-center shadow-sm backdrop-blur-md">
    <!-- Icon with micro-animation -->
    <div class="relative flex size-20 items-center justify-center rounded-full bg-slate-50 text-slate-400 ring-4 ring-slate-50/50 transition-all hover:scale-110 hover:text-slate-500 duration-300">
      <UIcon
        name="i-heroicons-inbox-open"
        class="size-10 animate-pulse"
      />
    </div>

    <h3 class="mt-6 text-xl font-extrabold text-slate-800">
      Bạn chưa có hồ sơ nào
    </h3>
    <p class="mx-auto mt-2 max-w-sm text-sm text-slate-500">
      Tạo hồ sơ đầu tiên của bạn để bắt đầu khai báo và nộp minh chứng xét chọn danh hiệu.
    </p>

    <div class="mt-6">
      <UButton
        icon="i-heroicons-plus"
        size="lg"
        label="Tạo hồ sơ"
        class="cursor-pointer font-bold px-6 shadow-md hover:shadow-lg transition-all duration-300 rounded-xl bg-gradient-to-r from-[#075EA8] to-[#2563EB]"
        @click="$emit('create')"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
defineEmits<{
  create: []
}>()
</script>
