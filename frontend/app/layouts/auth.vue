<template>
  <div class="bg-white">
    <div class="hidden md:flex h-screen overflow-hidden ">
      <div class="flex z-1 min-w-90 xl:min-w-130 justify-center items-center bg-white rounded-r-xl relative">
        <div class="absolute top-6 w-max flex justify-center gap-2">
          <NuxtImg
            v-for="(logo, idx) in LOGOS"
            :key="idx"
            class="h-16 w-full"
            :src="logo"
            format="webp"
            quality="70"
            alt="logo"
          />
        </div>
        <slot />
      </div>
      <div class="size-full">
        <NuxtImg
          :src="BANNER_AUTH"
          class="object-cover size-full"
          format="webp"
          quality="100"
          alt="banner"
        />
      </div>
    </div>
    <div class="flex md:hidden justify-center items-center h-screen">
      <div class="flex w-full h-full justify-center items-center relative">
        <div class="absolute top-6 w-max flex justify-center gap-2">
          <NuxtImg
            v-for="(logo, idx) in LOGOS"
            :key="idx"
            class="h-16 w-full"
            :src="logo"
            format="webp"
            quality="70"
            alt="logo"
          />
        </div>
        <slot />
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
