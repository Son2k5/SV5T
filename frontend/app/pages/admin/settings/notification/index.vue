<template>
  <div class="space-y-6">
    <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
      <div>
        <h1 class="text-2xl font-bold text-[#1E293B]">
          Cau hinh notification
        </h1>
        <p class="mt-1 text-sm text-[#64748B]">
          Quan ly SMTP, realtime, lich nhac han va noi dung template thong bao.
        </p>
      </div>
      <UButton
        color="neutral"
        variant="outline"
        icon="i-lucide-arrow-left"
        label="Quay lai cau hinh"
        to="/admin/settings"
      />
    </div>

    <div class="inline-flex rounded-2xl border border-[#E5E7EB] bg-white p-1 shadow-sm">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        type="button"
        class="h-10 rounded-xl px-4 text-sm font-semibold transition"
        :class="activeTab === tab.value ? 'bg-[#2563EB] text-white shadow-sm' : 'text-[#64748B] hover:bg-blue-50 hover:text-[#2563EB]'"
        @click="activeTab = tab.value"
      >
        {{ tab.label }}
      </button>
    </div>

    <CommonPageLoading v-if="loading && !loaded" />

    <div
      v-else-if="error"
      class="rounded-2xl bg-red-50 p-4 text-sm text-red-700"
    >
      <div class="flex flex-wrap items-center justify-between gap-3">
        <span>{{ error }}</span>
        <UButton
          color="error"
          variant="soft"
          label="Thu lai"
          @click="load"
        />
      </div>
    </div>

    <template v-else>
      <CommonPageSection
        v-if="activeTab === 'email'"
        title="Email configuration"
        title-icon="i-lucide-mail"
        inner-class="!block"
      >
        <div class="grid gap-4 lg:grid-cols-2">
          <UFormField label="SMTP host">
            <UInput
              v-model="form.smtpHost"
              placeholder="smtp.gmail.com"
            />
          </UFormField>
          <UFormField label="SMTP port">
            <UInput
              v-model.number="form.smtpPort"
              type="number"
              min="1"
              max="65535"
            />
          </UFormField>
          <UFormField label="Username">
            <UInput
              v-model="form.smtpUsername"
              placeholder="notification@example.com"
            />
          </UFormField>
          <UFormField label="Password">
            <UInput
              v-model="form.smtpPassword"
              type="password"
              autocomplete="new-password"
              placeholder="SMTP app password"
            />
          </UFormField>
          <UFormField label="Test email">
            <UInput
              v-model="testEmail"
              type="email"
              placeholder="admin@example.com"
            />
          </UFormField>
          <div class="flex items-end gap-3">
            <UButton
              color="neutral"
              variant="outline"
              icon="i-lucide-send"
              label="Test connection"
              :loading="testing"
              :disabled="saving"
              @click="testConnection"
            />
            <UButton
              color="info"
              icon="i-lucide-save"
              label="Save settings"
              :loading="saving"
              :disabled="testing"
              @click="saveSettings"
            />
          </div>
        </div>
      </CommonPageSection>

      <div
        v-else
        class="space-y-6"
      >
        <CommonPageSection
          title="Notification rules"
          title-icon="i-lucide-sliders-horizontal"
          inner-class="!block"
        >
          <div class="grid gap-4 lg:grid-cols-3">
            <div class="rounded-2xl border border-[#E5E7EB] bg-white p-4">
              <UCheckbox
                v-model="form.emailEnabled"
                label="Email notification"
              />
              <p class="mt-2 text-sm text-[#64748B]">
                Gui email theo template neu SMTP hop le.
              </p>
            </div>
            <div class="rounded-2xl border border-[#E5E7EB] bg-white p-4">
              <UCheckbox
                v-model="form.realtimeEnabled"
                label="Realtime notification"
              />
              <p class="mt-2 text-sm text-[#64748B]">
                Day thong bao den chuong tren giao dien.
              </p>
            </div>
            <UFormField label="So ngay nhac han truoc deadline">
              <UInput
                v-model.number="form.reminderDaysBeforeDeadline"
                type="number"
                min="0"
                max="30"
              />
            </UFormField>
          </div>
          <div class="mt-5">
            <UButton
              color="info"
              icon="i-lucide-save"
              label="Save rules"
              :loading="saving"
              @click="saveSettings"
            />
          </div>
        </CommonPageSection>

        <CommonPageSection
          title="Notification templates"
          title-icon="i-lucide-file-text"
          inner-class="!block p-0"
        >
          <UTable
            :data="templates"
            :columns="columns"
            :loading="loading"
            class="w-full"
          >
            <template #code-cell="{ row }">
              <code class="rounded-lg bg-blue-50 px-2 py-1 text-xs font-semibold text-[#2563EB]">{{ row.original.code }}</code>
            </template>
            <template #subject-cell="{ row }">
              <p class="max-w-xs truncate text-sm font-semibold text-[#1E293B]">
                {{ row.original.subject }}
              </p>
            </template>
            <template #preview-cell="{ row }">
              <p class="max-w-md text-sm text-[#64748B]">
                {{ preview(row.original.bodyTemplate) }}
              </p>
            </template>
            <template #channel-cell="{ row }">
              <UBadge
                color="info"
                variant="soft"
                :label="row.original.channel"
              />
            </template>
            <template #active-cell="{ row }">
              <UBadge
                :color="row.original.active ? 'success' : 'neutral'"
                variant="soft"
                :label="row.original.active ? 'Active' : 'Inactive'"
              />
            </template>
            <template #actions-cell="{ row }">
              <div class="flex justify-end">
                <UButton
                  color="info"
                  variant="ghost"
                  icon="i-lucide-pen-line"
                  aria-label="Sua template"
                  @click="openTemplate(row.original)"
                />
              </div>
            </template>
          </UTable>
        </CommonPageSection>
      </div>
    </template>

    <UModal
      :open="templateModalOpen"
      title="Cap nhat template notification"
      @update:open="templateModalOpen = $event"
    >
      <template #body>
        <form
          class="space-y-4"
          @submit.prevent="saveTemplate"
        >
          <UFormField label="Code">
            <UInput
              :model-value="editingTemplate?.code || ''"
              disabled
            />
          </UFormField>
          <UFormField label="Subject">
            <UInput v-model="templateForm.subject" />
          </UFormField>
          <UFormField label="Body template">
            <UTextarea
              v-model="templateForm.bodyTemplate"
              :rows="6"
            />
          </UFormField>
          <div class="grid gap-4 sm:grid-cols-2">
            <UFormField label="Channel">
              <USelect
                v-model="templateForm.channel"
                :items="channelOptions"
              />
            </UFormField>
            <div class="rounded-2xl border border-[#E5E7EB] p-4">
              <UCheckbox
                v-model="templateForm.active"
                label="Active"
              />
            </div>
          </div>
          <div class="rounded-2xl bg-slate-50 p-4">
            <p class="text-xs font-bold uppercase text-[#64748B]">
              Preview
            </p>
            <p class="mt-2 text-sm leading-6 text-[#334155]">
              {{ preview(templateForm.bodyTemplate) }}
            </p>
          </div>
        </form>
      </template>
      <template #footer>
        <div class="flex w-full justify-end gap-3">
          <UButton
            color="neutral"
            variant="outline"
            label="Huy"
            :disabled="savingTemplate"
            @click="templateModalOpen = false"
          />
          <UButton
            color="info"
            icon="i-lucide-save"
            label="Luu template"
            :loading="savingTemplate"
            @click="saveTemplate"
          />
        </div>
      </template>
    </UModal>
  </div>
