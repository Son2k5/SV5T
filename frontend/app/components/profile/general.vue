<template>
  <div class="flex flex-col lg:grid grid-cols-3 gap-10 lg:gap-4">
    <div class="flex flex-col gap-10 col-span-2">
      <CommonPageSection
        title="Thông tin Sinh viên"
        title-icon="i-heroicons-user-solid"
      >
        <div class="flex flex-wrap gap-y-2">
          <div
            v-for="info in GENERALINFO"
            :key="info.label"
            class="w-1/2 grid grid-cols-1 md:grid-cols-[30%_auto]"
          >
            <h3 class="font-bold text-info">
              {{ info.label }}:
            </h3>
            <p class="text-wrap">
              {{ info.desc }}
            </p>
          </div>
        </div>
      </CommonPageSection>
      <CommonPageSection inner-class="flex-wrap gap-6">
        <h2 class="text-2xl font-bold w-full text-center">
          Truy Cập Nhanh
        </h2>
        <div class="grid grid-cols-2 lg:grid-cols-3 w-full gap-6">
          <UButton
            v-for="action in QUICKACCESS"
            :key="action.label"
            variant="ghost"
            color="info"
            class="w-full cursor-pointer"
          >
            <template #default>
              <div class="mx-auto">
                <UIcon
                  :name="action.icon"
                  class="size-6"
                />
                <h3 class="font-bold text-lg">
                  {{ action.label }}
                </h3>
                <p class="text-dimmed">
                  {{ action.desc }}
                </p>
              </div>
            </template>
          </UButton>
        </div>
      </CommonPageSection>
    </div>
    <div class="flex flex-col gap-10">
      <CommonPageSection inner-class="bg-info-300 rounded-lg p-10 flex-col text-white gap-2">
        <h3 class="text-2xl font-bold">
          Tổng Điểm Xét Duyệt
        </h3>
        <h2 class="text-5xl font-bold">
          465 / 500
        </h2>
        <p class="text-lg">
          Số điểm xuất sắc! Hãy tiếp tục duy trì tiến độ!
        </p>
      </CommonPageSection>
      <CommonPageSection
        title="Tiến Độ 5 Tiêu Chí"
        container-class="h-full"
        inner-class="h-full"
        title-icon="i-heroicons-bolt-solid"
      >
        <div class="flex flex-col w-full h-full gap-4">
          <div
            v-for="progress in PROGRESSES"
            :key="progress.label"
            class="h-full"
          >
            <div class="flex justify-between font-bold w-full">
              <p>{{ progress.label }}</p>
              <p>{{ progress.score }}<span class="font-normal text-dimmed"> / 100</span></p>
            </div>
            <UProgress
              v-model="progress.score"
              color="info"
            />
          </div>
        </div>
      </CommonPageSection>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ProfileData } from '~/types/profile'

const { data: profile } = useNuxtData<ProfileData>('current-user')

const GENERALINFO = ref([
  {
    label: 'Mã Sinh viên',
    desc: profile.value?.studentCode,
  },
  {
    label: 'Số Điện thoại',
    desc: profile.value?.phoneNumber,
  },
  {
    label: 'Họ và tên',
    desc: `${profile.value?.lastName} ${profile.value?.firstName}`,
  },
  {
    label: 'Email',
    desc: profile.value?.email,
  },
  {
    label: 'Giới tính',
    desc: formatGender[profile.value?.gender ?? 'OTHER'],
  },
  {
    label: 'Ngành',
    desc: profile.value?.fieldOfStudy,
  },
  {
    label: 'Ngày sinh',
    desc: formatDate(profile.value?.dob),
  },
  {
    label: 'Khoa',
    desc: profile.value?.faculty,
  },
  {
    label: 'Dân tộc',
    desc: profile.value?.ethnicity,
  },
  {
    label: 'Lớp',
    desc: profile.value?.classCode,
  },
  {
    label: 'Địa chỉ',
    desc: `${profile.value?.commune} - ${profile.value?.province}`,
  },
  {
    label: 'Chức vụ',
    desc: profile.value?.currentPosition,
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
