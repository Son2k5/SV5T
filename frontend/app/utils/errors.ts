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
const NETWORK_ERROR_MESSAGE = 'Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng.'
const SYSTEM_ERROR_MESSAGE = 'Hệ thống gặp sự cố. Vui lòng thử lại sau.'
const NOT_FOUND_MESSAGE = 'Không tìm thấy dữ liệu.'

const normalizeErrorMessage = (message?: string) => {
  if (!message) return null

  const normalizedMessage = message.toLowerCase()

  // 1. Kiểm tra lỗi phiên đăng nhập
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

  // 2. Kiểm tra lỗi kết nối mạng / fetch
  const networkErrors = [
    'fetch failed',
    'failed to fetch',
    'network error',
    'networkerror',
    'load failed',
  ]
  if (networkErrors.some(error => normalizedMessage.includes(error))) {
    return NETWORK_ERROR_MESSAGE
  }

  // 3. Kiểm tra và chặn lộ các thông tin kỹ thuật / exception / SQL
  const technicalKeywords = [
    'exception',
    'sql',
    'hibernate',
    'database',
    'query',
    'nullpointer',
    'mysql',
    'jpa',
    'constraint',
    'truncation',
    'syntax',
    'column',
    'table',
    'entity',
    'h2',
    'driver',
    'jdbc',
  ]
  if (technicalKeywords.some(keyword => normalizedMessage.includes(keyword))) {
    return SYSTEM_ERROR_MESSAGE
  }

  return message
}

export const getErrorMessage = (error: unknown, fallback: string) => {
  if (typeof error !== 'object' || error === null) {
    return fallback
  }

  const apiError = error as ApiErrorLike

  // Lấy status code để xử lý nhanh các mã lỗi HTTP tiêu chuẩn
  const statusCode = apiError.statusCode ?? apiError.status ?? apiError.response?.status ?? apiError.data?.status

  if (statusCode === 401 || statusCode === 403) {
    return SESSION_EXPIRED_MESSAGE
  }
  if (statusCode === 404) {
    return NOT_FOUND_MESSAGE
  }
  if (statusCode && statusCode >= 500 && statusCode <= 599) {
    return SYSTEM_ERROR_MESSAGE
  }

  const rawMessage = apiError.data?.message ?? apiError.message
  return normalizeErrorMessage(rawMessage) ?? fallback
}

export const getErrorStatus = (error: unknown) => {
  if (typeof error !== 'object' || error === null) {
    return undefined
  }

  const apiError = error as ApiErrorLike
  return apiError.statusCode ?? apiError.status ?? apiError.response?.status ?? apiError.data?.status
}
