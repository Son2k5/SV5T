<template>
  <div class="w-full h-max">
    <div
      class="flex w-full gap-8 bg-gray-200 rounded-lg ring-info hover:ring-2 hover:shadow-xl hover:scale-102 transition py-2 px-4 cursor-pointer"
      @click="() => { isOpen = !isOpen }"
    >
      <div class="flex flex-col gap-2 h-24">
        <div class="bg-info-300 rounded-lg h-full w-24 flex justify-center items-center font-bold text-5xl">
          {{ criteria.id }}
        </div>
        <div
          v-if="criteria.isMandatory"
          class="font-bold text-lg text-center bg-error-400 rounded-lg"
        >
          Bắt buộc
        </div>
      </div>
      <div class="w-full">
        <h2 class="text-info text-2xl font-bold">
          {{ criteria.name }}
        </h2>
        <p>{{ criteria.description }}</p>
      </div>
      <div class="w-100 border-l-2 border-gray-300 flex flex-col pl-2 gap-2 justify-center">
        <div class="flex gap-2 items-center">
          <p>Trạng thái:</p>
          <UBadge
            size="xl"
            :label="criteria.evidenceStatus ? `● Đã Nộp` : `● Chưa Nộp`"
            :color="criteria.evidenceStatus ? 'success' : 'warning'"
          />
        </div>
        <p>Hình thức nộp: {{ criteria.evidenceType }}</p>
        <p>Nhận xét: {{ criteria.reviewerComment ? '1' : '0' }}</p>
      </div>
    </div>
    <div
      class="transition-all flex flex-col overflow-hidden bg-gray-200 rounded-lg px-4 gap-4"
      :class="isOpen ? 'h-60' : 'h-0'"
    >
      <div class="flex gap-8 mt-2">
        <h2 class="text-xl text-info font-bold">
          Nộp Minh chứng
        </h2>
        <div class="flex flex-col gap-2">
          <UInput
            type="file"
            class="cursor-pointer mr-2 w-full"
          />
          <UTextarea
            class="w-full"
            placeholder="Nhận xét cá nhân..."
            autoresize
          />
        </div>
        <UButton
          label="Lưu"
          class="mt-auto px-10 cursor-pointer"
          :loading="isLoading"
          :ui="{ label: ['mx-auto text-lg', isLoading && 'hidden'], leadingIcon: 'mx-auto' }"
          @click="handleSubmit"
        />
      </div>
      <div class="w-full">
        <h2 class="text-xl text-info font-bold">
          Nhận xét
        </h2>
        <div
          v-if="criteria.reviewerComment"
          class="flex items-center gap-8 px-2"
        >
          <div class="shrink-0 flex items-center gap-2">
            <UAvatar
              src="ProfileUserImageTemp.jpg"
              class="size-10"
            />
            <div>
              <p class="font-bold">
                Nguyễn Văn A
              </p>
              <p class="text-dimmed text-sm text-start">
                Mentor
              </p>
            </div>
          </div>
          <UTextarea
            :model-value="criteria.reviewerComment"
            autoresize
            disabled
            class="w-full"
          />
        </div>
        <p
          v-else
          class="mt-6 px-2"
        >
          Không có nhận xét
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Criteria } from '~/types/criteria'

defineProps<{
  criteria: Criteria
}>()

const toast = useToast()

const isOpen = shallowRef<boolean>(false)

const isLoading = shallowRef<boolean>(false)

const handleSubmit = async () => {
  isLoading.value = true
  await new Promise(resolve => setTimeout(resolve, 1000))
  isLoading.value = false
  isOpen.value = false
  toast.add({
    title: 'Nộp tài liệu thành công!',
    color: 'success',
  })
}
</script>
