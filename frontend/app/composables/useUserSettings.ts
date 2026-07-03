export interface UserInterfaceSettings {
  showNotificationBell: boolean
  showChatWidget: boolean
  sidebarExpanded: boolean
}

const USER_SETTINGS_STORAGE_KEY = 'sv5t:user-interface-settings'

const DEFAULT_USER_SETTINGS: UserInterfaceSettings = {
  showNotificationBell: true,
  showChatWidget: true,
  sidebarExpanded: true,
}

const normalizeSettings = (value: Partial<UserInterfaceSettings> | null | undefined): UserInterfaceSettings => ({
  showNotificationBell: value?.showNotificationBell ?? DEFAULT_USER_SETTINGS.showNotificationBell,
  showChatWidget: value?.showChatWidget ?? DEFAULT_USER_SETTINGS.showChatWidget,
  sidebarExpanded: value?.sidebarExpanded ?? DEFAULT_USER_SETTINGS.sidebarExpanded,
})

export const useUserSettings = () => {
  const settings = useState<UserInterfaceSettings>('user-interface-settings', () => ({ ...DEFAULT_USER_SETTINGS }))
  const hydrated = useState<boolean>('user-interface-settings-hydrated', () => false)

  const loadSettings = () => {
    if (!import.meta.client || hydrated.value) return

    try {
      const rawSettings = localStorage.getItem(USER_SETTINGS_STORAGE_KEY)
      if (rawSettings) {
        settings.value = normalizeSettings(JSON.parse(rawSettings) as Partial<UserInterfaceSettings>)
      }
    }
    catch {
      settings.value = { ...DEFAULT_USER_SETTINGS }
    }
    finally {
      hydrated.value = true
    }
  }

  const saveSettings = () => {
    if (!import.meta.client || !hydrated.value) return

    localStorage.setItem(USER_SETTINGS_STORAGE_KEY, JSON.stringify(settings.value))
  }

  const resetSettings = () => {
    settings.value = { ...DEFAULT_USER_SETTINGS }
    saveSettings()
  }

  if (import.meta.client) {
    loadSettings()
    watch(settings, saveSettings, { deep: true })
  }

  return {
    settings,
    resetSettings,
  }
}
