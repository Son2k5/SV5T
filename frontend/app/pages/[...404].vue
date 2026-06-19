<template>
  <div class="min-h-screen bg-[#F8FAFC] px-4 py-8 text-[#1E293B]">
    <div class="mx-auto flex min-h-[calc(100vh-4rem)] max-w-5xl flex-col items-center justify-center gap-8">
      <div class="flex items-center justify-center gap-3 rounded-2xl border border-[#E5E7EB] bg-white px-5 py-3 shadow-sm">
        <NuxtImg
          v-for="(logo, idx) in LOGOS"
          :key="idx"
          class="h-10 w-auto rounded-lg object-contain"
          :src="logo"
          format="webp"
          quality="70"
          alt="logo"
        />
      </div>

      <div class="w-full overflow-hidden rounded-3xl border border-[#E5E7EB] bg-white p-6 text-center shadow-[0_24px_70px_rgba(15,23,42,0.10)] md:p-10">
        <video
          preload="auto"
          :src="VIDEO_404"
          autoplay
          loop
          muted
          class="mx-auto h-56 w-full max-w-2xl object-contain md:h-80"
        />
        <div class="mx-auto mt-6 max-w-2xl">
          <p class="text-sm font-bold uppercase tracking-[0.18em] text-[#3B82F6]">
            404
          </p>
          <h1 class="mt-3 text-3xl font-bold tracking-tight text-[#1E293B] md:text-4xl">
            Trang bạn đang cố truy cập không tồn tại
          </h1>
          <p class="mt-3 text-sm leading-6 text-[#64748B] md:text-base">
            Đường dẫn có thể đã thay đổi hoặc bạn không có quyền truy cập vào nội dung này.
          </p>
          <UButton
            class="mt-6 h-11 rounded-xl px-8 shadow-sm"
            color="info"
            icon="i-lucide-arrow-left"
            label="Quay về trang chính"
            to="/"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  layout: false,
})

const LOGOS = [
  'logoHanu.jpg',
  'logo5Merits.jpg',
]

const VIDEO_404 = '404.mp4'
</script>
