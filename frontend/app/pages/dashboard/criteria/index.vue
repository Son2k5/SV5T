<template>
  <div class="flex flex-col gap-10">
    <div class="relative flex justify-center">
      <CriteriaAnimatedBackground />
      <div
        v-if="activeStandard && activeStandard != 'select'"
        class="absolute z-2 -bottom-8 rounded-lg shadow-xl flex bg-white"
      >
        <UButton
          v-for="option in levelOptions"
          :key="option.label"
          class="w-36 md:w-48 h-16 justify-center md:text-lg cursor-pointer"
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
    <CommonPageSection
      v-if="activeStandard && activeStandard != 'select' && activeStandard != 'group'"
      class="sticky top-16 z-2"
      inner-class="justify-center py-2"
    >
      <UButton
        v-for="option in standardOptions"
        :key="option.label"
        class="w-full h-10 justify-center"
        color="info"
        :variant="route.hash === option.hash ? 'outline' : 'ghost'"
        :label="option.label"
        :icon="option.icon"
        :to="{
          query: { level: route.query.level },
          hash: option.hash,
        }"
        @click="simulateLoading"
      />
    </CommonPageSection>

    <CommonPageLoading
      v-if="isLoading"
      class="h-80"
    />
    <CommonPageSection
      v-else-if="activeStandard === 'select'"
    >
      <div class="w-full py-20 flex flex-col justify-center items-center gap-8">
        <h2 class="text-5xl font-bold text-info">
          Chế Độ Minh Chứng
        </h2>
        <div class="flex gap-8 justify-center">
          <UButton
            v-for="option in modeOptions"
            :key="option.label"
            :to="{
              query: { level: option.query, mode: option.mode },
              hash: option.hash,
            }"
          >
            <div class="flex flex-col items-center gap-2 px-10 py-14">
              <div class="rounded-lg bg-gray-200 size-20 min-w-10 flex items-center justify-center">
                <UIcon
                  class="bg-info size-14"
                  :name="option.icon"
                />
              </div>
              <h1 class="text-center font-bold text-2xl">
                {{ option.label }}
              </h1>
              <p>{{ option.desc }}</p>
            </div>
          </UButton>
        </div>
      </div>
    </CommonPageSection>
    <CommonPageSection
      v-else-if="activeStandard === 'group'"
      inner-class="justify-center flex-col gap-12 py-20"
    >
      <div class="flex flex-col gap-2 items-center">
        <h2 class="text-info font-bold text-3xl">
          Xét danh hiệu
        </h2>
        <h2 class="text-info font-bold text-5xl">
          Tập thể 5 Tốt
        </h2>
      </div>
      <UForm class="flex flex-col gap-8">
        <div class="flex gap-8">
          <UFormField
            v-for="field in groupFormOptions"
            :key="field.label"
            :label="field.label"
            required
          >
            <UInput class="w-full" />
          </UFormField>
        </div>
        <UFormField>
          <UCheckbox
            required
            color="info"
            label="Xác nhận đủ điều kiện đạt danh hiệu tập thể 5"
          />
        </UFormField>
        <UButton
          label="Lưu"
          size="lg"
          class="w-max ml-auto px-8"
          @click="handleGroupSubmit"
        />
      </UForm>
    </CommonPageSection>
    <CommonPageSection
      v-else-if="activeStandard && activeStandard?.data"
      :title=" `Tiêu Chuẩn ${activeStandard.label}`"
      :title-icon="activeStandard.icon"
      inner-class="px-6 flex-col gap-8"
    >
      <template #title>
        <UButton
          color="neutral"
          label="Hướng dẫn Nộp"
          class="ml-auto cursor-pointer"
          size="xl"
          leading-icon="i-heroicons-play-circle-solid"
          target="_blank"
          to="https://www.youtube.com/watch?v=jk7LbXUpmz0"
        />
      </template>
      <CriteriaCard
        v-for="criteria in activeStandard.data.criteriaDTOList"
        :key="criteria.id"
        :criteria="criteria"
      />
    </CommonPageSection>
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
    desc: 'Xét danh hiệu đối với mỗi cá nhân',
    query: 'university',
    hash: '#ethics',
    icon: 'i-heroicons-user-solid',
  },
  {
    label: 'Tập thể',
    desc: 'Xét danh hiệu dưới dạng tập thể',
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
