<script setup lang="ts">
import { ref } from 'vue'

definePageMeta({ layout: 'admin', middleware: 'require-admin' })

const isGroup = ref(false)
</script>

<template>
  <div class="space-y-5">
    <!-- Re-render component on type switch so that internal pagination and queries reset cleanly -->
    <AdminEvidenceManagement
      :key="isGroup ? 'group' : 'individual'"
      :is-group="isGroup"
    >
      <template #mode-switcher="{ loading, refresh }">
        <!-- Tab Switcher -->
        <div class="flex flex-col gap-3 rounded-2xl   sm:flex-row sm:items-center sm:justify-between">
          <div class="grid w-full grid-cols-2 gap-1 rounded-xl border border-slate-200 bg-slate-100 p-1 sm:w-[360px]">
            <button
              type="button"
              :aria-pressed="!isGroup"
              :class="[
                'inline-flex h-10 items-center justify-center gap-2 rounded-lg px-3 text-sm font-semibold transition cursor-pointer',
                !isGroup ? 'bg-white text-blue-600 shadow-sm ring-1 ring-slate-200' : 'text-slate-500 hover:bg-white/70 hover:text-slate-800',
              ]"
              @click="isGroup = false"
            >
              <UIcon
                name="i-lucide-user-round"
                class="size-4 shrink-0"
              />
              <span class="truncate">Hồ sơ cá nhân</span>
            </button>
            <button
              type="button"
              :aria-pressed="isGroup"
              :class="[
                'inline-flex h-10 items-center justify-center gap-2 rounded-lg px-3 text-sm font-semibold transition cursor-pointer',
                isGroup ? 'bg-white text-blue-600 shadow-sm ring-1 ring-slate-200' : 'text-slate-500 hover:bg-white/70 hover:text-slate-800',
              ]"
              @click="isGroup = true"
            >
              <UIcon
                name="i-lucide-users-round"
                class="size-4 shrink-0"
              />
              <span class="truncate">Hồ sơ tập thể</span>
            </button>
          </div>

          <UButton
            color="neutral"
            variant="outline"
            icon="i-lucide-refresh-cw"
            label="Làm mới"
            :loading="loading"
            class="h-10 justify-center rounded-xl font-semibold cursor-pointer"
            @click="refresh()"
          />
        </div>
      </template>
    </AdminEvidenceManagement>
  </div>
</template>
