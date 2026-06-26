<script setup lang="ts">
definePageMeta({ middleware: 'require-admin' })
await navigateTo('/admin/evidence', { replace: true })
</script>
