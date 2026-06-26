<template>
  <div class="sv-card hover:-translate-y-1 hover:shadow-md duration-300 flex items-start justify-between p-5 group relative overflow-hidden">
    <!-- Background highlight pattern -->
    <div :class="['absolute -right-4 -bottom-4 size-24 rounded-full opacity-5 blur-xl transition-all duration-500 group-hover:scale-125', bgHighlightClass]" />

    <div class="space-y-3 min-w-0">
      <span class="text-xs font-bold text-slate-400 uppercase tracking-wider block truncate">
        {{ label }}
      </span>
      <div class="flex items-baseline gap-2">
        <h3 class="text-2xl font-extrabold text-slate-900 tracking-tight">
          {{ value !== undefined && value !== null ? value : '--' }}
        </h3>
        <span
          v-if="trend"
          :class="['text-xs font-semibold px-1.5 py-0.5 rounded-md flex items-center gap-0.5 shrink-0', trendClass]"
        >
          <UIcon
            :name="trend === 'up' ? 'i-lucide-trending-up' : trend === 'down' ? 'i-lucide-trending-down' : 'i-lucide-minus'"
            class="size-3"
          />
          {{ trendValue }}
        </span>
      </div>
      <p
        v-if="description"
        class="text-xs text-slate-500 font-medium truncate"
      >
        {{ description }}
      </p>
    </div>

    <div :class="['flex size-11 shrink-0 items-center justify-center rounded-xl border transition-all duration-300 group-hover:scale-110 shadow-2xs', toneClass]">
      <UIcon
        :name="icon"
        class="size-5.5"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  label: string
  value: string | number | undefined | null
  icon: string
  tone?: 'blue' | 'emerald' | 'amber' | 'rose' | 'violet' | 'slate'
  description?: string
  trend?: 'up' | 'down' | 'neutral'
  trendValue?: string
}>(), {
  tone: 'blue',
})

const toneClass = computed(() => {
  return {
    blue: 'bg-blue-50/70 text-blue-600 border-blue-100/50',
    emerald: 'bg-emerald-50/70 text-emerald-600 border-emerald-100/50',
    amber: 'bg-amber-50/70 text-amber-600 border-amber-100/50',
    rose: 'bg-rose-50/70 text-rose-600 border-rose-100/50',
    violet: 'bg-violet-50/70 text-violet-600 border-violet-100/50',
    slate: 'bg-slate-50/70 text-slate-600 border-slate-200/50',
  }[props.tone]
})

const bgHighlightClass = computed(() => {
  return {
    blue: 'bg-blue-500',
    emerald: 'bg-emerald-500',
    amber: 'bg-amber-500',
    rose: 'bg-rose-500',
    violet: 'bg-violet-500',
    slate: 'bg-slate-500',
  }[props.tone]
})

const trendClass = computed(() => {
  if (props.trend === 'up') return 'bg-emerald-50 text-emerald-600 border border-emerald-100'
  if (props.trend === 'down') return 'bg-rose-50 text-rose-600 border border-rose-100'
  return 'bg-slate-50 text-slate-600 border border-slate-100'
})
</script>
