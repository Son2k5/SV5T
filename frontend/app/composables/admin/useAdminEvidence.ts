import type { AdminPage, Evidence, EvidenceStatus, StudentApplication, StandardResultDTO } from '~/types/admin'
import { useAdminClient } from '~/composables/admin/useAdminClient'

export type EvidenceFilters = {
  page?: number
  size?: number
  status?: EvidenceStatus
  campaignPublicId?: string
  keyword?: string
  isGroup?: boolean
}

export const useAdminEvidence = () => {
  const { request } = useAdminClient()

  const fetchResults = (
    campaignPublicId?: string,
    filters: { page?: number, size?: number, status?: string, keyword?: string, isGroup?: boolean, level?: string } = {},
  ) => {
    const path = campaignPublicId ? `/admin/campaigns/${campaignPublicId}/results` : '/admin/campaigns/results'
    return request<AdminPage<StudentApplication>>(path, {
      method: 'GET',
      query: {
        page: filters.page ?? 0,
        size: filters.size ?? 10,
        status: filters.status || undefined,
        keyword: filters.keyword || undefined,
        isGroup: filters.isGroup !== undefined ? filters.isGroup : undefined,
        level: filters.level || undefined,
      },
    })
  }

  const fetchUserCriteriaTree = (userPublicId: string, campaignPublicId: string, isGroup?: boolean, level?: string) =>
    request<StandardResultDTO[]>(`/admin/users/${userPublicId}/campaigns/${campaignPublicId}/criteria-tree`, {
      query: {
        ...(isGroup !== undefined ? { isGroup } : {}),
        ...(level ? { level } : {}),
      },
    })

  const fetchEvidences = (filters: EvidenceFilters = {}) =>
    request<AdminPage<Evidence>>('/admin/evidences', {
      query: {
        page: filters.page ?? 0,
        size: filters.size ?? 10,
        status: filters.status,
        campaignPublicId: filters.campaignPublicId,
        keyword: filters.keyword || undefined,
      },
    })

  const getEvidence = (evidencePublicId: string) =>
    request<Evidence>(`/admin/evidences/${evidencePublicId}`)

  const approveEvidence = (evidencePublicId: string, comment?: string) =>
    request<Evidence>(`/admin/evidences/${evidencePublicId}/approve`, {
      method: 'PATCH',
      query: { comment: comment || undefined },
    })

  const rejectEvidence = (evidencePublicId: string, reason: string) =>
    request<Evidence>(`/admin/evidences/${evidencePublicId}/reject`, {
      method: 'PATCH',
      body: { reason },
    })

  const updateEvidenceComment = (evidencePublicId: string, comment: string) =>
    request<Evidence>(`/admin/evidences/${evidencePublicId}/comment`, {
      method: 'PATCH',
      query: { comment },
    })

  const bulkApprove = (publicIds: string[]) =>
    request<Evidence[]>('/admin/evidences/bulk-approve', {
      method: 'PATCH',
      body: { publicIds },
    })

  const bulkReject = (publicIds: string[], reason: string) =>
    request<Evidence[]>('/admin/evidences/bulk-reject', {
      method: 'PATCH',
      query: { reason },
      body: { publicIds },
    })

  return {
    fetchResults,
    fetchUserCriteriaTree,
    fetchEvidences,
    getEvidence,
    approveEvidence,
    rejectEvidence,
    updateEvidenceComment,
    bulkApprove,
    bulkReject,
  }
}
