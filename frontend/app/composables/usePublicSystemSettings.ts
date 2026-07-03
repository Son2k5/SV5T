import type { ApiResponse } from '~/types/auth'
import type { PublicSystemSettings } from '~/types/publicSettings'
import { PublicSystemSettingsEndpoint } from '~/constants/endpoints'

export const usePublicSystemSettings = async () => {
  const { data, refresh } = await useAsyncData(
    'public-system-settings',
    async () => {
      const response = await $fetch<ApiResponse<PublicSystemSettings>>(PublicSystemSettingsEndpoint())
      return response.data || {}
    },
    {
      default: () => ({}),
      server: false,
    },
  )

  const getSetting = (key: string, fallback = '') =>
    data.value[key] ?? fallback

  const maintenanceEnabled = computed(() => getSetting('system.maintenance.enabled') === 'true')
  const maintenanceMessage = computed(() =>
    getSetting('system.maintenance.message', 'Hệ thống đang bảo trì, một số chức năng có thể bị gián đoạn.'),
  )

  return {
    publicSettings: data,
    maintenanceEnabled,
    maintenanceMessage,
    refresh,
  }
}
