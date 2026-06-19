<template>
  <div class="flex flex-col gap-6">
    <div class="relative">
      <CriteriaAnimatedBackground />
      <div
        v-if="activeStandard && activeStandard != 'select'"
        class="relative z-2 mx-auto -mt-8 grid w-[calc(100%-2rem)] max-w-3xl grid-cols-1 gap-2 rounded-2xl border border-[#E5E7EB] bg-white/95 p-2 shadow-[0_18px_45px_rgba(15,23,42,0.12)] backdrop-blur sm:grid-cols-3"
      >
        <UButton
          v-for="option in levelOptions"
          :key="option.label"
          class="h-12 justify-center rounded-xl text-sm font-semibold transition duration-200 md:text-base"
          :variant="route.query.level === option.path ? 'solid' : 'ghost'"
          color="info"
          :to="{
            hash: route.hash,
            query: { level: option.path, mode: route.query.mode },
          }"
          :label="option.label"
          @click="simulateLoading"
        />
      </div>
    </div>

    <div
      v-if="activeStandard && activeStandard != 'select' && activeStandard != 'group'"
      class="sticky top-16 z-20 rounded-2xl border border-[#E5E7EB] bg-white/90 p-2 shadow-sm backdrop-blur"
    >
      <div class="grid grid-cols-1 gap-2 md:grid-cols-5">
        <UButton
          v-for="option in standardOptions"
          :key="option.label"
          class="h-11 justify-center rounded-xl text-sm font-semibold"
          color="info"
          :variant="route.hash === option.hash ? 'solid' : 'ghost'"
          :label="option.label"
          :icon="option.icon"
          :to="{
            query: { level: route.query.level },
            hash: option.hash,
          }"
          @click="simulateLoading"
        />
      </div>
    </div>

    <CommonPageLoading
      v-if="isLoading"
      class="h-80"
    />

    <div
      v-else-if="activeStandard === 'select'"
      class="grid gap-5 md:grid-cols-2"
    >
      <UButton
        v-for="option in modeOptions"
        :key="option.label"
        class="group h-auto justify-start rounded-2xl border border-[#E5E7EB] bg-white p-6 text-left shadow-sm transition duration-200 hover:-translate-y-0.5 hover:border-[#BFDBFE] hover:bg-blue-50/40 hover:shadow-[0_18px_45px_rgba(59,130,246,0.12)]"
        color="neutral"
        variant="ghost"
        :to="{
          query: { level: option.query, mode: option.mode },
          hash: option.hash,
        }"
        @click="simulateLoading"
      >
        <div class="flex w-full flex-col gap-5">
          <div class="flex size-14 items-center justify-center rounded-2xl bg-blue-50 text-[#3B82F6] transition group-hover:bg-[#3B82F6] group-hover:text-white">
            <UIcon
              class="size-7"
              :name="option.icon"
            />
          </div>
          <div>
            <h2 class="text-2xl font-bold text-[#1E293B]">
              {{ option.label }}
            </h2>
            <p class="mt-2 text-sm leading-6 text-[#64748B]">
              {{ option.desc }}
            </p>
          </div>
          <div class="flex items-center gap-2 text-sm font-semibold text-[#2563EB]">
            Bắt đầu xét duyệt
            <UIcon
              name="i-lucide-arrow-right"
              class="size-4 transition group-hover:translate-x-1"
            />
          </div>
        </div>
      </UButton>
    </div>

    <CommonPageSection
      v-else-if="activeStandard === 'group'"
      container-class="mx-auto max-w-4xl"
      inner-class="!block p-6 md:p-8"
    >
      <div class="mb-8 flex flex-col gap-2 text-center">
        <p class="text-sm font-bold uppercase tracking-[0.18em] text-[#3B82F6]">
          Xét danh hiệu
        </p>
        <h2 class="text-3xl font-bold text-[#1E293B] md:text-4xl">
          Tập thể 5 Tốt
        </h2>
        <p class="text-sm leading-6 text-[#64748B]">
          Điền thông tin đại diện tập thể để tiếp tục quy trình xét duyệt.
        </p>
      </div>
      <UForm class="flex flex-col gap-6">
        <div class="grid gap-5 md:grid-cols-2">
          <UFormField
            v-for="field in groupFormOptions"
            :key="field.label"
            :label="field.label"
            required
          >
            <UInput class="w-full" />
          </UFormField>
        </div>
        <div class="rounded-2xl border border-[#E5E7EB] bg-[#F8FAFC] p-4">
          <UFormField>
            <UCheckbox
              required
              color="info"
              label="Xác nhận đủ điều kiện đạt danh hiệu tập thể 5 Tốt"
            />
          </UFormField>
        </div>
        <UButton
          label="Lưu hồ sơ tập thể"
          icon="i-lucide-save"
          size="lg"
          class="ml-auto w-full justify-center px-8 shadow-sm sm:w-max"
          @click="handleGroupSubmit"
        />
      </UForm>
    </CommonPageSection>

    <div
      v-else-if="activeStandard && activeStandard?.data"
      class="flex flex-col gap-5"
    >
      <div class="flex flex-col gap-4 rounded-2xl border border-[#E5E7EB] bg-white p-5 shadow-sm lg:flex-row lg:items-center lg:justify-between">
        <div class="flex items-center gap-3">
          <div class="sv-icon-tile">
            <UIcon
              :name="activeStandard.icon"
              class="size-5"
            />
          </div>
          <div>
            <p class="text-sm font-semibold uppercase tracking-wide text-[#64748B]">
              Bộ tiêu chuẩn xét duyệt
            </p>
            <h2 class="text-2xl font-bold text-[#1E293B]">
              Tiêu chuẩn {{ activeStandard.label }}
            </h2>
          </div>
        </div>
        <UButton
          color="neutral"
          label="Hướng dẫn nộp"
          class="w-full justify-center rounded-xl border border-[#E5E7EB] bg-white shadow-sm lg:w-max"
          size="lg"
          leading-icon="i-heroicons-play-circle-solid"
          target="_blank"
          to="https://www.youtube.com/watch?v=jk7LbXUpmz0"
        />
      </div>

      <CriteriaCard
        v-for="criteria in activeStandard.data.criteriaDTOList"
        :key="criteria.id"
        :criteria="criteria"
      />
    </div>

    <CommonPageEmpty v-else />
  </div>
