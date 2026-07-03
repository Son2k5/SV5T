<template>
  <div class="space-y-6">
    <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
      <div>
        <h1 class="text-2xl font-bold text-[#1E293B]">
          Cấu hình hệ thống
        </h1>
        <p class="mt-1 text-sm text-[#64748B]">
          Chỉ quản lý các cấu hình nền chưa có màn hình riêng trong hệ thống.
        </p>
      </div>
      <div class="flex flex-wrap gap-3 sm:justify-end">
        <UButton
          color="neutral"
          variant="outline"
          icon="i-lucide-bell-ring"
          label="Notification"
          to="/admin/settings/notification"
        />
        <UButton
          color="info"
          icon="i-lucide-plus"
          label="Thêm key nâng cao"
          @click="openCreate"
        />
      </div>
    </div>

    

    <div class="grid gap-6 lg:grid-cols-[18rem_minmax(0,1fr)]">
      <CommonPageSection inner-class="!block p-2">
        <nav class="space-y-1">
          <button
            v-for="section in settingSections"
            :key="section.value"
            type="button"
            class="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left text-sm font-semibold transition"
            :class="activeSection === section.value ? 'bg-[#2563EB] text-white shadow-sm' : 'text-[#334155] hover:bg-blue-50 hover:text-[#2563EB]'"
            @click="activeSection = section.value"
          >
            <UIcon
              :name="section.icon"
              class="size-5 shrink-0"
            />
            <span class="min-w-0 flex-1 truncate">{{ section.label }}</span>
          </button>
        </nav>
      </CommonPageSection>

      <div class="min-w-0 space-y-6">
        <CommonPageLoading v-if="loading && !loaded" />

        <CommonPageSection
          v-else-if="error"
          title="Không thể tải cấu hình"
          title-icon="i-lucide-circle-alert"
          inner-class="flex-col items-start gap-4"
        >
          <p class="text-sm text-red-600">
            {{ error }}
          </p>
          <UButton
            color="info"
            label="Thử lại"
            @click="load"
          />
        </CommonPageSection>

        <template v-else>
          <CommonPageSection
            v-if="activeSection === 'general'"
            title="General - Cài đặt chung"
            title-icon="i-lucide-settings"
            inner-class="!block"
          >
            <div class="grid gap-5 lg:grid-cols-2">
              <UFormField label="Tên hệ thống">
                <UInput
                  v-model="generalForm.name"
                  placeholder="Sinh viên 5 Tốt"
                />
              </UFormField>
              <UFormField label="Tên viết tắt">
                <UInput
                  v-model="generalForm.shortName"
                  placeholder="SV5T"
                />
              </UFormField>
              <UFormField label="Logo URL">
                <UInput
                  v-model="generalForm.logoUrl"
                  placeholder="/logo5Merits.jpg"
                />
              </UFormField>
              <UFormField label="Favicon URL">
                <UInput
                  v-model="generalForm.faviconUrl"
                  placeholder="/favicon.ico"
                />
              </UFormField>
              <UFormField
                label="Banner trang chủ"
                class="lg:col-span-2"
              >
                <UInput
                  v-model="generalForm.homeBannerUrl"
                  placeholder="/bannerAuth.png"
                />
              </UFormField>
              <UFormField label="Email liên hệ">
                <UInput
                  v-model="generalForm.contactEmail"
                  type="email"
                  placeholder="support@hanu.edu.vn"
                />
              </UFormField>
              <UFormField label="Hotline">
                <UInput
                  v-model="generalForm.hotline"
                  placeholder="024..."
                />
              </UFormField>
              <UFormField
                label="Địa chỉ đơn vị"
                class="lg:col-span-2"
              >
                <UTextarea
                  v-model="generalForm.address"
                  :rows="2"
                />
              </UFormField>
              <UFormField label="Múi giờ">
                <USelect
                  v-model="generalForm.timezone"
                  :items="timezoneOptions"
                />
              </UFormField>
              <UFormField label="Ngôn ngữ mặc định">
                <USelect
                  v-model="generalForm.defaultLanguage"
                  :items="languageOptions"
                />
              </UFormField>
              <div class="rounded-2xl border border-[#E5E7EB] p-4">
                <UCheckbox
                  v-model="generalForm.maintenanceEnabled"
                  label="Bật maintenance mode"
                />
                <p class="mt-2 text-sm text-[#64748B]">
                  Khi bật, hệ thống sẽ hiển thị thông báo bảo trì cho user.
                </p>
              </div>
              <UFormField label="Thông báo bảo trì">
                <UTextarea
                  v-model="generalForm.maintenanceMessage"
                  :rows="3"
                  placeholder="Hệ thống đang bảo trì..."
                />
              </UFormField>
              <UFormField
                label="Footer / copyright"
                class="lg:col-span-2"
              >
                <UInput
                  v-model="generalForm.footerText"
                  placeholder="© HANU"
                />
              </UFormField>
            </div>
            <SettingsSaveBar
              :loading="submittingSection === 'general'"
              @save="saveGeneral"
            />
          </CommonPageSection>

          <CommonPageSection
            v-else-if="activeSection === 'academic'"
            title="Academic Term - Năm học & Học kỳ"
            title-icon="i-lucide-graduation-cap"
            inner-class="!block"
          >
            <div class="grid gap-5 lg:grid-cols-2">
              <UFormField label="Học kỳ hiện tại">
                <UInput
                  v-model="academicForm.currentTerm"
                  placeholder="2025-2026 HK1"
                />
              </UFormField>
              <div class="rounded-2xl border border-[#E5E7EB] p-4">
                <UCheckbox
                  v-model="academicForm.lockClosedYears"
                  label="Khóa dữ liệu năm học đã đóng"
                />
              </div>
              <UFormField label="Ngày bắt đầu năm học mặc định">
                <UInput
                  v-model="academicForm.defaultStartDate"
                  type="date"
                />
              </UFormField>
              <UFormField label="Ngày kết thúc năm học mặc định">
                <UInput
                  v-model="academicForm.defaultEndDate"
                  type="date"
                />
              </UFormField>
            </div>

            <div class="mt-6 rounded-2xl border border-[#E5E7EB]">
              <div class="grid gap-3 border-b border-[#E5E7EB] p-4 lg:grid-cols-[1fr_10rem_10rem_9rem_auto]">
                <UInput
                  v-model="academicYearDraft.label"
                  placeholder="2025-2026"
                />
                <UInput
                  v-model="academicYearDraft.startDate"
                  type="date"
                />
                <UInput
                  v-model="academicYearDraft.endDate"
                  type="date"
                />
                <USelect
                  v-model="academicYearDraft.status"
                  :items="academicYearStatusOptions"
                />
                <UButton
                  color="info"
                  icon="i-lucide-plus"
                  label="Thêm"
                  @click="addAcademicYear"
                />
              </div>
              <div class="divide-y divide-[#E5E7EB]">
                <div
                  v-for="(year, index) in academicYears"
                  :key="`${year.label}-${index}`"
                  class="grid gap-3 p-4 lg:grid-cols-[1fr_10rem_10rem_9rem_auto]"
                >
                  <UInput v-model="year.label" />
                  <UInput
                    v-model="year.startDate"
                    type="date"
                  />
                  <UInput
                    v-model="year.endDate"
                    type="date"
                  />
                  <USelect
                    v-model="year.status"
                    :items="academicYearStatusOptions"
                  />
                  <UButton
                    color="error"
                    variant="ghost"
                    icon="i-lucide-trash-2"
                    aria-label="Xóa năm học"
                    @click="academicYears.splice(index, 1)"
                  />
                </div>
                <div
                  v-if="!academicYears.length"
                  class="p-6 text-center text-sm text-[#64748B]"
                >
                  Chưa có năm học nào.
                </div>
              </div>
            </div>
            <SettingsSaveBar
              :loading="submittingSection === 'academic'"
              @save="saveAcademic"
            />
          </CommonPageSection>

          <CommonPageSection
            v-else-if="activeSection === 'campaignPolicy'"
            title="Campaign - Mặc định đợt xét"
            title-icon="i-lucide-calendar-range"
            inner-class="!block"
          >
            <p class="mb-5 text-sm text-[#64748B]">
              Tạo/sửa từng đợt xét vẫn nằm ở tab Campaign; phần này chỉ lưu policy mặc định khi mở đợt mới.
            </p>
            <div class="grid gap-5 lg:grid-cols-2">
              <UFormField label="Số cấp duyệt mặc định">
                <USelect
                  v-model="campaignPolicyForm.approvalLevels"
                  :items="approvalLevelOptions"
                />
              </UFormField>
              <UFormField label="Thời hạn mặc định (ngày)">
                <UInput
                  v-model.number="campaignPolicyForm.defaultDeadlineDays"
                  type="number"
                  min="1"
                />
              </UFormField>
              <div class="rounded-2xl border border-[#E5E7EB] p-4">
                <UCheckbox
                  v-model="campaignPolicyForm.allowEditAfterSubmit"
                  label="Cho sửa hồ sơ sau khi nộp"
                />
              </div>
              <div class="rounded-2xl border border-[#E5E7EB] p-4">
                <UCheckbox
                  v-model="campaignPolicyForm.allowResubmitAfterRejected"
                  label="Cho nộp lại sau khi bị từ chối"
                />
              </div>
              <div class="rounded-2xl border border-[#E5E7EB] p-4">
                <UCheckbox
                  v-model="campaignPolicyForm.autoRejectAfterDeadline"
                  label="Tự động từ chối khi quá hạn"
                />
              </div>
              <div class="rounded-2xl border border-[#E5E7EB] p-4">
                <UCheckbox
                  v-model="campaignPolicyForm.allowLateSubmission"
                  label="Cho nộp trễ có lý do"
                />
              </div>
              <UFormField label="Grace period (ngày)">
                <UInput
                  v-model.number="campaignPolicyForm.gracePeriodDays"
                  type="number"
                  min="0"
                />
              </UFormField>
            </div>
            <SettingsSaveBar
              :loading="submittingSection === 'campaignPolicy'"
              @save="saveCampaignPolicy"
            />
          </CommonPageSection>

          <CommonPageSection
            v-else-if="activeSection === 'evidencePolicy'"
            title="Evidence - Cấu hình minh chứng"
            title-icon="i-lucide-file-up"
            inner-class="!block"
          >
            <p class="mb-5 text-sm text-[#64748B]">
              Duyệt minh chứng nằm ở tab Evidence; yêu cầu minh chứng theo từng tiêu chí nằm ở tab Criteria.
            </p>
            <div class="grid gap-5 lg:grid-cols-2">
              <UFormField label="Dung lượng tối đa mỗi file (MB)">
                <UInput
                  v-model.number="evidencePolicyForm.maxFileSizeMb"
                  type="number"
                  min="1"
                />
              </UFormField>
              <UFormField label="Định dạng cho phép">
                <UInput
                  v-model="evidencePolicyForm.allowedFormats"
                  placeholder="pdf,jpg,jpeg,png,docx"
                />
              </UFormField>
              <UFormField label="Số file tối đa mỗi minh chứng">
                <UInput
                  v-model.number="evidencePolicyForm.maxFilesPerEvidence"
                  type="number"
                  min="1"
                />
              </UFormField>
              <UFormField label="Số file tối đa mỗi hồ sơ">
                <UInput
                  v-model.number="evidencePolicyForm.maxFilesPerApplication"
                  type="number"
                  min="1"
                />
              </UFormField>
              <div class="rounded-2xl border border-[#E5E7EB] p-4">
                <UCheckbox
                  v-model="evidencePolicyForm.watermarkEnabled"
                  label="Bật watermark file"
                />
              </div>
              <div class="rounded-2xl border border-[#E5E7EB] p-4">
                <UCheckbox
                  v-model="evidencePolicyForm.duplicateCheckEnabled"
                  label="Kiểm tra trùng lặp file"
                />
              </div>
            </div>
            <SettingsSaveBar
              :loading="submittingSection === 'evidencePolicy'"
              @save="saveEvidencePolicy"
            />
          </CommonPageSection>

          <CommonPageSection
            v-else-if="activeSection === 'security'"
            title="Security - Bảo mật"
            title-icon="i-lucide-shield-check"
            inner-class="!block"
          >
            <div class="grid gap-5 lg:grid-cols-2">
              <UFormField label="JWT hết hạn sau (phút)">
                <UInput
                  v-model.number="securityForm.jwtAccessExpirationMinutes"
                  type="number"
                  min="1"
                />
              </UFormField>
              <UFormField label="Refresh token hết hạn sau (ngày)">
                <UInput
                  v-model.number="securityForm.refreshTokenExpirationDays"
                  type="number"
                  min="1"
                />
              </UFormField>
              <UFormField label="Số lần đăng nhập sai tối đa">
                <UInput
                  v-model.number="securityForm.maxLoginAttempts"
                  type="number"
                  min="1"
                />
              </UFormField>
              <UFormField label="Độ dài mật khẩu tối thiểu">
                <UInput
                  v-model.number="securityForm.passwordMinLength"
                  type="number"
                  min="8"
                />
              </UFormField>
              <UFormField label="Giới hạn upload / phút">
                <UInput
                  v-model.number="securityForm.uploadRateLimitPerMinute"
                  type="number"
                  min="1"
                />
              </UFormField>
              <UFormField label="Rate limit API / phút">
                <UInput
                  v-model.number="securityForm.apiRateLimitPerMinute"
                  type="number"
                  min="1"
                />
              </UFormField>
              <UFormField label="Bắt đổi mật khẩu sau (ngày, 0 = tắt)">
                <UInput
                  v-model.number="securityForm.forcePasswordChangeDays"
                  type="number"
                  min="0"
                />
              </UFormField>
              <div class="rounded-2xl border border-[#E5E7EB] p-4">
                <UCheckbox
                  v-model="securityForm.passwordRequireSpecial"
                  label="Yêu cầu ký tự đặc biệt"
                />
              </div>
              <div class="rounded-2xl border border-[#E5E7EB] p-4 lg:col-span-2">
                <UCheckbox
                  v-model="securityForm.twoFactorEnabled"
                  label="Bật 2FA khi hệ thống hỗ trợ"
                />
              </div>
            </div>
            <SettingsSaveBar
              :loading="submittingSection === 'security'"
              @save="saveSecurity"
            />
          </CommonPageSection>

          <CommonPageSection
            v-else-if="activeSection === 'storage'"
            title="Storage & Integration - Lưu trữ & Tích hợp"
            title-icon="i-lucide-cloud"
            inner-class="!block"
          >
            <div class="grid gap-5 lg:grid-cols-2">
              <UFormField label="Nơi lưu file">
                <USelect
                  v-model="storageForm.provider"
                  :items="storageProviderOptions"
                />
              </UFormField>
              <UFormField label="Credential ref / secret alias">
                <UInput
                  v-model="storageForm.credentialRef"
                  placeholder="cloudinary.main"
                />
              </UFormField>
              <UFormField label="Dung lượng tổng (GB)">
                <UInput
                  v-model.number="storageForm.maxTotalGb"
                  type="number"
                  min="1"
                />
              </UFormField>
              <UFormField label="Dung lượng mỗi user (MB)">
                <UInput
                  v-model.number="storageForm.maxPerUserMb"
                  type="number"
                  min="1"
                />
              </UFormField>
              <div class="rounded-2xl border border-[#E5E7EB] p-4">
                <UCheckbox
                  v-model="storageForm.ssoEnabled"
                  label="Bật SSO"
                />
              </div>
              <div class="rounded-2xl border border-[#E5E7EB] p-4">
                <UCheckbox
                  v-model="storageForm.ldapEnabled"
                  label="Bật LDAP"
                />
              </div>
            </div>
            <p class="mt-4 text-sm text-[#64748B]">
              Không lưu secret thô tại đây. Hãy dùng secret alias hoặc credential đã mã hóa ở backend.
            </p>
            <SettingsSaveBar
              :loading="submittingSection === 'storage'"
              @save="saveStorage"
            />
          </CommonPageSection>

          <CommonPageSection
            v-else-if="activeSection === 'audit'"
            title="Audit & System Log - Nhật ký hệ thống"
            title-icon="i-lucide-history"
            inner-class="!block"
          >
            <div class="mb-5 grid gap-4 lg:grid-cols-[1fr_1fr_10rem_auto_auto] lg:items-end">
              <UFormField label="Module / entity">
                <UInput
                  v-model="auditFilters.entity"
                  placeholder="User, Campaign..."
                />
              </UFormField>
              <UFormField label="Hành động">
                <UInput
                  v-model="auditFilters.action"
                  placeholder="CREATE, UPDATE..."
                />
              </UFormField>
              <UFormField label="User ID">
                <UInput
                  v-model.number="auditFilters.userId"
                  type="number"
                  min="1"
                />
              </UFormField>
              <UButton
                color="info"
                icon="i-lucide-search"
                label="Lọc"
                :loading="auditLoading"
                @click="loadAuditLogs"
              />
              <UButton
                color="neutral"
                variant="outline"
                icon="i-lucide-download"
                label="Export"
                :disabled="!auditPageData?.content.length"
                @click="exportAuditLogs"
              />
            </div>

            <div class="mb-5 grid gap-5 lg:grid-cols-2">
              <UFormField label="Lưu log trong (ngày)">
                <UInput
                  v-model.number="auditSettingsForm.retentionDays"
                  type="number"
                  min="1"
                />
              </UFormField>
              <div class="rounded-2xl border border-[#E5E7EB] p-4">
                <UCheckbox
                  v-model="auditSettingsForm.systemErrorAlertEnabled"
                  label="Gửi cảnh báo lỗi hệ thống cho admin"
                />
              </div>
            </div>
            <SettingsSaveBar
              :loading="submittingSection === 'auditSettings'"
              @save="saveAuditSettings"
            />

            <UTable
              :data="auditPageData?.content || []"
              :columns="auditColumns"
              :loading="auditLoading"
              class="mt-5 w-full"
            >
              <template #actor-cell="{ row }">
                <p class="text-sm font-semibold text-[#1E293B]">
                  {{ row.original.actorEmail || `#${row.original.actorId || '-'}` }}
                </p>
              </template>
              <template #createdAt-cell="{ row }">
                <span class="text-sm text-[#64748B]">{{ formatDateTime(row.original.createdAt) }}</span>
              </template>
            </UTable>
            <div
              v-if="auditPageData"
              class="mt-4 flex flex-col justify-between gap-3 sm:flex-row sm:items-center"
            >
              <p class="text-sm text-[#64748B]">
                Tổng {{ auditPageData.totalElements }} log
              </p>
              <UPagination
                v-model:page="auditUiPage"
                :total="auditPageData.totalElements"
                :items-per-page="auditPageData.size"
              />
            </div>
          </CommonPageSection>
        </template>
      </div>
    </div>

    <UModal
      :open="formOpen"
      :title="editing ? 'Cập nhật cấu hình' : 'Tạo cấu hình'"
      @update:open="formOpen = $event"
    >
      <template #body>
        <form
          class="space-y-4"
          @submit.prevent="save"
        >
          <UFormField
            label="Khóa cấu hình"
            name="keyName"
            :error="formErrors.keyName"
          >
            <UInput
              v-model="form.keyName"
              :disabled="Boolean(editing)"
              placeholder="Ví dụ: system.name"
            />
          </UFormField>
          <UFormField
            label="Giá trị"
            name="value"
            :error="formErrors.value"
          >
            <UTextarea
              v-model="form.value"
              :rows="4"
              placeholder="Giá trị cấu hình"
            />
          </UFormField>
          <UFormField
            label="Mô tả"
            name="description"
          >
            <UTextarea
              v-model="form.description"
              :rows="3"
              placeholder="Mô tả ý nghĩa của cấu hình"
            />
          </UFormField>
        </form>
      </template>
      <template #footer>
        <div class="flex w-full justify-end gap-3">
          <UButton
            color="neutral"
            variant="outline"
            label="Hủy"
            :disabled="submitting"
            @click="formOpen = false"
          />
          <UButton
            color="info"
            :loading="submitting"
            :label="editing ? 'Lưu thay đổi' : 'Tạo cấu hình'"
            @click="save"
          />
        </div>
      </template>
    </UModal>

    <AdminSharedConfirmDeleteModal
      v-model:open="deleteOpen"
      title="Xóa cấu hình?"
      :description="`Cấu hình ${deleting?.keyName || ''} sẽ bị xóa vĩnh viễn.`"
      :loading="submitting"
      @confirm="confirmDelete"
    />
  </div>