</template>

<script setup lang="ts">
import type { TableColumn } from '@nuxt/ui'
import type {
  NotificationChannel,
  NotificationSettingForm,
  NotificationTemplateDto,
  NotificationTemplateForm,
} from '~/types/notification'
import { useNotificationSettings } from '~/composables/admin/useNotificationSettings'
import { getErrorMessage } from '~/utils/errors'

definePageMeta({ layout: 'admin', middleware: 'require-admin' })

type TabValue = 'email' | 'rules'

const toast = useToast()
const {
  fetchSettings,
  fetchTemplates,
  testSmtpConnection,
  updateSettings,
  updateTemplate,
} = useNotificationSettings()

const tabs: Array<{ label: string, value: TabValue }> = [
  { label: 'Email Configuration', value: 'email' },
  { label: 'Notification Rules', value: 'rules' },
]
const channelOptions: Array<{ label: string, value: NotificationChannel }> = [
  { label: 'Email', value: 'EMAIL' },
  { label: 'Realtime', value: 'REALTIME' },
  { label: 'Both', value: 'BOTH' },
]
const columns: TableColumn<NotificationTemplateDto>[] = [
  { accessorKey: 'code', header: 'Code' },
  { accessorKey: 'subject', header: 'Subject' },
  { id: 'preview', header: 'Preview' },
  { accessorKey: 'channel', header: 'Channel' },
  { accessorKey: 'active', header: 'Status' },
  { id: 'actions', header: '' },
]

