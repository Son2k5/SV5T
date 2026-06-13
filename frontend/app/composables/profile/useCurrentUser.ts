import { CurrentUserEndpoint } from '~/constants/endpoints'
import type { ApiResponse, ProfileResponse } from '~/types/auth'
import { getErrorMessage } from '~/utils/errors'

export const useCurrentUser = async () => {
  const isLoading = ref<boolean>(false)
  const toast = useToast()
  const { accessToken } = useAuth()

  const fetchCurrentUser = async () => {
    try {
      const { data } = await $fetch<ProfileResponse>(CurrentUserEndpoint(), {
        method: 'GET',
        headers: {
          Authorization: `Bearer ${accessToken.value}`,
        },
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
  const { data: profile } = await useAsyncData(`current-user`, () => fetchCurrentUser())

  const updateCurrentUser = async (user: Record<string, unknown>) => {
    isLoading.value = true
    try {
      const { message } = await $fetch<ApiResponse>(CurrentUserEndpoint(), {
        method: 'PUT',
        headers: {
          Authorization: `Bearer ${accessToken.value}`,
        },
        body: {
          ...user,
        },
      })
      toast.add({
        title: 'Cập nhật Thông tin Thành công!!',
        description: message,
        color: 'success',
      })
      await refreshNuxtData('current-user')
      return navigateTo(`/dashboard/${profile.value?.id}`)
    }
    catch (error) {
      toast.add({
        title: 'Đăng nhập thất bại!',
        description: getErrorMessage(error, 'Vui lòng thử lại sau.'),
        color: 'error',
      })
    }
    finally {
      isLoading.value = false
    }
  }

  return { profile, updateCurrentUser, isLoading }
}