</template>

<script setup lang="ts">
import type { TableColumn } from '@nuxt/ui'
import type { AdminPage, AuditLog, SystemSetting, SystemSettingForm } from '~/types/admin'
import { useAdminAuditLogs } from '~/composables/admin/useAdminAuditLogs'
import { useAdminSettings } from '~/composables/admin/useAdminSettings'
import { getErrorMessage } from '~/utils/errors'

definePageMeta({ layout: 'admin', middleware: 'require-admin' })

type SectionValue
  = 'general'
    | 'academic'
    | 'campaignPolicy'
    | 'evidencePolicy'
    | 'security'
    | 'storage'
    | 'audit'
    | 'advanced'

type AcademicYearStatus = 'OPEN' | 'CLOSED'

interface AcademicYearSetting {
  label: string
  startDate: string
  endDate: string
  status: AcademicYearStatus
}

const toast = useToast()
const { fetchSettings, createSetting, updateSetting, deleteSetting } = useAdminSettings()
const { fetchAuditLogs } = useAdminAuditLogs()

const delegatedModules = [
  { title: 'Criteria CRUD', desc: 'Tiêu chí, tiêu chí phụ, yêu cầu minh chứng theo tiêu chí.', icon: 'i-lucide-list-checks', to: '/admin/criteria' },
  { title: 'Campaign CRUD', desc: 'Tạo đợt xét, mở/đóng, phạm vi, thời hạn từng đợt.', icon: 'i-lucide-calendar-range', to: '/admin/campaigns' },
  { title: 'Notification', desc: 'SMTP, realtime, reminder và template thông báo.', icon: 'i-lucide-bell-ring', to: '/admin/settings/notification' },
  { title: 'User & Role', desc: 'Tạo user, đổi role và trạng thái tài khoản.', icon: 'i-lucide-users', to: '/admin/users' },
]

