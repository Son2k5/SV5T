import type { ApiResponse, LogInPayload, LoginResponse, RegisterPayload, ResetPasswordPayload } from '~/types/auth'
import { LogInEndpoint, LogOutEndpoint, MissingPasswordEndpoint, refreshAccessTokenEndpoint, RegisterEndpoint, ResetPasswordEndpoint, VerifyResetEndpoint } from '~/constants/endpoints'
import { getErrorMessage } from '~/utils/errors'

export const useAuth = () => {
  const toast = useToast()

  const accessToken = useState<string | null>('accessToken', () => null)

  if (import.meta.client && !accessToken.value) {
    accessToken.value = sessionStorage.getItem('accessToken')
  }

  const setAccessToken = (token: string | null) => {
    accessToken.value = token

    if (import.meta.client) {
      if (token) {
        sessionStorage.setItem('accessToken', token)
      }
      else {
        sessionStorage.removeItem('accessToken')
      }
    }
  }

  const refreshAccessToken = async () => {
    try {
      const headers = import.meta.server ? useRequestHeaders(['cookie']) : undefined
      const { data } = await $fetch<{ data: { accessToken: string }, message: string }>(refreshAccessTokenEndpoint(), {
        method: 'POST',
        credentials: 'include',
        ...(headers ? { headers } : {}),
      })
      if (data?.accessToken) {
        setAccessToken(data.accessToken)
        return true
      }
      return false
    }
    catch {
      setAccessToken(null)
      return false
    }
  }

  const logIn = async (payload: LogInPayload) => {
    try {
      const { message, data } = await $fetch<LoginResponse>(LogInEndpoint(), {
        method: 'POST',
        body: payload,
        credentials: 'include',
      })

      if (!data?.accessToken) {
        throw new Error('Missing access token')
      }

      toast.add({
        title: 'Đăng nhập thành công!',
        description: message,
      })
      setAccessToken(data.accessToken)
      return data
    }
    catch (error) {
      toast.add({
        title: 'Đăng nhập thất bại!',
        description: getErrorMessage(error, 'Vui lòng thử lại sau.'),
        color: 'error',
      })
      return false
    }
  }

  const logOut = async () => {
    try {
      const token = accessToken.value
      await $fetch(LogOutEndpoint(), {
        method: 'POST',
        credentials: 'include',
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })
    }
    finally {
      setAccessToken(null)
      await navigateTo('/login')
    }
  }

  const register = async (payload: RegisterPayload) => {
    try {
      const { message, data } = await $fetch<ApiResponse>(RegisterEndpoint(), {
        method: 'POST',
        body: payload,
      })

      toast.add({
        title: 'Đăng ký thành công!',
        description: message,
      })
      return data
    }
    catch (error) {
      toast.add({
        title: 'Đăng ký thất bại!',
        color: 'error',
        description: getErrorMessage(error, 'Vui lòng thử lại sau.'),
      })
      return false
    }
  }

  const missingPassword = async (email: string) => {
    try {
      const { data } = await $fetch<ApiResponse>(MissingPasswordEndpoint(), {
        method: 'POST',
        query: { email },
      })

      toast.add({
        title: 'Đã gửi mail!',
        description: `Hướng dẫn phục hồi tài khoản đã được gửi về ${email}`,
      })
      return data
    }
    catch {
      toast.add({
        title: 'Gửi mail thất bại!',
        description: 'Vui lòng thử lại sau.',
        color: 'error',
      })
      return false
    }
  }

  const resetPassword = async (payload: ResetPasswordPayload) => {
    try {
      const { message, data } = await $fetch<ApiResponse>(ResetPasswordEndpoint(), {
        method: 'POST',
        body: payload,
      })

      toast.add({
        title: 'Thay đổi mật khẩu thành công!',
        description: message,
      })
      return data
    }
    catch {
      toast.add({
        title: 'Thay đổi mật khẩu thất bại!',
        description: 'Vui lòng thử lại sau.',
        color: 'error',
      })
      return false
    }
  }

  const verifyResetToken = async (token: string) => {
    try {
      await $fetch<ApiResponse>(VerifyResetEndpoint(), {
        method: 'GET',
        query: { token: token },
      })
      return true
    }
    catch {
      return false
    }
  }

  return { accessToken, refreshAccessToken, logIn, logOut, register, missingPassword, resetPassword, verifyResetToken }
}
