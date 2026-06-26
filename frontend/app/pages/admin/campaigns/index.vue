<template>
  <div class="space-y-6">
    <!-- Header Page -->
    <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center ">
      <div>
        <h1 class="text-2xl font-bold text-slate-900 tracking-tight">
          Quản lý Đợt xét duyệt
        </h1>
        <p class="mt-1 text-sm text-slate-500">
          Thiết lập đợt xét duyệt, nhóm tiêu chuẩn và bộ tiêu chí đánh giá cho cả cá nhân và tập thể.
        </p>
      </div>

      <UButton
        color="info"
        icon="i-lucide-calendar-plus"
        label="Tạo đợt xét duyệt"
        class="cursor-pointer font-semibold rounded-xl px-4 py-2.5 shadow-sm"
        @click="openCreate"
      />
    </div>

    <!-- Stats Summary Section -->
    <div class="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
      <div class="bg-white p-5 rounded-2xl border border-slate-100 shadow-sm flex items-center gap-4">
        <div class="size-11 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center border border-blue-100/50">
          <UIcon
            name="i-lucide-calendar-range"
            class="size-5.5"
          />
        </div>
        <div>
          <p class="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
            Tổng đợt xét duyệt
          </p>
          <p class="text-2xl font-bold text-slate-900 mt-0.5">
            {{ pageData?.totalElements || 0 }}
          </p>
        </div>
      </div>

      <div class="bg-white p-5 rounded-2xl border border-slate-100 shadow-sm flex items-center gap-4">
        <div class="size-11 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center border border-emerald-100/50">
          <UIcon
            name="i-lucide-check-circle"
            class="size-5.5"
          />
        </div>
        <div>
          <p class="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
            Đang hoạt động
          </p>
          <p class="text-2xl font-bold text-slate-900 mt-0.5">
            {{ pageData?.content.filter(c => c.status === 'ACTIVE').length || 0 }}
          </p>
        </div>
      </div>

      <div class="bg-white p-5 rounded-2xl border border-slate-100 shadow-sm flex items-center gap-4">
        <div class="size-11 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center border border-amber-100/50">
          <UIcon
            name="i-lucide-lock"
            class="size-5.5"
          />
        </div>
        <div>
          <p class="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
            Nháp / Đã đóng
          </p>
          <p class="text-2xl font-bold text-slate-900 mt-0.5">
            {{ pageData?.content.filter(c => ['DRAFT', 'CLOSED'].includes(c.status)).length || 0 }}
          </p>
        </div>
      </div>

      <div class="bg-white p-5 rounded-2xl border border-slate-100 shadow-sm flex items-center gap-4">
        <div class="size-11 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center border border-purple-100/50">
          <UIcon
            name="i-lucide-users"
            class="size-5.5"
          />
        </div>
        <div>
          <p class="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
            Đợt tập thể
          </p>
          <p class="text-2xl font-bold text-slate-900 mt-0.5">
            {{ pageData?.content.filter(c => c.isGroup === 'GROUP' || c.isGroup === 'BOTH').length || 0 }}
          </p>
        </div>
      </div>
    </div>

    <!-- Campaigns Table Card -->
    <div class="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm space-y-6">
      <div class="flex flex-col gap-3 sm:flex-row sm:items-center justify-between">
        <USelect
          v-model="status"
          class="w-full sm:w-56"
          :items="statusOptions"
          placeholder="Tất cả trạng thái"
        />

        <p class="text-xs font-semibold text-slate-400">
          Hiển thị trang {{ uiPage }} của {{ pageData?.totalPages || 1 }}
        </p>
      </div>

      <CommonPageLoading v-if="loading && !pageData" />

      <CommonPageEmpty
        v-else-if="pageData && !pageData.content.length"
        title="Chưa có đợt xét duyệt"
        desc="Hãy tạo đợt xét duyệt đầu tiên để bắt đầu cấu hình tiêu chí."
      />

      <div
        v-else
        class="overflow-x-auto rounded-xl border border-slate-100"
      >
        <UTable
          :data="pageData?.content || []"
          :columns="columns"
          :loading="loading"
          class="w-full text-slate-700"
          :ui="{
            thead: 'bg-slate-50 border-b border-slate-100',
            th: 'py-3.5 text-slate-500 font-bold text-xs uppercase tracking-wider',
            td: 'py-4 text-sm align-middle',
          }"
        >
          <template #name-cell="{ row }">
            <div class="min-w-64 max-w-sm">
              <div class="flex flex-wrap items-center gap-2">
                <p class="font-semibold text-slate-900">
                  {{ row.original.name }}
                </p>
                <UBadge
                  :color="row.original.isGroup === 'BOTH' ? 'warning' : (row.original.isGroup === 'GROUP' ? 'info' : 'primary')"
                  variant="subtle"
                  class="rounded-md font-bold text-[9px] px-1.5 py-0.5"
                >
                  {{ row.original.isGroup === 'BOTH' ? 'Cả hai' : (row.original.isGroup === 'GROUP' ? 'Tập thể' : 'Cá nhân') }}
                </UBadge>
                <UBadge
                  color="neutral"
                  variant="subtle"
                  class="rounded-md font-bold text-[9px] px-1.5 py-0.5"
                >
                  {{ getLevelLabel(row.original.level) }}
                </UBadge>
              </div>
              <p class="mt-1 line-clamp-2 text-xs text-slate-400 leading-relaxed">
                {{ row.original.description || 'Không có mô tả' }}
              </p>
            </div>
          </template>

          <template #period-cell="{ row }">
            <div class="text-xs font-medium text-slate-600 space-y-0.5">
              <div class="flex items-center gap-1.5">
                <UIcon
                  name="i-lucide-calendar-days"
                  class="text-slate-400 size-3.5"
                />
                <span>{{ formatDate(row.original.startDate) }}</span>
              </div>
              <div class="flex items-center gap-1.5 text-slate-400">
                <UIcon
                  name="i-lucide-arrow-right"
                  class="size-3"
                />
                <span>đến {{ formatDate(row.original.endDate) }}</span>
              </div>
            </div>
          </template>

          <template #status-cell="{ row }">
            <UBadge
              :color="statusMeta[row.original.status].color"
              variant="subtle"
              class="rounded-lg font-semibold text-xs px-2 py-0.5"
            >
              {{ statusMeta[row.original.status].label }}
            </UBadge>
          </template>

          <template #criteriaCount-cell="{ row }">
            <div class="flex items-center gap-2 text-sm font-semibold text-slate-700">
              <span class="size-8 rounded-lg bg-blue-50/50 flex items-center justify-center border border-blue-100/20">
                <UIcon
                  name="i-lucide-list-checks"
                  class="text-blue-500 size-4"
                />
              </span>
              <span>{{ row.original.criteriaCount }}</span>
            </div>
          </template>

          <template #actions-cell="{ row }">
            <div class="flex justify-end gap-1.5 px-2">
              <UButton
                :to="`/admin/campaigns/${row.original.publicId}`"
                color="neutral"
                variant="ghost"
                icon="i-lucide-eye"
                class="size-8 rounded-lg cursor-pointer hover:bg-slate-50 text-slate-400 hover:text-slate-700"
                aria-label="Xem chi tiết"
              />
              <UButton
                :to="`/admin/campaigns/${row.original.publicId}?edit=1`"
                color="info"
                variant="ghost"
                icon="i-lucide-pen-line"
                class="size-8 rounded-lg cursor-pointer hover:bg-slate-50 text-slate-400 hover:text-slate-700"
                aria-label="Chỉnh sửa"
              />
              <UButton
                color="error"
                variant="ghost"
                icon="i-lucide-trash-2"
                class="size-8 rounded-lg cursor-pointer hover:bg-red-50 text-slate-400 hover:text-red-600"
                aria-label="Xóa"
                @click="askDelete(row.original)"
              />
            </div>
          </template>
        </UTable>
      </div>

      <!-- Pagination Block -->
      <div
        v-if="pageData"
        class="pt-4 flex flex-col justify-between gap-3 sm:flex-row sm:items-center border-t border-slate-100"
      >
        <p class="text-xs font-semibold text-slate-400">
          Tổng cộng {{ pageData.totalElements }} đợt xét duyệt
        </p>
        <UPagination
          v-model:page="uiPage"
          :total="pageData.totalElements"
          :items-per-page="pageData.size"
          class="scale-90 origin-right"
        />
      </div>
    </div>

    <!-- Modals -->
    <AdminCampaignsCampaignFormModal
      v-model:open="formOpen"
      :loading="submitting"
      @submit="createCampaign"
    />

    <AdminSharedConfirmDeleteModal
      v-model:open="deleteOpen"
      title="Lưu trữ đợt xét duyệt?"
      :description="`Đợt “${deletingCampaign?.name || ''}” sẽ được chuyển sang trạng thái lưu trữ.`"
      confirm-label="Lưu trữ"
      :loading="submitting"
      @confirm="confirmDelete"
    />
  </div>