const settingSections: Array<{ label: string, value: SectionValue, icon: string }> = [
  { label: 'General', value: 'general', icon: 'i-lucide-settings' },
  { label: 'Academic Term', value: 'academic', icon: 'i-lucide-graduation-cap' },
  { label: 'Campaign Defaults', value: 'campaignPolicy', icon: 'i-lucide-calendar-range' },
  { label: 'Evidence Policy', value: 'evidencePolicy', icon: 'i-lucide-file-up' },
  { label: 'Security', value: 'security', icon: 'i-lucide-shield-check' },
  { label: 'Storage & Integration', value: 'storage', icon: 'i-lucide-cloud' },
  { label: 'Audit & System Log', value: 'audit', icon: 'i-lucide-history' },
  { label: 'Key value nâng cao', value: 'advanced', icon: 'i-lucide-database' },
]

const timezoneOptions = [
  { label: 'Asia/Ho_Chi_Minh', value: 'Asia/Ho_Chi_Minh' },
  { label: 'UTC', value: 'UTC' },
]
const languageOptions = [
  { label: 'Tiếng Việt', value: 'vi' },
  { label: 'English', value: 'en' },
]
const academicYearStatusOptions: Array<{ label: string, value: AcademicYearStatus }> = [
  { label: 'Đang mở', value: 'OPEN' },
  { label: 'Đã đóng', value: 'CLOSED' },
]
const approvalLevelOptions = [
  { label: '1 cấp', value: 'ONE_LEVEL' },
  { label: 'Nhiều cấp: cố vấn -> khoa -> đoàn trường', value: 'MULTI_LEVEL' },
]
const storageProviderOptions = [
  { label: 'Local FileStorageService', value: 'LOCAL' },
  { label: 'Cloudinary', value: 'CLOUDINARY' },
]

