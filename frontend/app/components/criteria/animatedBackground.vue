<template>
  <div class="relative flex h-96 w-full items-center justify-center overflow-hidden rounded-3xl bg-gradient-to-br from-[#3B82F6] via-[#60A5FA] to-[#93C5FD] shadow-[0_24px_70px_rgba(37,99,235,0.22)]">
    <div class="absolute inset-0 bg-[radial-gradient(circle_at_20%_20%,rgba(255,255,255,0.28),transparent_28%),radial-gradient(circle_at_80%_10%,rgba(255,255,255,0.18),transparent_22%)]" />
    <div class="absolute -inset-0 size-full select-none opacity-40 animate-background-pan">
      <div
        v-for="row in 5"
        :key="row"
        class="flex gap-[124px]"
        :class="row % 2 != 0 && 'ml-[96px] bg-red-0'"
      >
        <span
          v-for="icon in 10"
          :key="icon"
          class="text-[96px] text-white/30"
        > + </span>
      </div>
    </div>
    <div class="z-2 mx-4 flex flex-col items-center gap-4 rounded-3xl border border-white/40 bg-white/85 px-8 py-8 text-center font-bold shadow-[0_24px_70px_rgba(15,23,42,0.16)] backdrop-blur md:px-16">
      <h1 class="text-sm uppercase tracking-[0.25em] text-[#2563EB] md:text-base">
        HỘI SINH VIÊN TRƯỜNG ĐẠI HỌC HÀ NỘI
      </h1>
      <h1 class="text-2xl text-[#1E293B]">
        XÉT CHỌN DANH HIỆU
      </h1>
      <h1 class="text-4xl text-[#3B82F6] lg:text-5xl">
        "SINH VIÊN 5 TỐT"
      </h1>
      <UBadge
        class="rounded-full px-4 py-1 text-sm"
        color="neutral"
        label="Năm học 2025 - 2026"
        variant="subtle"
      />
    </div>
  </div>
</template>

<style scoped>
@keyframes diagonal-pan {
    0% {
        transform: translate(-380px, -288px)
    }
    100% {
        transform: translate(0, 0);
    }
}

.animate-background-pan {
    animation: diagonal-pan 7s linear infinite
}
</style>