</template>

<script setup lang="ts">
import type { TableColumn } from '@nuxt/ui'
import type {
  AdminPage,
  Campaign,
  CampaignForm,
  CampaignStatus,
} from '~/types/admin'
import { getErrorMessage } from '~/utils/errors'
import { useAdminCampaigns } from '~/composables/admin/useAdminCampaigns'

definePageMeta({ layout: 'admin', middleware: 'require-admin' })

const toast = useToast()
const {
  fetchCampaigns,
  createCampaign: create,
  deleteCampaign,
} = useAdminCampaigns()

const pageData = ref<AdminPage<Campaign> | null>(null)
const status = ref<CampaignStatus | undefined>()
const uiPage = ref(1)
const loading = ref(false)
const submitting = ref(false)
const formOpen = ref(false)
const deleteOpen = ref(false)
const deletingCampaign = ref<Campaign | null>(null)

const columns: TableColumn<Campaign>[] = [
  { accessorKey: 'name', header: 'Đợt xét duyệt' },
  { id: 'period', header: 'Thời gian' },
  { accessorKey: 'status', header: 'Trạng thái' },
  { accessorKey: 'criteriaCount', header: 'Tiêu chí' },
  { id: 'actions', header: '' },
]

const statusOptions = [
  { label: 'Tất cả trạng thái', value: undefined },
  { label: 'Nháp', value: 'DRAFT' },
  { label: 'Đang mở', value: 'ACTIVE' },
  { label: 'Đã đóng', value: 'CLOSED' },
  { label: 'Lưu trữ', value: 'ARCHIVED' },
]

