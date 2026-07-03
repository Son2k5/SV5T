<template>
  <div class="space-y-6">
    <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
      <div>
        <h1 class="text-2xl font-bold text-[#1E293B]">
          Thong bao
        </h1>
        <p class="mt-1 text-sm text-[#64748B]">
          Theo doi cap nhat ve ho so, ket qua xet duyet va han nop.
        </p>
      </div>
      <UButton
        color="info"
        icon="i-lucide-check-check"
        label="Danh dau da doc tat ca"
        :disabled="unreadCount === 0"
        @click="readAll"
      />
    </div>

    <CommonPageSection inner-class="!block">
      <div class="mb-5 flex flex-col gap-3 sm:flex-row sm:items-center">
        <USelect
          v-model="selectedType"
          class="w-full sm:w-72"
          :items="typeOptions"
          placeholder="Tat ca thong bao"
        />
        <div class="text-sm text-[#64748B] sm:ml-auto">
          {{ unreadCount }} thong bao chua doc
        </div>
      </div>

      <div
        v-if="error"
        class="mb-4 flex flex-wrap items-center justify-between gap-3 rounded-2xl bg-red-50 p-4 text-sm text-red-700"
      >
        <span>{{ error }}</span>
        <UButton
          color="error"
          variant="soft"
          label="Thu lai"
          @click="load"
        />
      </div>

      <CommonPageLoading v-if="loading && !pageState.content.length" />
      <CommonPageEmpty
        v-else-if="!pageState.content.length"
        title="Chua co thong bao"
        desc="Cac cap nhat ve ho so va han nop se xuat hien tai day."
      />
      <template v-else>
        <div class="divide-y divide-[#E5E7EB] overflow-hidden rounded-2xl border border-[#E5E7EB] bg-white">
          <button
            v-for="notification in pageState.content"
            :key="notification.publicId"
            type="button"
            class="flex w-full gap-4 px-4 py-4 text-left transition hover:bg-blue-50/60 sm:px-5"
            @click="read(notification.publicId)"
          >
            <span
              class="mt-2 size-2.5 shrink-0 rounded-full"
              :class="notification.read ? 'bg-slate-200' : 'bg-[#2563EB]'"
            />
            <span class="min-w-0 flex-1">
              <span class="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                <span class="text-sm font-bold text-[#1E293B]">{{ notification.title }}</span>
                <span class="text-xs font-medium text-[#64748B]">{{ formatDate(notification.createdAt) }}</span>
              </span>
              <span class="mt-2 block text-sm leading-6 text-[#475569]">{{ notification.content }}</span>
              <span class="mt-3 inline-flex items-center rounded-full bg-blue-50 px-2.5 py-1 text-xs font-semibold text-[#2563EB]">
                {{ typeLabel(notification.type) }}
              </span>
            </span>
          </button>
        </div>

        <div class="mt-5 flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
          <p class="text-sm text-[#64748B]">
            Tong {{ pageState.totalElements }} thong bao
          </p>
          <UPagination
            v-model:page="uiPage"
            :total="pageState.totalElements"
            :items-per-page="pageState.size"
          />
        </div>
      </template>
    </CommonPageSection>
  </div>
</template>

<script setup lang="ts">
import type { NotificationType } from '~/types/notification'

definePageMeta({ layout: 'default' })

const {
  error,
  fetchNotifications,
  fetchUnreadCount,
  loading,
  markAllAsRead,
  markAsRead,
  pageState,
  unreadCount,
} = useNotifications()

const uiPage = ref(1)
const selectedType = ref<NotificationType | undefined>()

const typeOptions: Array<{ label: string, value?: NotificationType }> = [
  { label: 'Tat ca thong bao', value: undefined },
  { label: 'Da nhan ho so', value: 'SUBMISSION_RECEIVED' },
  { label: 'Da duyet', value: 'APPROVED' },
  { label: 'Bi tu choi', value: 'REJECTED' },
  { label: 'Nhac han', value: 'DEADLINE_REMINDER' },
  { label: 'He thong', value: 'SYSTEM_ALERT' },
]

const load = async () => {
  await Promise.all([
    fetchNotifications({ page: uiPage.value - 1, size: 10, type: selectedType.value }),
    fetchUnreadCount(),
  ])
}

const read = async (publicId: string) => {
  await markAsRead(publicId)
}

const readAll = async () => {
  await markAllAsRead()
}

const typeLabel = (type: NotificationType) =>
  typeOptions.find(option => option.value === type)?.label || type

const formatDate = (value: string) => {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return new Intl.DateTimeFormat('vi-VN', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(date)
}

watch(selectedType, () => {
  uiPage.value = 1
  void load()
})
watch(uiPage, () => {
  void load()
})

onMounted(load)
</script>
