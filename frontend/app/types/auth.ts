import type { ProfileData } from './profile'

export type ApiResponse<T = unknown> = {
  message: string
  data: T
  success: boolean
}

export type ProfileResponse = ApiResponse<ProfileData>

export type LoginResponse = ApiResponse<{
  accessToken: string
  user: {
    id?: string
    publicId?: string
    email: string
    userName?: string
    role: string
  }
}>

export type LogInPayload = {
  email: string
  userPassword: string
}

export type RegisterPayload = {
  email: string
  userPassword: string
}

export type ResetPasswordPayload = {
  token: string
  newPw: string
}
