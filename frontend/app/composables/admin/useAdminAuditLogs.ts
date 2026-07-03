import type { AdminPage, AuditLog } from '~/types/admin'
import { useAdminClient } from '~/composables/admin/useAdminClient'

export interface AuditLogQuery {
  page?: number
  size?: number
  entity?: string
  action?: string
  userId?: number
}

export const useAdminAuditLogs = () => {
  const { request } = useAdminClient()

  const fetchAuditLogs = (query: AuditLogQuery = {}) =>
    request<AdminPage<AuditLog>>('/admin/audit-logs', {
      query: {
        page: query.page ?? 0,
        size: query.size ?? 10,
        entity: query.entity,
        action: query.action,
        userId: query.userId,
      },
    })

  return { fetchAuditLogs }
}
