<script setup lang="ts">
definePageMeta({ middleware: 'require-admin' })
await navigateTo('/admin/dashboard', { replace: true })
</script>
