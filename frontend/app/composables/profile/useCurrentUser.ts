import { CurrentUserAvatarEndpoint, CurrentUserEndpoint } from '~/constants/endpoints'
import type { ApiResponse } from '~/types/auth'
import type { ProfileData, UserProfileUpdateRequest } from '~/types/profile'
import { getErrorMessage } from '~/utils/errors'

export const useCurrentUser = async () => {
  const isLoading = ref(false)
  const avatarLoading = ref(false)
  const toast = useToast()
  const { authFetch } = useAuth()

  const fetchCurrentUser = async () => {
    try {
      const { data } = await authFetch<ApiResponse<ProfileData>>(CurrentUserEndpoint(), {
        method: 'GET',
      })

      return data
    }
    catch (error) {
      toast.add({
        title: 'Thất bại nhận thông tin!',
        description: getErrorMessage(error, 'Vui lòng thử lại sau.'),
        color: 'error',
      })
    }
  }

  const { data: profile } = await useAsyncData('current-user', () => fetchCurrentUser())

  const updateCurrentUser = async (payload: UserProfileUpdateRequest) => {
    isLoading.value = true

    try {
      const { data, message } = await authFetch<ApiResponse<ProfileData>>(CurrentUserEndpoint(), {
        method: 'PUT',
        body: payload,
      })

      toast.add({
        title: 'Cập nhật thông tin thành công!',
        description: message,
        color: 'success',
      })

      await refreshNuxtData('current-user')

      return data
    }
    catch (error) {
      toast.add({
        title: 'Cập nhật thông tin thất bại!',
        description: getErrorMessage(error, 'Vui lòng thử lại sau.'),
        color: 'error',
      })
    }
    finally {
      isLoading.value = false
    }
  }

  const updateCurrentUserAvatar = async (file: File) => {
    avatarLoading.value = true

    try {
      const formData = new FormData()
      formData.append('file', file)

      const { data, message } = await authFetch<ApiResponse<ProfileData>>(CurrentUserAvatarEndpoint(), {
        method: 'PUT',
        body: formData,
      })

      toast.add({
        title: 'Cập nhật ảnh đại diện thành công!',
        description: message,
        color: 'success',
      })

      profile.value = data
      await refreshNuxtData('current-user')

      return data
    }
    catch (error) {
      toast.add({
        title: 'Cập nhật ảnh đại diện thất bại!',
        description: getErrorMessage(error, 'Vui lòng thử lại sau.'),
        color: 'error',
      })
    }
    finally {
      avatarLoading.value = false
    }
  }

  return { profile, updateCurrentUser, updateCurrentUserAvatar, isLoading, avatarLoading }
}
