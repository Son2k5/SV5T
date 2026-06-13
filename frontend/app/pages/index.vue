<template>
  <div />
</template>

<script setup lang="ts">
definePageMeta({
  layout: false,
  middleware: () => {
    const { accessToken } = useAuth()
    if (accessToken) return navigateTo('/dashboard')
    return navigateTo('/login')
  },
})
</script>
