<template>
  <div class="sv-card p-6 md:p-10 flex flex-col items-center">
    <div class="w-full max-w-4xl">
      <!-- Horizontal Timeline -->
      <div class="relative flex flex-col items-center justify-between md:flex-row gap-8 md:gap-4 my-6">
        <!-- Connecting Line background (Desktop) -->
        <div class="absolute top-[32px] left-[10%] right-[10%] hidden h-[3px] bg-slate-100 md:block z-0" />

        <!-- Active colored progress lines (Desktop) -->
        <div class="absolute top-[32px] left-[10%] w-[26%] hidden h-[3px] bg-[#2563EB] md:block z-0" />
        <div class="absolute top-[32px] left-[37%] w-[26%] hidden h-[3px] bg-[#2563EB] md:block z-0" />
        <div class="absolute top-[32px] left-[63%] w-[26%] hidden h-[3px] bg-[#10B981] md:block z-0" />

        <!-- Step 1 -->
        <div class="relative z-10 flex flex-col items-center text-center group">
          <div class="flex size-16 items-center justify-center rounded-full bg-[#2563EB] text-white shadow-md ring-4 ring-blue-100 transition-transform duration-300 group-hover:scale-110">
            <UIcon
              name="i-heroicons-plus"
              class="size-8"
            />
          </div>
          <span class="mt-3 text-sm font-extrabold text-slate-700">1. Chọn Tiêu Chí</span>
        </div>

        <!-- Step 2 -->
        <div class="relative z-10 flex flex-col items-center text-center group">
          <div class="flex size-16 items-center justify-center rounded-full bg-[#2563EB] text-white shadow-md ring-4 ring-blue-100 transition-transform duration-300 group-hover:scale-110">
            <UIcon
              name="i-heroicons-user-plus"
              class="size-7"
            />
          </div>
          <span class="mt-3 text-sm font-extrabold text-slate-700">2. Thêm Minh Chứng</span>
        </div>

        <!-- Step 3 -->
        <div class="relative z-10 flex flex-col items-center text-center group">
          <div class="flex size-16 items-center justify-center rounded-full bg-[#2563EB] text-white shadow-md ring-4 ring-blue-100 transition-transform duration-300 group-hover:scale-110">
            <UIcon
              name="i-heroicons-paper-airplane"
              class="size-7"
            />
          </div>
          <span class="mt-3 text-sm font-extrabold text-slate-700">3. Xác nhận</span>
        </div>

        <!-- Step 4 -->
        <div class="relative z-10 flex flex-col items-center text-center group">
          <div class="flex size-16 items-center justify-center rounded-full bg-[#10B981] text-white shadow-md ring-4 ring-emerald-100 transition-transform duration-300 group-hover:scale-110">
            <UIcon
              name="i-heroicons-check"
              class="size-8"
            />
          </div>
          <span class="mt-3 text-sm font-extrabold text-slate-700">Hoàn Tất</span>
        </div>
      </div>
    </div>

    <!-- Action button at bottom -->
    <div class="mt-8">
      <UButton
        icon="i-heroicons-plus-circle"
        size="lg"
        label="Lập hồ sơ ngay"
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
