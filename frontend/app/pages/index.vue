<template>
  <div />
</template>

<script setup lang="ts">
definePageMeta({
  layout: false,
  middleware: async () => {
    const { accessToken, refreshAccessToken } = useAuth()
    if (accessToken.value || await refreshAccessToken()) return navigateTo('/dashboard')
    return navigateTo('/login')
  },
})
</script>
