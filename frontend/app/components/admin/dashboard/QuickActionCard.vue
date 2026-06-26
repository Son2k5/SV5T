<template>
  <div class="space-y-4">
    <SectionTitle
      title="Thao tác nhanh"
      icon="i-lucide-zap"
    />

    <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <NuxtLink
        v-for="action in actions"
        :key="action.title"
        :to="action.to"
        class="sv-card p-5 flex items-start gap-4 hover:-translate-y-0.5 hover:shadow-xs group duration-200 cursor-pointer text-left border border-slate-100"
      >
        <div :class="['flex size-10 shrink-0 items-center justify-center rounded-xl transition duration-300 group-hover:scale-105', action.bgClass]">
          <UIcon
            :name="action.icon"
            class="size-5"
          />
        </div>
        <div class="min-w-0">
          <h4 class="text-sm font-bold text-slate-800 tracking-tight transition duration-200 group-hover:text-blue-600">
            {{ action.title }}
          </h4>
          <p class="mt-1 text-xs text-slate-400 font-medium leading-relaxed">
            {{ action.description }}
          </p>
        </div>
      </NuxtLink>
    </div>
  </div>
</template>

<script setup lang="ts">
import SectionTitle from './SectionTitle.vue'

const actions = [
  {
    title: 'Tạo đợt xét chọn',
    description: 'Tạo đợt xét duyệt Sinh viên 5 Tốt mới.',
    icon: 'i-lucide-calendar-plus',
    to: '/admin/campaigns',
    bgClass: 'bg-blue-50 text-blue-600 border border-blue-100/50',
  },
  {
    title: 'Thêm tiêu chí',
    description: 'Thiết lập và phân loại tiêu chí con.',
    icon: 'i-lucide-plus-circle',
    to: '/admin/criteria',
    bgClass: 'bg-purple-50 text-purple-600 border border-purple-100/50',
  },
  {
    title: 'Thêm Standard',
    description: 'Tạo nhóm tiêu chuẩn đánh giá chính.',
    icon: 'i-lucide-folder-plus',
    to: '/admin/criteria',
    bgClass: 'bg-emerald-50 text-emerald-600 border border-emerald-100/50',
  },
  {
    title: 'Duyệt minh chứng',
    description: 'Xử lý và chấm điểm minh chứng hồ sơ.',
    icon: 'i-lucide-file-check-2',
    to: '/admin/evidence',
    bgClass: 'bg-amber-50 text-amber-600 border border-amber-100/50',
  },
  {
    title: 'Quản lý người dùng',
    description: 'Quản trị phân quyền, danh sách thành viên.',
    icon: 'i-lucide-users-2',
    to: '/admin/users',
    bgClass: 'bg-indigo-50 text-indigo-600 border border-indigo-100/50',
  },
  {
    title: 'Xem báo cáo kết quả',
    description: 'Theo dõi tiến trình đạt/loại của hồ sơ.',
    icon: 'i-lucide-file-bar-chart-2',
    to: '/admin/evidence',
    bgClass: 'bg-rose-50 text-rose-600 border border-rose-100/50',
  },
]
</script>
