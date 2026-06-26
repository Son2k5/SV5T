import type { SystemSetting, SystemSettingForm } from '~/types/admin'
import { useAdminClient } from '~/composables/admin/useAdminClient'

export const useAdminSettings = () => {
  const { request } = useAdminClient()

  const fetchSettings = () => request<SystemSetting[]>('/admin/settings')

  const createSetting = (form: SystemSettingForm) =>
    request<SystemSetting>('/admin/settings', { method: 'POST', body: form })

  const updateSetting = (key: string, form: Pick<SystemSettingForm, 'value' | 'description'>) =>
    request<SystemSetting>(`/admin/settings/${encodeURIComponent(key)}`, {
      method: 'PUT',
      body: form,
    })

  const deleteSetting = (key: string) =>
    request<undefined>(`/admin/settings/${encodeURIComponent(key)}`, { method: 'DELETE' })

  return { fetchSettings, createSetting, updateSetting, deleteSetting }
}
