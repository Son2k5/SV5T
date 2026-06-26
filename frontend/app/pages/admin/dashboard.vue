<template>
  <div class="space-y-6 animate-fade-in duration-300">
    <!-- Header -->
    <AdminDashboardDashboardHeader
      :loading="loading"
      :last-updated="lastUpdated"
      @refresh="load"
      @export="exportData"
    />

    <!-- Error Banner -->
    <div
      v-if="error"
      class="flex items-center justify-between gap-4 p-4 bg-rose-50 border border-rose-100 rounded-2xl text-sm text-rose-700 shadow-2xs"
    >
      <div class="flex items-center gap-2">
        <UIcon
          name="i-lucide-alert-triangle"
          class="size-5 shrink-0 text-rose-500"
        />
        <span>{{ error }}</span>
      </div>
      <UButton
        color="rose"
        variant="subtle"
        label="Thử lại"
        icon="i-lucide-refresh-cw"
        class="rounded-xl font-bold cursor-pointer"
        @click="load"
      />
    </div>

    <!-- Skeleton Loaders during initial fetching -->
    <div
      v-if="loading && !data"
      class="space-y-6"
    >
      <!-- Stats skeleton -->
      <div class="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
        <div
          v-for="i in 8"
          :key="i"
          class="bg-white p-5 rounded-2xl border border-slate-100 shadow-2xs flex items-center justify-between"
        >
          <div class="space-y-3 w-2/3">
            <div class="h-3 w-16 bg-slate-100 rounded animate-pulse" />
            <div class="h-6 w-24 bg-slate-100 rounded animate-pulse" />
            <div class="h-3 w-32 bg-slate-100 rounded animate-pulse" />
          </div>
          <div class="size-11 rounded-xl bg-slate-100 animate-pulse" />
        </div>
      </div>

      <!-- Main body skeleton -->
      <div class="grid gap-6 lg:grid-cols-12">
        <div class="lg:col-span-8 space-y-6">
          <div class="bg-white p-6 rounded-2xl border border-slate-100 shadow-2xs h-96 animate-pulse" />
          <div class="bg-white p-6 rounded-2xl border border-slate-100 shadow-2xs h-64 animate-pulse" />
        </div>
        <div class="lg:col-span-4 space-y-6">
          <div class="bg-white p-6 rounded-2xl border border-slate-100 shadow-2xs h-48 animate-pulse" />
          <div class="bg-white p-6 rounded-2xl border border-slate-100 shadow-2xs h-64 animate-pulse" />
        </div>
      </div>
    </div>

    <!-- Dashboard Main Content -->
    <template v-else-if="data">
      <!-- Quick Statistics Cards Grid -->
      <div class="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
        <AdminDashboardStatCard
          label="Tổng người dùng"
          :value="data.stats.totalUsers"
          icon="i-lucide-users"
          tone="blue"
          trend="up"
          trend-value="+4.2%"
          description="Sinh viên, mentor và quản trị viên"
        />
        <AdminDashboardStatCard
          label="Tổng Campaign"
          :value="data.stats.totalCampaigns"
          icon="i-lucide-calendar"
          tone="violet"
          trend="neutral"
          trend-value="0%"
          description="Đợt xét tuyển học kỳ"
        />
        <AdminDashboardStatCard
          label="Tổng Tiêu chí"
          :value="data.stats.totalCriteria"
          icon="i-lucide-list-checks"
          tone="slate"
          trend="up"
          trend-value="+15"
          description="Tiêu chuẩn đánh giá"
        />
        <AdminDashboardStatCard
          label="Tổng Standard"
          :value="data.stats.totalStandards"
          icon="i-lucide-folder"
          tone="blue"
          trend="up"
          trend-value="+2"
          description="Nhóm chuẩn xét duyệt"
        />
        <AdminDashboardStatCard
          label="Hồ sơ chờ duyệt"
          :value="data.stats.pendingApplications"
          icon="i-lucide-clock"
          tone="amber"
          :trend="data.stats.pendingApplications > 5 ? 'up' : 'neutral'"
          :trend-value="data.stats.pendingApplications.toString()"
          description="Hồ sơ sinh viên đã nộp"
        />
        <AdminDashboardStatCard
          label="Hồ sơ đã duyệt"
          :value="data.stats.approvedApplications"
          icon="i-lucide-check-circle"
          tone="emerald"
          trend="up"
          trend-value="+8.4%"
          description="Hồ sơ đạt danh hiệu 5 Tốt"
        />
        <AdminDashboardStatCard
          label="Hồ sơ bị từ chối"
          :value="data.stats.rejectedApplications"
          icon="i-lucide-x-circle"
          tone="rose"
          trend="down"
          trend-value="-2.1%"
          description="Hồ sơ chưa đạt yêu cầu"
        />
        <AdminDashboardStatCard
          label="Tỷ lệ hoàn thành"
          :value="`${data.stats.passRate}%`"
          icon="i-lucide-sparkles"
          tone="emerald"
          trend="up"
          trend-value="Tốt"
          :description="`Từ chối: ${data.stats.failRate}%`"
        />
      </div>

      <!-- Main Layout: 12 Columns -->
      <div class="grid gap-6 lg:grid-cols-12 items-start">
        <!-- Left: SVG Charts & Quick Actions (8 Cols) -->
        <div class="lg:col-span-8 space-y-6">
          <!-- Charts Section -->
          <AdminDashboardChartCard
            :approved="data.stats.approvedApplications"
            :rejected="data.stats.rejectedApplications"
            :pending="data.stats.pendingApplications"
            :pass-rate="data.stats.passRate"
            :fail-rate="data.stats.failRate"
            :campaign-progress-list="data.campaignProgress"
          />

          <!-- Quick Actions Grid -->
          <AdminDashboardQuickActionCard />
        </div>

        <!-- Right: Notifications, Progress & Activities (4 Cols) -->
        <div class="lg:col-span-4 space-y-6">
          <!-- Notification Card -->
          <AdminDashboardNotificationCard
            :pending-evidences="data.stats.pendingEvidences"
            :campaigns="data.campaigns"
          />

          <!-- Campaign Progress List -->
          <AdminDashboardProgressCard
            :items="data.campaignProgress"
          />
        </div>
      </div>
    </template>

    <!-- Empty Dashboard State -->
    <CommonPageEmpty
      v-else
      title="Không thể nhận dữ liệu tổng quan"
      desc="Vui lòng thử tải lại hoặc kiểm tra kết nối mạng."
    >
      <UButton
        color="info"
        label="Tải lại Dashboard"
        icon="i-lucide-refresh-cw"
        class="cursor-pointer"
        @click="load"
      />
    </CommonPageEmpty>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { CampaignProgress, Campaign } from '~/types/admin'