const statusMeta: Record<CampaignStatus, { label: string, color: 'neutral' | 'success' | 'warning' | 'error' }> = {
  DRAFT: { label: 'Nháp', color: 'neutral' },
  ACTIVE: { label: 'Đang mở', color: 'success' },
  CLOSED: { label: 'Đã đóng', color: 'warning' },
  ARCHIVED: { label: 'Lưu trữ', color: 'error' },
}

const formatDate = (value: string | null) => {
  if (!value) return 'Chưa xác định'

  return new Intl.DateTimeFormat('vi-VN', {
    dateStyle: 'medium',
  }).format(new Date(`${value}T00:00:00`))
}

const getLevelLabel = (level: string) => {
  switch (level) {
    case 'UNIVERSITY':
      return 'Cấp trường'
    case 'CITY':
      return 'Cấp thành phố'
    case 'NATION':
      return 'Cấp trung ương'
    case 'UNI_CITY':
      return 'Trường & Thành phố'
    case 'UNI_NATION':
      return 'Trường & Trung ương'
    case 'CITY_NATION':
      return 'Thành phố & Trung ương'
    case 'ALL':
      return 'Cả ba cấp'
    default:
      return level
  }
}

const load = async () => {
  loading.value = true
  try {
    const response = await fetchCampaigns(uiPage.value - 1, status.value)
    pageData.value = response.data
  }
  finally {
    loading.value = false
  }
}

watch(status, () => {
  if (uiPage.value !== 1) uiPage.value = 1
  else load()
})
watch(uiPage, load)

const openCreate = () => {
  formOpen.value = true
}

const createCampaign = async (form: CampaignForm) => {
  submitting.value = true
  try {
    const response = await create(form)
    toast.add({ title: 'Đã tạo đợt xét duyệt', color: 'success' })
    await navigateTo(`/admin/campaigns/${response.data.publicId}`)
  }
  catch (cause) {
    toast.add({
      title: 'Không thể tạo đợt xét duyệt',
      description: getErrorMessage(cause, 'Vui lòng kiểm tra lại dữ liệu.'),
      color: 'error',
    })
  }
  finally {
    submitting.value = false
  }
}

const askDelete = (campaign: Campaign) => {
  deletingCampaign.value = campaign
  deleteOpen.value = true
}

const confirmDelete = async () => {
  if (!deletingCampaign.value) return

  submitting.value = true
  try {
    await deleteCampaign(deletingCampaign.value.publicId)
    deleteOpen.value = false
    toast.add({ title: 'Đã lưu trữ đợt xét duyệt', color: 'success' })
    await load()
  }
  catch (cause) {
    toast.add({
      title: 'Không thể lưu trữ',
      description: getErrorMessage(cause, 'Vui lòng thử lại.'),
      color: 'error',
    })
  }
  finally {
    submitting.value = false
  }
}

onMounted(load)
</script>