const activeSection = ref<SectionValue>('general')
const settings = ref<SystemSetting[]>([])
const loading = ref(false)
const loaded = ref(false)
const submitting = ref(false)
const submittingSection = ref<SectionValue | 'auditSettings' | ''>('')
const error = ref('')

const search = ref('')
const selectedCategory = ref('all')
const formOpen = ref(false)
const deleteOpen = ref(false)
const editing = ref<SystemSetting | null>(null)
const deleting = ref<SystemSetting | null>(null)
const form = reactive<SystemSettingForm>({ keyName: '', value: '', description: '' })
const formErrors = reactive<Partial<Record<'keyName' | 'value', string>>>({})

const generalForm = reactive({
  name: 'Sinh viên 5 Tốt',
  shortName: 'SV5T',
  logoUrl: '/logo5Merits.jpg',
  faviconUrl: '/favicon.ico',
  homeBannerUrl: '/bannerAuth.png',
  contactEmail: '',
  hotline: '',
  address: '',
  timezone: 'Asia/Ho_Chi_Minh',
  defaultLanguage: 'vi',
  maintenanceEnabled: false,
  maintenanceMessage: '',
  footerText: '© HANU',
})

const academicForm = reactive({
  currentTerm: '',
  lockClosedYears: true,
  defaultStartDate: '',
  defaultEndDate: '',
})
const academicYearDraft = reactive<AcademicYearSetting>({
  label: '',
  startDate: '',
  endDate: '',
  status: 'OPEN',
})
const academicYears = ref<AcademicYearSetting[]>([])

