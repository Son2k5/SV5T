<template>
  <div class="w-full bg-gray-300 rounded-t-lg p-4 grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-10">
    <div class="flex flex-col gap-6">
      <div class="flex items-center">
        <NuxtImg
          src="/logo5Merits.jpg"
          format="webp"
          quality="70"
          class="size-14"
        />
        <h2 class="text-info text-3xl font-bold ml-2">
          Sinh Viên 5 Tốt
        </h2>
      </div>
      <p>
        Hệ thống quản lý sinh viên toàn diện, giúp theo dõi học tập, rèn luyện và phát triển kỹ năng theo chuẩn 5
        tốt: Đạo đức tốt, Học tập tốt, Thể lực tốt, Tình nguyện tốt, Hội nhập tốt.
      </p>
      <div class="flex flex-col gap-2">
        <div
          v-for="info in INFOS"
          :key="info.desc"
          class="flex gap-2 items-center"
        >
          <UIcon :name="info.icon" />
          <p>{{ info.desc }}</p>
        </div>
      </div>
    </div>
    <div class="w-full">
      <div class="h-4" />
      <h3 class="text-info text-2xl font-bold">
        Hỗ trợ
      </h3>
      <div class="flex w-full">
        <div
          v-for="(col, id) in LINKS"
          :key="id"
          class="flex flex-col w-full"
        >
          <UButton
            v-for="link in col"
            :key="link.label"
            color="neutral"
            variant="link"
            :label="link.label"
            class="cursor-pointer p-4 pl-0"
          />
        </div>
      </div>
    </div>
    <div>
      <div class="h-4" />
      <h3 class="text-info text-2xl font-bold">
        Kết nối với chúng tôi
      </h3>
      <div class="flex gap-2">
        <UButton
          v-for="socialmedia in SOCIALMEDIAS"
          :key="socialmedia.icon"
          color="info"
          variant="ghost"
          class="p-2 cursor-pointer"
        >
          <template #default>
            <NuxtImg
              :src="socialmedia.icon"
              format="webp"
              quality="70"
              class="size-8"
            />
          </template>
        </UButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const INFOS = [
  {
    desc: '123 Đường Đại Học, Quận Cầu Giấy, Hà Nội',
    icon: 'i-heroicons-map-pin-solid',
  },
  {
    desc: '02434567890',
    icon: 'i-heroicons-phone-solid',
  },
  {
    desc: 'sinhvien5tot@university.edu.vn',
    icon: 'i-heroicons-envelope-solid',
  },
]

const LINKS = [
  [
    {
      label: 'Hướng dẫn sử dụng',
      link: '',
    },
    {
      label: 'Câu hỏi thường gặp',
      link: '',
    },
    {
      label: 'Liên hệ hỗ trợ',
      link: '',
    },
    {
      label: 'Góp ý - Phản hồi',
      link: '',
    }],
  [
    {
      label: 'Quy chế đào tạo',
      link: '',
    },
    {
      label: 'Chính sách bảo mật',
      link: '',
    },
    {
      label: 'Điều khoản sử dụng',
      link: '',
    }],
]

const SOCIALMEDIAS = [
  {
    icon: '/logoFacebook.png',
    link: '',
  },
  {
    icon: '/logoZalo.png',
    link: '',
  },
]
</script>
