// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  modules: [
    '@nuxt/eslint',
    '@nuxt/ui',
    '@nuxt/image',
    '@vueuse/nuxt',
  ],
  devtools: { enabled: true },
  app: {
    head: {
      title: 'Sinh Viên 5 Tốt - HANU',
      link: [
        { rel: 'icon', type: 'image/x-icon', href: '/favicon.ico', id: 'favicon' },
      ],
      meta: [
        { property: 'image', content: '/bannerAuth.png' },
        { property: 'og:image', content: '/bannerAuth.png' },
        { property: 'og:image:width', content: '1200' },
        { property: 'og:image:height', content: '630' },
      ],
    },
  },
  css: ['~/assets/css/main.css'],
  ui: {
    colorMode: false,
  },
  runtimeConfig: {
    public: {
      apiBaseUrl: process.env.NUXT_PUBLIC_API_BASE_URL || 'http://localhost:8080',
    },
  },
  compatibilityDate: '2025-07-15',
  vite: {
    cacheDir: '.vite',
  },
  eslint: {
    config: {
      stylistic: true,
    },
  },
})
