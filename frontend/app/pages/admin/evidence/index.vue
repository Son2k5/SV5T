<script setup lang="ts">
import { ref } from 'vue'

definePageMeta({ layout: 'admin', middleware: 'require-admin' })

const isGroup = ref(false)
</script>

<template>
  <div class="space-y-5">
    <!-- Tab Switcher -->
    <div class="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-xs flex justify-between items-center">
      <div class="flex gap-2 rounded-xl bg-slate-100 p-1 w-full max-w-[280px] border border-slate-200">
        <button
          type="button"
          :class="[
            'flex-1 h-9 rounded-lg text-sm font-semibold transition cursor-pointer',
            !isGroup ? 'bg-white text-blue-600 shadow-xs border border-slate-200' : 'text-slate-500 hover:text-slate-800',
          ]"
          @click="isGroup = false"
        >
          Hồ sơ cá nhân
        </button>
        <button
          type="button"
          :class="[
            'flex-1 h-9 rounded-lg text-sm font-semibold transition cursor-pointer',
            isGroup ? 'bg-white text-blue-600 shadow-xs border border-slate-200' : 'text-slate-500 hover:text-slate-800',
          ]"
          @click="isGroup = true"
        >
          Hồ sơ tập thể
        </button>
      </div>

      <p class="text-xs text-[#64748B] hidden md:block">
        Đang xem danh sách xét duyệt: <span class="font-bold text-[#1E293B]">{{ isGroup ? 'Tập thể' : 'Cá nhân' }}</span>
      </p>
    </div>

    <!-- Re-render component on type switch so that internal pagination and queries reset cleanly -->
    <AdminEvidenceEvidenceManagement
      :key="isGroup ? 'group' : 'individual'"
      :is-group="isGroup"
    />
  </div>
</template>
