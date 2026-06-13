type ApiErrorLike = {
  data?: {
    message?: string
  }
  message?: string
}

export const getErrorMessage = (error: unknown, fallback: string) => {
  if (typeof error !== 'object' || error === null) {
    return fallback
  }

  const apiError = error as ApiErrorLike
  return apiError.data?.message ?? apiError.message ?? fallback
}
