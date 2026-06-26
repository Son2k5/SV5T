<template>
  <UBadge
    :color="badgeMeta.color"
    :variant="variant || 'soft'"
    :size="size || 'sm'"
    class="rounded-full font-semibold px-2.5 py-0.5 whitespace-nowrap transition-all"
  >
    <template v-if="showIcon && badgeMeta.icon">
      <UIcon
        :name="badgeMeta.icon"
        class="mr-1 size-3.5 shrink-0"
      />
    </template>
    {{ badgeMeta.label }}
  </UBadge>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  status?: string | null
  variant?: 'solid' | 'outline' | 'soft' | 'subtle'
  size?: 'xs' | 'sm' | 'md' | 'lg'
  showIcon?: boolean
}>()

const badgeMeta = computed(() => {
  const s = props.status?.toUpperCase() || 'NOT_SUBMITTED'
  switch (s) {
    case 'APPROVED':
    case 'PASS':
      return {
        label: 'Đã duyệt',
        color: 'success' as const,
        icon: 'i-lucide-check-circle-2',
      }
    case 'REJECTED':
    case 'FAIL':
      return {
        label: 'Từ chối',
        color: 'error' as const,
        icon: 'i-lucide-x-circle',
      }
    case 'PENDING':
    case 'SUBMITTED':
      return {
        label: 'Chờ duyệt',
        color: 'warning' as const,
        icon: 'i-lucide-clock',
      }
    case 'NONE':
      return {
        label: 'Không cần nộp',
        color: 'neutral' as const,
        icon: 'i-lucide-minus-circle',
      }
    case 'NOT_SUBMITTED':
    default:
      return {
        label: 'Chưa nộp',
        color: 'neutral' as const,
        icon: 'i-lucide-help-circle',
      }
  }
})
</script>