const campaignPolicyForm = reactive({
  approvalLevels: 'ONE_LEVEL',
  defaultDeadlineDays: 30,
  allowEditAfterSubmit: false,
  allowResubmitAfterRejected: true,
  autoRejectAfterDeadline: false,
  allowLateSubmission: false,
  gracePeriodDays: 0,
})

const evidencePolicyForm = reactive({
  maxFileSizeMb: 10,
  allowedFormats: 'pdf,jpg,jpeg,png,docx',
  maxFilesPerEvidence: 3,
  maxFilesPerApplication: 30,
  watermarkEnabled: false,
  duplicateCheckEnabled: false,
})

const securityForm = reactive({
  jwtAccessExpirationMinutes: 30,
  refreshTokenExpirationDays: 7,
  maxLoginAttempts: 5,
  passwordMinLength: 8,
  passwordRequireSpecial: false,
  uploadRateLimitPerMinute: 20,
  apiRateLimitPerMinute: 120,
  forcePasswordChangeDays: 0,
  twoFactorEnabled: false,
})

const storageForm = reactive({
  provider: 'CLOUDINARY',
  credentialRef: '',
  maxTotalGb: 100,
  maxPerUserMb: 500,
  ssoEnabled: false,
  ldapEnabled: false,
})

const auditSettingsForm = reactive({
  retentionDays: 365,
  systemErrorAlertEnabled: true,
})

const auditFilters = reactive<{ entity: string, action: string, userId?: number }>({
  entity: '',
  action: '',
  userId: undefined,
})
const auditUiPage = ref(1)
const auditLoading = ref(false)
const auditPageData = ref<AdminPage<AuditLog> | null>(null)

const columns: TableColumn<SystemSetting>[] = [
  { id: 'category', header: 'Nhóm' },
  { accessorKey: 'keyName', header: 'Khóa' },
  { accessorKey: 'value', header: 'Giá trị' },
  { accessorKey: 'description', header: 'Mô tả' },
  { id: 'actions', header: '' },
]
const auditColumns: TableColumn<AuditLog>[] = [
  { accessorKey: 'entity', header: 'Module' },
  { accessorKey: 'action', header: 'Hành động' },
  { id: 'actor', header: 'Người thao tác' },
  { accessorKey: 'oldValue', header: 'Trước' },
  { accessorKey: 'newValue', header: 'Sau' },
  { accessorKey: 'createdAt', header: 'Thời gian' },
]

const settingsMap = computed(() => new Map(settings.value.map(setting => [setting.keyName, setting])))
const isScoringSetting = (setting: Pick<SystemSetting, 'keyName'>) => setting.keyName.toLowerCase().startsWith('scoring.')
const visibleSettings = computed(() => settings.value.filter(setting => !isScoringSetting(setting)))

const getValue = (key: string, fallback = '') =>
  settingsMap.value.get(key)?.value ?? fallback

const getBool = (key: string, fallback = false) => {
  const value = getValue(key, String(fallback))
  return value === 'true'
}

const getNumber = (key: string, fallback: number) => {
  const value = Number(getValue(key, String(fallback)))
  return Number.isFinite(value) ? value : fallback
}

function getJson<T>(key: string, fallback: T): T {
  try {
    return JSON.parse(getValue(key, JSON.stringify(fallback))) as T
  }
  catch {
    return fallback
  }
}

