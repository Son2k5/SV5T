<template>
  <div />
</template>

<script setup lang="ts">
definePageMeta({
  layout: false,
  middleware: async () => {
    const { ensureAccessToken, hasIdleExpired, logOut } = useAuth()

    if (import.meta.client && hasIdleExpired()) {
      await logOut({ redirect: false, reason: 'idle' })
      return navigateTo('/login?error=session_idle')
    }

    if (await ensureAccessToken()) return navigateTo('/dashboard')
    return navigateTo('/login')
  },
})
</script>