</template>

<script setup lang="ts">
const route = useRoute()

const isLoading = shallowRef<boolean>(false)
const simulateLoading = async () => {
  isLoading.value = true
  await new Promise(resolve => setTimeout(resolve, 1000))
  isLoading.value = false
}

const { data } = await useCriteria()
const toast = useToast()

const activeStandard = computed(() => {
  if (!data.value) return null

  if (route.query.mode === 'group') return 'group'
  if (!route.hash) return 'select'

  const activeStandardOption = standardOptions.find(option => option.hash === route.hash)
  return {
    label: activeStandardOption?.label,
    icon: activeStandardOption?.icon,
    data: data.value.find(standard => standard.id === activeStandardOption?.id) ?? null,
  }
})

const handleGroupSubmit = () => {
  simulateLoading()
  toast.add({
    title: 'Nộp tài liệu thành công',
    color: 'success',
  })
  navigateTo({
    query: { },
    hash: '',
  })
}

const modeOptions = [
  {
    label: 'Cá nhân',
    desc: 'Xét danh hiệu đối với từng sinh viên theo hồ sơ và minh chứng cá nhân.',
    query: 'university',
    hash: '#ethics',
    icon: 'i-heroicons-user-solid',
  },
  {
    label: 'Tập thể',
    desc: 'Xét danh hiệu cho tập thể lớp, câu lạc bộ hoặc đơn vị trực thuộc.',
    query: 'university',
    mode: 'group',
    icon: 'i-heroicons-users-solid',
  },
]

const levelOptions = [
  {
    label: 'Cấp Trường',
    path: 'university',
  },
  {
    label: 'Cấp Thành Phố',
    path: 'city',
  },
  {
    label: 'Cấp Trung Ương',
    path: 'nation',
  },
]

const standardOptions = [
  {
    id: 1,
    label: 'Đạo đức tốt',
    icon: 'i-heroicons-heart-solid',
    hash: '#ethics',
  },
  {
    id: 2,
    label: 'Học tập tốt',
    icon: 'i-heroicons-book-open-solid',
    hash: '#studies',
  },
  {
    id: 3,
    label: 'Thể lực tốt',
    icon: 'i-heroicons-bolt-solid',
    hash: '#strength',
  },
  {
    id: 4,
    label: 'Tình nguyện tốt',
    icon: 'i-heroicons-users-solid',
    hash: '#volunteering',
  },
  {
    id: 5,
    label: 'Hội nhập tốt',
    icon: 'i-heroicons-globe-alt-solid',
    hash: '#integration',
  },
]

const groupFormOptions = [
  {
    label: 'Họ và tên người đại diện',
  },
  {
    label: 'Lớp hành chính',
  },
]
</script>
