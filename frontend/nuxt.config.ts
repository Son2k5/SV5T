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
        { rel: 'preconnect', href: 'https://fonts.googleapis.com' },
        { rel: 'preconnect', href: 'https://fonts.gstatic.com', crossorigin: '' },
        { rel: 'stylesheet', href: 'https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap' },
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
  routeRules: {
    '/**': {
      headers: {
        'X-Frame-Options': 'DENY',
        'X-Content-Type-Options': 'nosniff',
        'Referrer-Policy': 'strict-origin-when-cross-origin',
      },
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
