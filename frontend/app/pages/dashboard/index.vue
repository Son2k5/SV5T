<template>
  <div>
    <ProfileMissingInfo v-if="profile?.id && !profile?.profileCompleted" />
    <p v-if="profile?.profileCompleted">
      profile competed
    </p>
  </div>
</template>

<script setup lang="ts">
import type { ProfileData } from '~/types/profile'

const { data: profile } = useNuxtData<ProfileData>('current-user')
</script>
