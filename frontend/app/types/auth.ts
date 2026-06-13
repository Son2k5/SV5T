import type { ProfileData } from './profile'

export type ApiResponse<T = unknown> = {
  message: string
  data: T
}

export type ProfileResponse = {
  message: string
  data: ProfileData
}

export type LoginResponse = {
  message: string
  data: {
    accessToken: string
    user: {
      id: string
      email: string
      userName: string
      role: string
    }
  }
}

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
