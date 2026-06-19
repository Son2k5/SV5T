<template>
  <div class="grid grid-cols-1 gap-6 xl:grid-cols-[minmax(0,2fr)_minmax(320px,0.9fr)]">
    <div class="flex flex-col gap-6">
      <CommonPageSection
        title="Thông tin Sinh viên"
        title-icon="i-heroicons-user-solid"
        inner-class="!block p-0"
      >
        <div class="grid grid-cols-1 lg:grid-cols-2">
          <div
            v-for="info in GENERALINFO"
            :key="info.label"
            class="group flex gap-3 border-b border-[#E5E7EB] px-5 py-4 transition hover:bg-[#F8FAFC] lg:[&:nth-last-child(-n+2)]:border-b-0"
          >
            <div class="mt-0.5 flex size-8 shrink-0 items-center justify-center rounded-xl bg-blue-50 text-[#3B82F6]">
              <UIcon
                name="i-lucide-circle-dot"
                class="size-3"
              />
            </div>
            <div class="min-w-0">
              <h3 class="text-xs font-semibold uppercase tracking-wide text-[#64748B]">
                {{ info.label }}
              </h3>
              <p class="mt-1 break-words text-sm font-semibold text-[#1E293B]">
                {{ info.desc || '-' }}
              </p>
            </div>
          </div>
        </div>
      </CommonPageSection>

      <CommonPageSection inner-class="!block">
        <div class="mb-5 flex items-center justify-between gap-4">
          <div>
            <h2 class="text-xl font-bold text-[#1E293B]">
              Truy cập nhanh
            </h2>
            <p class="mt-1 text-sm text-[#64748B]">
              Các tác vụ thường dùng cho hồ sơ và minh chứng
            </p>
          </div>
          <UIcon
            name="i-lucide-sparkles"
            class="size-5 text-[#3B82F6]"
          />
        </div>
        <div class="grid w-full grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <button
            v-for="action in QUICKACCESS"
            :key="action.label"
            class="group rounded-2xl border border-[#E5E7EB] bg-white p-5 text-left shadow-sm transition duration-200 hover:-translate-y-0.5 hover:border-[#BFDBFE] hover:bg-blue-50/40 hover:shadow-[0_16px_36px_rgba(59,130,246,0.12)]"
            type="button"
          >
            <div class="mb-4 flex size-12 items-center justify-center rounded-2xl bg-blue-50 text-[#3B82F6] transition group-hover:scale-105 group-hover:bg-[#3B82F6] group-hover:text-white">
              <UIcon
                :name="action.icon"
                class="size-6"
              />
            </div>
            <h3 class="text-base font-bold text-[#1E293B]">
              {{ action.label }}
            </h3>
            <p class="mt-1 text-sm leading-6 text-[#64748B]">
              {{ action.desc }}
            </p>
          </button>
        </div>
      </CommonPageSection>
    </div>

    <div class="flex flex-col gap-6">
      <CommonPageSection
        container-class="overflow-hidden border-0 bg-gradient-to-br from-[#3B82F6] to-[#60A5FA] text-white"
        inner-class="relative flex-col items-start gap-4 p-7"
      >
        <div class="absolute -right-10 -top-10 size-36 rounded-full bg-white/15" />
        <div class="absolute -bottom-12 right-10 size-28 rounded-full bg-white/10" />
        <div class="flex size-12 items-center justify-center rounded-2xl bg-white/20">
          <UIcon
            name="i-lucide-trophy"
            class="size-6"
          />
        </div>
        <h3 class="relative text-lg font-semibold text-blue-50">
          Tổng Điểm Xét Duyệt
        </h3>
        <h2 class="relative text-5xl font-bold tracking-tight">
          465 / 500
        </h2>
        <p class="relative max-w-sm text-sm leading-6 text-blue-50">
          Số điểm xuất sắc! Hãy tiếp tục duy trì tiến độ!
        </p>
      </CommonPageSection>

      <CommonPageSection
        title="Tiến Độ 5 Tiêu Chí"
        container-class="h-full"
        inner-class="h-full !block"
        title-icon="i-heroicons-bolt-solid"
      >
        <div class="flex h-full w-full flex-col gap-5">
          <div
            v-for="progress in PROGRESSES"
            :key="progress.label"
            class="space-y-2"
          >
            <div class="flex w-full items-center justify-between gap-3">
              <p class="text-sm font-semibold text-[#1E293B]">
                {{ progress.label }}
              </p>
              <p class="text-sm font-bold text-[#2563EB]">
                {{ progress.score }}<span class="font-normal text-[#64748B]"> / 100</span>
              </p>
            </div>
            <div class="h-2.5 overflow-hidden rounded-full bg-slate-100">
              <div
                class="h-full rounded-full bg-gradient-to-r from-[#3B82F6] to-[#60A5FA] transition-all duration-300"
                :style="{ width: `${progress.score}%` }"
              />
            </div>
          </div>
          <UButton
            label="Nộp thêm minh chứng"
            icon="i-lucide-arrow-right"
            trailing
            variant="soft"
            color="info"
            class="mt-2 w-full justify-center"
          />
        </div>
      </CommonPageSection>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ProfileData } from '~/types/profile'

