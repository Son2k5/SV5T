<template>
  <div class="min-h-screen bg-[#F8FAFC]">
    <CommonMaintenanceBanner />
    <div class="hidden min-h-screen overflow-hidden md:grid md:grid-cols-[minmax(380px,460px)_1fr]">
      <div class="relative z-1 flex items-center justify-center border-r border-[#E5E7EB] bg-white px-8">
        <div class="absolute top-8 flex w-max justify-center gap-3">
          <NuxtImg
            v-for="(logo, idx) in LOGOS"
            :key="idx"
            class="h-14 w-auto rounded-2xl shadow-sm"
            :src="logo"
            format="webp"
            quality="70"
            alt="logo"
          />
        </div>
        <div class="w-full max-w-sm rounded-3xl border border-[#E5E7EB] bg-white p-8 shadow-[0_24px_70px_rgba(15,23,42,0.08)]">
          <slot />
        </div>
      </div>
      <div class="relative size-full overflow-hidden">
        <NuxtImg
          :src="BANNER_AUTH"
          class="size-full object-cover"
          format="webp"
          quality="100"
          alt="banner"
        />
        <div class="absolute inset-0 bg-gradient-to-br from-[#3B82F6]/65 via-[#60A5FA]/30 to-slate-950/40" />
        <div class="absolute bottom-12 left-12 max-w-xl text-white">
          <p class="mb-3 text-sm font-semibold uppercase tracking-[0.3em] text-blue-100">
            Sinh viên 5 Tốt
          </p>
          <h1 class="text-5xl font-bold leading-tight">
            Quản lý hồ sơ, minh chứng và tiến độ trong một không gian hiện đại.
          </h1>
        </div>
      </div>
    </div>
    <div class="flex min-h-screen items-center justify-center p-5 md:hidden">
      <div class="relative flex w-full justify-center">
        <div class="absolute -top-24 flex w-max justify-center gap-3">
          <NuxtImg
            v-for="(logo, idx) in LOGOS"
            :key="idx"
            class="h-14 w-auto rounded-2xl shadow-sm"
            :src="logo"
            format="webp"
            quality="70"
            alt="logo"
          />
        </div>
        <div class="w-full max-w-sm rounded-3xl border border-[#E5E7EB] bg-white p-6 shadow-[0_24px_70px_rgba(15,23,42,0.10)]">
          <slot />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const LOGOS = [
  'logoHanu.jpg',
  'logo5Merits.jpg',
]

const BANNER_AUTH = 'bannerAuth.png'
</script>