import { useAdminDashboard } from '~/composables/admin/useAdminDashboard'
import type { ExtendedDashboardStats } from '~/composables/admin/useAdminDashboard'
import { getErrorMessage } from '~/utils/errors'

interface DashboardData {
  stats: ExtendedDashboardStats
  campaignProgress: CampaignProgress[]
  campaigns: Campaign[]
}

definePageMeta({ layout: 'admin', middleware: 'require-admin' })

const toast = useToast()
const { fetchDashboard } = useAdminDashboard()
const data = ref<DashboardData | null>(null)
const loading = ref(true)
const error = ref('')
const lastUpdated = ref<Date | null>(null)

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    data.value = await fetchDashboard()
    lastUpdated.value = new Date()
  }
  catch (cause) {
    error.value = getErrorMessage(cause, 'Không thể kết nối đến máy chủ. Vui lòng thử lại sau.')
    toast.add({
      title: 'Lỗi tải dữ liệu',
      description: error.value,
      color: 'danger',
    })
  }
  finally {
    loading.value = false
  }
}

// Client-side CSV Exporter
const exportData = () => {
  if (!data.value) return

  try {
    const stats = data.value.stats
    const rows = [
      ['Bao Cao Tong Quan He Thong Sinh Vien 5 Tot'],
      [`Ngay xuat: ${new Intl.DateTimeFormat('vi-VN', { dateStyle: 'short', timeStyle: 'medium' }).format(new Date())}`],
      [],
      ['Chi so', 'Gia tri'],
      ['Tong nguoi dung', stats.totalUsers],
      ['Tong Campaign', stats.totalCampaigns],
      ['Tong Tieu chi', stats.totalCriteria],
      ['Tong Standard', stats.totalStandards],
      ['Ho so cho duyet', stats.pendingApplications],
      ['Ho so da duyet', stats.approvedApplications],
      ['Ho so bi tu choi', stats.rejectedApplications],
      ['Ty le dat (%)', stats.passRate],
      ['Ty le truot (%)', stats.failRate],
      [],
      ['Tien do cac dot xet tuyen dang hoat dong'],
      ['Ten Campaign', 'Da xu ly', 'Tong so', 'Phan tram hoan thanh (%)'],
      ...(data.value.campaignProgress || []).map((cp: CampaignProgress) => [
        cp.campaignName,
        cp.reviewed,
        cp.total,
        `${cp.percent}%`,
      ]),
    ]

    const csvContent = 'data:text/csv;charset=utf-8,\uFEFF'
      + rows.map(e => e.map(val => `"${val.toString().replace(/"/g, '""')}"`).join(',')).join('\n')

    const encodedUri = encodeURI(csvContent)
    const link = document.createElement('a')
    link.setAttribute('href', encodedUri)
    link.setAttribute('download', `bao_cao_dashboard_5tot_${Date.now()}.csv`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    toast.add({
      title: 'Xuất báo cáo thành công',
      description: 'Tải xuống tập tin CSV báo cáo hoàn tất!',
      color: 'success',
    })
  }
  catch (err) {
    console.error(err)
    toast.add({
      title: 'Lỗi xuất báo cáo',
      description: 'Không thể xuất dữ liệu dạng CSV.',
      color: 'danger',
    })
  }
}

onMounted(load)
</script>
