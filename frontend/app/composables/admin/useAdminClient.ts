import type { AdminApiResponse } from '~/types/admin'
import { useAuth } from '~/composables/useAuth'

type Query = Record<string, string | number | boolean | null | undefined>

type AdminRequestOptions = {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  body?: unknown
  query?: Query
}

/**
 * Keeps the base URL and authenticated request behaviour in one place.
 * useAuth().authFetch already attaches the Bearer token and refreshes it on 401.
 */
export const useAdminClient = () => {
  const config = useRuntimeConfig()
  const { authFetch } = useAuth()
  const baseUrl = String(config.public.apiBaseUrl || 'http://localhost:8080').replace(/\/$/, '')

  const request = <T>(path: string, options: AdminRequestOptions = {}) =>
    authFetch<AdminApiResponse<T>>(`${baseUrl}/api${path}`, options)

  return { request }
}
