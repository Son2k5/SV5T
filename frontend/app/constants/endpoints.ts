const getBaseUrl = () => {
  const config = useRuntimeConfig()
  const baseUrl = config.public.apiBaseUrl

  return (typeof baseUrl === 'string' && baseUrl.length > 0 ? baseUrl : 'http://localhost:8080').replace(/\/$/, '')
}

const endpoint = (path: string) => `${getBaseUrl()}${path}`

export const refreshAccessTokenEndpoint = () => endpoint('/user/auth/refresh_access_token')

export const LogInEndpoint = () => endpoint('/user/auth/login')
export const LogOutEndpoint = () => endpoint('/user/auth/log_out')
export const RegisterEndpoint = () => endpoint('/user/auth/register')
export const MissingPasswordEndpoint = () => endpoint('/user/auth/missing_password')
export const ResetPasswordEndpoint = () => endpoint('/user/auth/reset_password')
export const VerifyResetEndpoint = () => endpoint('/user/auth/verify_reset_token')

export const GetCriteriaEndpoint = () => endpoint('/user/campaign/get_campaign')

export const CurrentUserEndpoint = () => endpoint('/user/profile')
export const CurrentUserAvatarEndpoint = () => endpoint('/user/profile/avatar')