const activeTab = ref<TabValue>('email')
const loaded = ref(false)
const loading = ref(false)
const saving = ref(false)
const testing = ref(false)
const savingTemplate = ref(false)
const error = ref('')
const templates = ref<NotificationTemplateDto[]>([])
const testEmail = ref('')
const templateModalOpen = ref(false)
const editingTemplate = ref<NotificationTemplateDto | null>(null)
const form = reactive<NotificationSettingForm>({
  smtpHost: '',
  smtpPort: 587,
  smtpUsername: '',
  smtpPassword: '',
  emailEnabled: false,
  realtimeEnabled: true,
  reminderDaysBeforeDeadline: 3,
})
const templateForm = reactive<NotificationTemplateForm>({
  subject: '',
  bodyTemplate: '',
  channel: 'BOTH',
  active: true,
})
const samplePayload: Record<string, string> = {
  studentName: 'Nguyen Van A',
  campaignName: 'Sinh vien 5 tot 2025',
  deadline: '30/09/2026',
  reason: 'Minh chung chua hop le',
  status: 'APPROVED',
  evidenceName: 'Bang diem',
  systemMessage: 'He thong se bao tri luc 22:00.',
}

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const [settingsResponse, templatesResponse] = await Promise.all([
      fetchSettings(),
      fetchTemplates(),
    ])
    const settings = settingsResponse.data
    Object.assign(form, {
      smtpHost: settings.smtpHost || '',
      smtpPort: settings.smtpPort || 587,
      smtpUsername: settings.smtpUsername || '',
      smtpPassword: settings.smtpPassword || '',
      emailEnabled: settings.emailEnabled,
      realtimeEnabled: settings.realtimeEnabled,
      reminderDaysBeforeDeadline: settings.reminderDaysBeforeDeadline || 3,
    })
    templates.value = templatesResponse.data
    loaded.value = true
  }
  catch (cause) {
    error.value = getErrorMessage(cause, 'Khong the tai cau hinh notification.')
  }
  finally {
    loading.value = false
  }
}

const saveSettings = async () => {
  saving.value = true
  try {
    const response = await updateSettings({ ...form })
    const settings = response.data
    Object.assign(form, {
      smtpHost: settings.smtpHost || '',
      smtpPort: settings.smtpPort || 587,
      smtpUsername: settings.smtpUsername || '',
      smtpPassword: settings.smtpPassword || '',
      emailEnabled: settings.emailEnabled,
      realtimeEnabled: settings.realtimeEnabled,
      reminderDaysBeforeDeadline: settings.reminderDaysBeforeDeadline || 3,
    })
    toast.add({ title: 'Da luu cau hinh notification', color: 'success' })
  }
  catch (cause) {
    toast.add({
      title: 'Khong the luu cau hinh',
      description: getErrorMessage(cause, 'Vui long kiem tra lai du lieu.'),
      color: 'error',
    })
  }
  finally {
    saving.value = false
  }
}

const testConnection = async () => {
  if (!testEmail.value.trim()) {
    toast.add({ title: 'Vui long nhap email test', color: 'error' })
    return
  }

  testing.value = true
  try {
    await testSmtpConnection({
      smtpHost: form.smtpHost,
      smtpPort: form.smtpPort,
      smtpUsername: form.smtpUsername,
      smtpPassword: form.smtpPassword,
      testEmail: testEmail.value.trim(),
    })
    toast.add({ title: 'Gui email test thanh cong', color: 'success' })
  }
  catch (cause) {
    toast.add({
      title: 'Test SMTP that bai',
      description: getErrorMessage(cause, 'Vui long kiem tra SMTP host, port, username va password.'),
      color: 'error',
    })
  }
  finally {
    testing.value = false
  }
}

const openTemplate = (template: NotificationTemplateDto) => {
  editingTemplate.value = template
  Object.assign(templateForm, {
    subject: template.subject,
    bodyTemplate: template.bodyTemplate,
    channel: template.channel,
    active: template.active,
  })
  templateModalOpen.value = true
}

const saveTemplate = async () => {
  if (!editingTemplate.value) return

  savingTemplate.value = true
  try {
    const response = await updateTemplate(editingTemplate.value.code, { ...templateForm })
    templates.value = templates.value.map(template =>
      template.code === response.data.code ? response.data : template,
    )
    templateModalOpen.value = false
    toast.add({ title: 'Da cap nhat template', color: 'success' })
  }
  catch (cause) {
    toast.add({
      title: 'Khong the cap nhat template',
      description: getErrorMessage(cause, 'Vui long kiem tra placeholder trong template.'),
      color: 'error',
    })
  }
  finally {
    savingTemplate.value = false
  }
}

const preview = (template: string) =>
  template.replace(/\{\{\s*([a-zA-Z][a-zA-Z0-9_]*)\s*}}/g, (_, key: string) => samplePayload[key] || '')

onMounted(load)
</script>
