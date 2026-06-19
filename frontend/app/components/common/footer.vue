<template>
  <footer class="border-t border-[#E5E7EB] bg-white/80 px-4 py-8 text-[#64748B] backdrop-blur">
    <div class="mx-auto grid w-full max-w-[1440px] grid-cols-1 gap-8 lg:grid-cols-2 xl:grid-cols-3">
      <div class="flex flex-col gap-5">
        <div class="flex items-center">
          <NuxtImg
            src="/logo5Merits.jpg"
            format="webp"
            quality="70"
            class="size-12 rounded-2xl shadow-sm"
          />
          <h2 class="ml-3 text-2xl font-bold text-[#1E293B]">
            Sinh Viên 5 Tốt
          </h2>
        </div>
        <p class="max-w-lg leading-7">
          Hệ thống quản lý sinh viên toàn diện, giúp theo dõi học tập, rèn luyện và phát triển kỹ năng theo chuẩn 5
          tốt: Đạo đức tốt, Học tập tốt, Thể lực tốt, Tình nguyện tốt, Hội nhập tốt.
        </p>
        <div class="flex flex-col gap-2">
          <div
            v-for="info in INFOS"
            :key="info.desc"
            class="flex items-center gap-2"
          >
            <UIcon
              :name="info.icon"
              class="size-4 text-[#3B82F6]"
            />
            <p>{{ info.desc }}</p>
          </div>
        </div>
      </div>
      <div class="w-full">
        <h3 class="text-lg font-bold text-[#1E293B]">
          Hỗ trợ
        </h3>
        <div class="mt-3 flex w-full">
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
              class="w-max cursor-pointer p-2 pl-0 text-[#64748B] hover:text-[#2563EB]"
            />
          </div>
        </div>
      </div>
      <div>
        <h3 class="text-lg font-bold text-[#1E293B]">
          Kết nối với chúng tôi
        </h3>
        <div class="mt-4 flex gap-2">
          <UButton
            v-for="socialmedia in SOCIALMEDIAS"
            :key="socialmedia.icon"
            color="neutral"
            variant="ghost"
            class="cursor-pointer rounded-2xl border border-[#E5E7EB] bg-white p-2 hover:border-[#3B82F6] hover:bg-blue-50"
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
  </footer>
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
