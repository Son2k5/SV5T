<template>
  <div class="w-full h-max">
    <div
      class="sv-card sv-card-hover flex w-full cursor-pointer flex-col gap-4 p-5 lg:flex-row lg:items-center"
      @click="() => { isOpen = !isOpen }"
    >
      <div class="flex items-center gap-3 lg:w-32 lg:flex-col">
        <div class="flex size-16 items-center justify-center rounded-2xl bg-blue-50 text-2xl font-bold text-[#2563EB] lg:size-20 lg:text-4xl">
          {{ index }}
        </div>
        <div
          v-if="criteria.isMandatory"
          class="rounded-full bg-red-50 px-3 py-1 text-xs font-bold text-red-600"
        >
          Bắt buộc
        </div>
      </div>
      <div class="min-w-0 flex-1">
        <h2 class="text-xl font-bold text-[#1E293B]">
          {{ criteria.name }}
        </h2>
        <p class="mt-2 text-sm leading-6 text-[#64748B]">
          {{ criteria.description }}
        </p>
      </div>
      <div class="flex w-full flex-col gap-3 rounded-2xl border border-[#E5E7EB] bg-[#F8FAFC] p-4 lg:w-80">
        <div class="flex items-center justify-between gap-2">
          <p class="text-sm font-semibold text-[#64748B]">
            Trạng thái
          </p>
          <UBadge
            class="rounded-full"
            :label="criteria.evidenceStatus ? `● Đã Nộp` : `● Chưa Nộp`"
            :color="criteria.evidenceStatus ? 'success' : 'warning'"
          />
        </div>
        <p class="text-sm text-[#64748B]">
          Hình thức nộp: <span class="font-semibold text-[#1E293B]">{{ criteria.evidenceType }}</span>
        </p>
        <p class="text-sm text-[#64748B]">
          Nhận xét: <span class="font-semibold text-[#1E293B]">{{ criteria.reviewerComment ? '1' : '0' }}</span>
        </p>
      </div>
    </div>
    <div
      class="mt-3 flex flex-col overflow-hidden rounded-2xl border border-[#E5E7EB] bg-white px-5 transition-all duration-300"
      :class="isOpen ? 'max-h-[520px] py-5 opacity-100' : 'max-h-0 py-0 opacity-0'"
    >
      <div class="grid gap-5 lg:grid-cols-[180px_minmax(0,1fr)_auto]">
        <h2 class="text-lg font-bold text-[#1E293B]">
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
          class="mt-auto cursor-pointer px-8 shadow-sm"
          :loading="isLoading"
          :ui="{ label: ['mx-auto text-lg', isLoading && 'hidden'], leadingIcon: 'mx-auto' }"
          @click="handleSubmit"
        />
      </div>
      <div class="w-full">
        <h2 class="text-lg font-bold text-[#1E293B]">
          Nhận xét
        </h2>
        <div
          v-if="criteria.reviewerComment"
          class="mt-3 flex items-center gap-5 rounded-2xl bg-[#F8FAFC] p-3"
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
          class="mt-3 rounded-2xl bg-[#F8FAFC] p-4 text-sm text-[#64748B]"
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
  index?: number
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