const hydrateForms = () => {
  Object.assign(generalForm, {
    name: getValue('system.name', 'Sinh viên 5 Tốt'),
    shortName: getValue('system.shortName', 'SV5T'),
    logoUrl: getValue('system.logoUrl', '/logo5Merits.jpg'),
    faviconUrl: getValue('system.faviconUrl', '/favicon.ico'),
    homeBannerUrl: getValue('system.homeBannerUrl', '/bannerAuth.png'),
    contactEmail: getValue('system.contactEmail'),
    hotline: getValue('system.hotline'),
    address: getValue('system.address'),
    timezone: getValue('system.timezone', 'Asia/Ho_Chi_Minh'),
    defaultLanguage: getValue('system.defaultLanguage', 'vi'),
    maintenanceEnabled: getBool('system.maintenance.enabled'),
    maintenanceMessage: getValue('system.maintenance.message'),
    footerText: getValue('system.footerText', '© HANU'),
  })
  Object.assign(academicForm, {
    currentTerm: getValue('academic.currentTerm'),
    lockClosedYears: getBool('academic.lockClosedYears', true),
    defaultStartDate: getValue('academic.defaultStartDate'),
    defaultEndDate: getValue('academic.defaultEndDate'),
  })
  academicYears.value = getJson<AcademicYearSetting[]>('academic.years', [])
  Object.assign(campaignPolicyForm, {
    approvalLevels: getValue('campaign.defaultApprovalLevels', 'ONE_LEVEL'),
    defaultDeadlineDays: getNumber('campaign.defaultDeadlineDays', 30),
    allowEditAfterSubmit: getBool('campaign.allowEditAfterSubmit'),
    allowResubmitAfterRejected: getBool('campaign.allowResubmitAfterRejected', true),
    autoRejectAfterDeadline: getBool('campaign.autoRejectAfterDeadline'),
    allowLateSubmission: getBool('campaign.allowLateSubmission'),
    gracePeriodDays: getNumber('campaign.gracePeriodDays', 0),
  })
  Object.assign(evidencePolicyForm, {
    maxFileSizeMb: getNumber('evidence.maxFileSizeMb', 10),
    allowedFormats: getValue('evidence.allowedFormats', 'pdf,jpg,jpeg,png,docx'),
    maxFilesPerEvidence: getNumber('evidence.maxFilesPerEvidence', 3),
    maxFilesPerApplication: getNumber('evidence.maxFilesPerApplication', 30),
    watermarkEnabled: getBool('evidence.watermarkEnabled'),
    duplicateCheckEnabled: getBool('evidence.duplicateCheckEnabled'),
  })
  Object.assign(securityForm, {
    jwtAccessExpirationMinutes: getNumber('security.jwtAccessExpirationMinutes', 30),
    refreshTokenExpirationDays: getNumber('security.refreshTokenExpirationDays', 7),
    maxLoginAttempts: getNumber('security.maxLoginAttempts', 5),
    passwordMinLength: getNumber('security.passwordMinLength', 8),
    passwordRequireSpecial: getBool('security.passwordRequireSpecial'),
    uploadRateLimitPerMinute: getNumber('security.uploadRateLimitPerMinute', 20),
    apiRateLimitPerMinute: getNumber('security.apiRateLimitPerMinute', 120),
    forcePasswordChangeDays: getNumber('security.forcePasswordChangeDays', 0),
    twoFactorEnabled: getBool('security.twoFactorEnabled'),
  })
  Object.assign(storageForm, {
    provider: getValue('storage.provider', 'CLOUDINARY'),
    credentialRef: getValue('storage.credentialRef'),
    maxTotalGb: getNumber('storage.maxTotalGb', 100),
    maxPerUserMb: getNumber('storage.maxPerUserMb', 500),
    ssoEnabled: getBool('integration.ssoEnabled'),
    ldapEnabled: getBool('integration.ldapEnabled'),
  })
  Object.assign(auditSettingsForm, {
    retentionDays: getNumber('audit.retentionDays', 365),
    systemErrorAlertEnabled: getBool('audit.systemErrorAlertEnabled', true),
  })
}

const settingPayload = (keyName: string, value: string | number | boolean, description: string) => ({
  keyName,
  value: String(value),
  description,
})

const saveSettingsBatch = async (
  section: SectionValue | 'auditSettings',
  entries: Array<{ keyName: string, value: string | number | boolean, description: string }>,
) => {
  submittingSection.value = section
  try {
    for (const entry of entries) {
      const existing = settingsMap.value.get(entry.keyName)
      if (existing) {
        await updateSetting(entry.keyName, { value: String(entry.value), description: entry.description })
      }
      else {
        await createSetting({
          keyName: entry.keyName,
          value: String(entry.value),
          description: entry.description,
        })
      }
    }
    toast.add({ title: 'Đã lưu cấu hình', color: 'success' })
    await load()
  }
  catch (cause) {
    toast.add({
      title: 'Không thể lưu cấu hình',
      description: getErrorMessage(cause, 'Vui lòng thử lại.'),
      color: 'error',
    })
  }
  finally {
    submittingSection.value = ''
  }
}

const saveGeneral = () => saveSettingsBatch('general', [
  settingPayload('system.name', generalForm.name, 'Tên hệ thống'),
  settingPayload('system.shortName', generalForm.shortName, 'Tên viết tắt hệ thống'),
  settingPayload('system.logoUrl', generalForm.logoUrl, 'Logo hệ thống'),
  settingPayload('system.faviconUrl', generalForm.faviconUrl, 'Favicon hệ thống'),
  settingPayload('system.homeBannerUrl', generalForm.homeBannerUrl, 'Banner trang chủ'),
  settingPayload('system.contactEmail', generalForm.contactEmail, 'Email liên hệ'),
  settingPayload('system.hotline', generalForm.hotline, 'Hotline liên hệ'),
  settingPayload('system.address', generalForm.address, 'Địa chỉ đơn vị'),
  settingPayload('system.timezone', generalForm.timezone, 'Múi giờ mặc định'),
  settingPayload('system.defaultLanguage', generalForm.defaultLanguage, 'Ngôn ngữ mặc định'),
  settingPayload('system.maintenance.enabled', generalForm.maintenanceEnabled, 'Bật/tắt maintenance mode'),
  settingPayload('system.maintenance.message', generalForm.maintenanceMessage, 'Thông báo maintenance cho user'),
  settingPayload('system.footerText', generalForm.footerText, 'Footer/copyright'),
])

