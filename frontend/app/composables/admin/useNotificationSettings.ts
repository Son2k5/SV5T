import type {
  NotificationSettingDto,
  NotificationSettingForm,
  NotificationTemplateDto,
  NotificationTemplateForm,
  NotificationType,
  SmtpTestForm,
} from '~/types/notification'
import { useAdminClient } from '~/composables/admin/useAdminClient'

export const useNotificationSettings = () => {
  const { request } = useAdminClient()

  const fetchSettings = () =>
    request<NotificationSettingDto>('/admin/notification-settings')

  const updateSettings = (form: NotificationSettingForm) =>
    request<NotificationSettingDto>('/admin/notification-settings', {
      method: 'PUT',
      body: form,
    })

  const testSmtpConnection = (form: SmtpTestForm) =>
    request<undefined>('/admin/notification-settings/test', {
      method: 'POST',
      body: form,
    })

  const fetchTemplates = () =>
    request<NotificationTemplateDto[]>('/admin/notification-templates')

  const updateTemplate = (code: NotificationType, form: NotificationTemplateForm) =>
    request<NotificationTemplateDto>(`/admin/notification-templates/${encodeURIComponent(code)}`, {
      method: 'PUT',
      body: form,
    })

  return {
    fetchSettings,
    fetchTemplates,
    testSmtpConnection,
    updateSettings,
    updateTemplate,
  }
}