const { data: profile } = useNuxtData<ProfileData>('current-user')

const GENERALINFO = computed(() => [
  {
    label: 'Mã Sinh viên',
    desc: profile.value?.studentCode,
  },
  {
    label: 'Họ và tên',
    desc: profile.value?.fullName,
  },
  {
    label: 'Email tài khoản',
    desc: profile.value?.email,
  },
  {
    label: 'Email liên hệ',
    desc: profile.value?.contactEmail,
  },
  {
    label: 'Số Điện thoại',
    desc: profile.value?.phoneNumber,
  },
  {
    label: 'Giới tính',
    desc: formatGender[profile.value?.gender ?? 'OTHER'],
  },
  {
    label: 'Ngày sinh',
    desc: formatDate(profile.value?.birthDay),
  },
  {
    label: 'Số CCCD/CMND',
    desc: profile.value?.identityCardNumber,
  },
  {
    label: 'Dân tộc',
    desc: profile.value?.ethnicity,
  },
  {
    label: 'Trường',
    desc: profile.value?.school,
  },
  {
    label: 'Ngành',
    desc: profile.value?.major,
  },
  {
    label: 'Khoa',
    desc: profile.value?.faculty,
  },
  {
    label: 'Sinh viên năm',
    desc: profile.value?.academicYear?.toString(),
  },
  {
    label: 'Lớp',
    desc: profile.value?.administrativeClass,
  },
  {
    label: 'Địa chỉ thường trú',
    desc: formatAddress(profile.value?.permanentAddress),
  },
  {
    label: 'Địa chỉ tạm trú',
    desc: formatAddress(profile.value?.temporaryAddress),
  },
  {
    label: 'Chức vụ',
    desc: profile.value?.currentPosition,
  },
  {
    label: 'Chức vụ Đoàn hội',
    desc: profile.value?.unionPosition,
  },
  {
    label: 'Đảng viên / Đoàn viên',
    desc: formatPoliticalStatus[profile.value?.politicalStatus ?? 'NONE'],
  },
])

const QUICKACCESS = [
  {
    label: 'Lịch Hoạt động',
    desc: 'Lịch các sự kiện sắp diễn ra',
    icon: 'i-heroicons-calendar-solid',
  },
  {
    label: 'Xem Điểm 5 Tốt',
    desc: 'Theo dõi 5 tiêu chí đánh giá',
    icon: 'i-heroicons-chart-bar-solid',
  },
  {
    label: 'Đăng ký Hoạt động',
    desc: 'Đăng ký hoạt động theo cá nhân',
    icon: 'i-heroicons-trophy-solid',
  },
  {
    label: 'Nộp minh chứng',
    desc: 'Tải lên tài liệu minh chứng',
    icon: 'i-heroicons-document-solid',
  },
  {
    label: 'Nhắn tin',
    desc: 'Liên hệ trực tiếp',
    icon: 'i-heroicons-chat-bubble-left-solid',
  },
  {
    label: 'Hỗ trợ',
    desc: 'Yêu cầu tư vấn và giải pháp',
    icon: 'i-heroicons-lifebuoy-solid',
  },
]

const PROGRESSES = [
  {
    label: 'Học tập tốt',
    score: 22,
  },
  {
    label: 'Đạo đức tốt',
    score: 36,
  },
  {
    label: 'Thể lục tốt',
    score: 47,
  },
  {
    label: 'Tình nguyện tốt',
    score: 74,
  },
  {
    label: 'Hội nhập tốt',
    score: 96,
  },
]
</script>
