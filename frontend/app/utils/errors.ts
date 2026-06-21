type ApiErrorLike = {
  data?: {
    message?: string
    status?: number
  }
  status?: number
  statusCode?: number
  response?: {
    status?: number
  }
  message?: string
}

const SESSION_EXPIRED_MESSAGE = 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.'

const normalizeErrorMessage = (message?: string) => {
  if (!message) return null

  const normalizedMessage = message.toLowerCase()
  const tokenErrors = [
    'invalid token',
    'token expired',
    'token khong hop le',
    'token không hợp lệ',
    'phiên đăng nhập không hợp lệ',
  ]

  if (tokenErrors.some(error => normalizedMessage.includes(error))) {
    return SESSION_EXPIRED_MESSAGE
  }

  return message
}

export const getErrorMessage = (error: unknown, fallback: string) => {
  if (typeof error !== 'object' || error === null) {
    return fallback
  }

  const apiError = error as ApiErrorLike
  return normalizeErrorMessage(apiError.data?.message ?? apiError.message) ?? fallback
}

export const getErrorStatus = (error: unknown) => {
  if (typeof error !== 'object' || error === null) {
    return undefined
  }

  const apiError = error as ApiErrorLike
  return apiError.statusCode ?? apiError.status ?? apiError.response?.status ?? apiError.data?.status
}