const saveAcademic = () => saveSettingsBatch('academic', [
  settingPayload('academic.currentTerm', academicForm.currentTerm, 'Học kỳ hiện tại'),
  settingPayload('academic.lockClosedYears', academicForm.lockClosedYears, 'Khóa dữ liệu năm học đã đóng'),
  settingPayload('academic.defaultStartDate', academicForm.defaultStartDate, 'Ngày bắt đầu năm học mặc định'),
  settingPayload('academic.defaultEndDate', academicForm.defaultEndDate, 'Ngày kết thúc năm học mặc định'),
  settingPayload('academic.years', JSON.stringify(academicYears.value), 'Danh sách năm học'),
])

const saveCampaignPolicy = () => saveSettingsBatch('campaignPolicy', [
  settingPayload('campaign.defaultApprovalLevels', campaignPolicyForm.approvalLevels, 'Số cấp duyệt mặc định'),
  settingPayload('campaign.defaultDeadlineDays', campaignPolicyForm.defaultDeadlineDays, 'Thời hạn mặc định theo ngày'),
  settingPayload('campaign.allowEditAfterSubmit', campaignPolicyForm.allowEditAfterSubmit, 'Cho sửa hồ sơ sau khi nộp'),
  settingPayload('campaign.allowResubmitAfterRejected', campaignPolicyForm.allowResubmitAfterRejected, 'Cho nộp lại sau khi từ chối'),
  settingPayload('campaign.autoRejectAfterDeadline', campaignPolicyForm.autoRejectAfterDeadline, 'Tự động từ chối khi quá hạn'),
  settingPayload('campaign.allowLateSubmission', campaignPolicyForm.allowLateSubmission, 'Cho nộp trễ có lý do'),
  settingPayload('campaign.gracePeriodDays', campaignPolicyForm.gracePeriodDays, 'Grace period theo ngày'),
])

const saveEvidencePolicy = () => saveSettingsBatch('evidencePolicy', [
  settingPayload('evidence.maxFileSizeMb', evidencePolicyForm.maxFileSizeMb, 'Dung lượng tối đa mỗi file'),
  settingPayload('evidence.allowedFormats', evidencePolicyForm.allowedFormats, 'Định dạng file cho phép'),
  settingPayload('evidence.maxFilesPerEvidence', evidencePolicyForm.maxFilesPerEvidence, 'Số file tối đa mỗi minh chứng'),
  settingPayload('evidence.maxFilesPerApplication', evidencePolicyForm.maxFilesPerApplication, 'Số file tối đa mỗi hồ sơ'),
  settingPayload('evidence.watermarkEnabled', evidencePolicyForm.watermarkEnabled, 'Bật watermark'),
  settingPayload('evidence.duplicateCheckEnabled', evidencePolicyForm.duplicateCheckEnabled, 'Kiểm tra trùng file'),
])

const saveSecurity = () => saveSettingsBatch('security', [
  settingPayload('security.jwtAccessExpirationMinutes', securityForm.jwtAccessExpirationMinutes, 'Thời gian hết hạn JWT'),
  settingPayload('security.refreshTokenExpirationDays', securityForm.refreshTokenExpirationDays, 'Thời gian hết hạn refresh token'),
  settingPayload('security.maxLoginAttempts', securityForm.maxLoginAttempts, 'Số lần đăng nhập sai tối đa'),
  settingPayload('security.passwordMinLength', securityForm.passwordMinLength, 'Độ dài mật khẩu tối thiểu'),
  settingPayload('security.passwordRequireSpecial', securityForm.passwordRequireSpecial, 'Yêu cầu ký tự đặc biệt'),
  settingPayload('security.uploadRateLimitPerMinute', securityForm.uploadRateLimitPerMinute, 'Giới hạn upload theo phút'),
  settingPayload('security.apiRateLimitPerMinute', securityForm.apiRateLimitPerMinute, 'Rate limit API theo phút'),
  settingPayload('security.forcePasswordChangeDays', securityForm.forcePasswordChangeDays, 'Bắt buộc đổi mật khẩu định kỳ'),
  settingPayload('security.twoFactorEnabled', securityForm.twoFactorEnabled, 'Bật 2FA'),
])

const saveStorage = () => saveSettingsBatch('storage', [
  settingPayload('storage.provider', storageForm.provider, 'Provider lưu file'),
  settingPayload('storage.credentialRef', storageForm.credentialRef, 'Credential ref/secret alias'),
  settingPayload('storage.maxTotalGb', storageForm.maxTotalGb, 'Dung lượng lưu trữ tổng'),
  settingPayload('storage.maxPerUserMb', storageForm.maxPerUserMb, 'Dung lượng mỗi user'),
  settingPayload('integration.ssoEnabled', storageForm.ssoEnabled, 'Bật/tắt SSO'),
  settingPayload('integration.ldapEnabled', storageForm.ldapEnabled, 'Bật/tắt LDAP'),
])

const saveAuditSettings = () => saveSettingsBatch('auditSettings', [
  settingPayload('audit.retentionDays', auditSettingsForm.retentionDays, 'Số ngày lưu audit log'),
  settingPayload('audit.systemErrorAlertEnabled', auditSettingsForm.systemErrorAlertEnabled, 'Gửi cảnh báo lỗi hệ thống'),
])

const addAcademicYear = () => {
  if (!academicYearDraft.label.trim()) {
    toast.add({ title: 'Vui lòng nhập năm học', color: 'error' })
    return
  }
  academicYears.value.push({ ...academicYearDraft, label: academicYearDraft.label.trim() })
  Object.assign(academicYearDraft, { label: '', startDate: '', endDate: '', status: 'OPEN' })
}

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await fetchSettings()
    settings.value = response.data
    hydrateForms()
    loaded.value = true
  }
  catch (cause) {
    error.value = getErrorMessage(cause, 'Không thể tải cấu hình hệ thống.')
  }
  finally {
    loading.value = false
  }
}

const loadAuditLogs = async () => {
  auditLoading.value = true
  try {
    const response = await fetchAuditLogs({
      page: auditUiPage.value - 1,
      size: 10,
      entity: auditFilters.entity.trim() || undefined,
      action: auditFilters.action.trim() || undefined,
      userId: auditFilters.userId,
    })
    auditPageData.value = response.data
  }
  catch (cause) {
    toast.add({
      title: 'Không thể tải audit log',
      description: getErrorMessage(cause, 'Vui lòng thử lại.'),
      color: 'error',
    })
  }
  finally {
    auditLoading.value = false
  }
}

watch(auditUiPage, loadAuditLogs)
watch(activeSection, (section) => {
  if (section === 'audit' && !auditPageData.value) {
    void loadAuditLogs()
  }
})

const exportAuditLogs = () => {
  if (!import.meta.client) return

  const logs = auditPageData.value?.content || []
  const escapeCell = (value: unknown) => `"${String(value ?? '').replace(/"/g, '""')}"`
  const rows = [
    ['id', 'entity', 'entityId', 'action', 'actorId', 'actorEmail', 'oldValue', 'newValue', 'createdAt'],
    ...logs.map(log => [
      log.id,
      log.entity,
      log.entityId,
      log.action,
      log.actorId,
      log.actorEmail,
      log.oldValue,
      log.newValue,
      log.createdAt,
    ]),
  ]
  const csv = rows.map(row => row.map(escapeCell).join(',')).join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `audit-logs-${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(url)
}

const getSettingCategory = (keyName: string) => {
  const [category] = keyName.split('.')
  return category?.trim() || 'general'
}

const formatCategory = (category: string) => {
  const labels: Record<string, string> = {
    academic: 'Năm học',
    app: 'Ứng dụng',
    audit: 'Audit',
    campaign: 'Campaign',
    evidence: 'Minh chứng',
    general: 'Chung',
    integration: 'Tích hợp',
    notification: 'Notification',
    security: 'Bảo mật',
    storage: 'Lưu trữ',
    system: 'Hệ thống',
  }

  return labels[category.toLowerCase()] || category
}

const categoryOptions = computed(() => {
  const categories = Array.from(new Set(visibleSettings.value.map(setting => getSettingCategory(setting.keyName))))
    .sort((left, right) => formatCategory(left).localeCompare(formatCategory(right), 'vi'))

  return [
    { label: 'Tất cả nhóm', value: 'all' },
    ...categories.map(category => ({
      label: formatCategory(category),
      value: category,
    })),
  ]
})

const filteredSettings = computed(() => {
  const keyword = search.value.trim().toLowerCase()

  return visibleSettings.value.filter((setting) => {
    const category = getSettingCategory(setting.keyName)
    const matchesCategory = selectedCategory.value === 'all' || selectedCategory.value === category
    const matchesKeyword = !keyword
      || setting.keyName.toLowerCase().includes(keyword)
      || setting.value.toLowerCase().includes(keyword)
      || (setting.description || '').toLowerCase().includes(keyword)

    return matchesCategory && matchesKeyword
  })
})

const resetFilters = () => {
  search.value = ''
  selectedCategory.value = 'all'
}

const openCreate = () => {
  editing.value = null
  Object.assign(form, { keyName: '', value: '', description: '' })
  formOpen.value = true
}

const openEdit = (setting: SystemSetting) => {
  editing.value = setting
  Object.assign(form, { keyName: setting.keyName, value: setting.value, description: setting.description || '' })
  formOpen.value = true
}

const askDelete = (setting: SystemSetting) => {
  deleting.value = setting
  deleteOpen.value = true
}

const save = async () => {
  Object.assign(formErrors, { keyName: undefined, value: undefined })
  if (!editing.value && !form.keyName.trim()) formErrors.keyName = 'Khóa cấu hình là bắt buộc.'
  if (!editing.value && form.keyName.trim().toLowerCase().startsWith('scoring.')) formErrors.keyName = 'Không tạo key scoring.* trong Settings.'
  if (!form.value.trim()) formErrors.value = 'Giá trị cấu hình là bắt buộc.'
  if (Object.values(formErrors).some(Boolean)) return

  submitting.value = true
  try {
    if (editing.value) {
      await updateSetting(editing.value.keyName, { value: form.value, description: form.description })
      toast.add({ title: 'Đã cập nhật cấu hình', color: 'success' })
    }
    else {
      await createSetting({ keyName: form.keyName.trim(), value: form.value, description: form.description })
      toast.add({ title: 'Đã tạo cấu hình', color: 'success' })
    }
    formOpen.value = false
    await load()
  }
  catch (cause) {
    toast.add({ title: 'Không thể lưu cấu hình', description: getErrorMessage(cause, 'Vui lòng thử lại.'), color: 'error' })
  }
  finally {
    submitting.value = false
  }
}

const confirmDelete = async () => {
  if (!deleting.value) return
  submitting.value = true
  try {
    await deleteSetting(deleting.value.keyName)
    toast.add({ title: 'Đã xóa cấu hình', color: 'success' })
    deleteOpen.value = false
    await load()
  }
  catch (cause) {
    toast.add({ title: 'Không thể xóa cấu hình', description: getErrorMessage(cause, 'Vui lòng thử lại.'), color: 'error' })
  }
  finally {
    submitting.value = false
  }
}

const formatDateTime = (value: string | null) => {
  if (!value) return '-'

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value

  return new Intl.DateTimeFormat('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

onMounted(load)
</script>
