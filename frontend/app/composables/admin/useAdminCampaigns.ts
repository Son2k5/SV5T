import type {
  AdminPage,
  Campaign,
  CampaignForm,
  CampaignStatus,
  Criteria,
  CriteriaForm,
  EvidenceType,
  Standard,
} from '~/types/admin'
import { useAdminClient } from '~/composables/admin/useAdminClient'

export const useAdminCampaigns = () => {
  const { request } = useAdminClient()

  const fetchCampaigns = (page = 0, status?: CampaignStatus, level?: string, isGroup?: boolean) =>
    request<AdminPage<Campaign>>('/admin/campaigns', {
      query: { page, size: 10, status, level, isGroup },
    })

  const fetchCampaign = async (campaignPublicId: string) => {
    const response = await request<Campaign>(`/admin/campaigns/${campaignPublicId}`)
    return response.data
  }

  const createCampaign = (form: CampaignForm) =>
    request<Campaign>('/admin/campaigns', {
      method: 'POST',
      body: form,
    })

  const updateCampaign = (campaignPublicId: string, form: CampaignForm) =>
    request<Campaign>(`/admin/campaigns/${campaignPublicId}`, {
      method: 'PUT',
      body: form,
    })

  const deleteCampaign = (campaignPublicId: string) =>
    request<undefined>(`/admin/campaigns/${campaignPublicId}`, {
      method: 'DELETE',
    })

  const fetchStandards = async (campaignPublicId: string) => {
    const response = await request<Standard[]>(
      `/admin/campaigns/${campaignPublicId}/standards`,
    )
    return response.data
  }

  const createStandard = (
    campaignPublicId: string,
    form: Pick<Standard, 'name' | 'description' | 'level'>,
  ) =>
    request<Standard>(`/admin/campaigns/${campaignPublicId}/standards`, {
      method: 'POST',
      body: form,
    })

  const updateStandard = (
    standardPublicId: string,
    form: Pick<Standard, 'name' | 'description' | 'level'>,
  ) =>
    request<Standard>(`/admin/standards/${standardPublicId}`, {
      method: 'PUT',
      body: form,
    })

  const deleteStandard = (standardPublicId: string) =>
    request<undefined>(`/admin/standards/${standardPublicId}`, {
      method: 'DELETE',
    })

  const reorderStandard = (standardPublicId: string, orderIndex: number) =>
    request<undefined>(`/admin/standards/${standardPublicId}/reorder`, {
      method: 'PATCH',
      query: { orderIndex },
    })

  const fetchCriteria = async (standardPublicId: string) => {
    const response = await request<Criteria[]>(
      `/admin/standards/${standardPublicId}/criteria`,
    )
    return response.data
  }

  const createCriteria = (standardPublicId: string, form: CriteriaForm) =>
    request<Criteria>(`/admin/standards/${standardPublicId}/criteria`, {
      method: 'POST',
      body: form,
    })

  const updateCriteria = (criteriaPublicId: string, form: CriteriaForm) =>
    request<Criteria>(`/admin/criteria/${criteriaPublicId}`, {
      method: 'PUT',
      body: form,
    })

  const deleteCriteria = (criteriaPublicId: string) =>
    request<undefined>(`/admin/criteria/${criteriaPublicId}`, {
      method: 'DELETE',
    })

  const reorderCriteria = (criteriaPublicId: string, orderIndex: number) =>
    request<undefined>(`/admin/criteria/${criteriaPublicId}/reorder`, {
      method: 'PATCH',
      query: { orderIndex },
    })
  const updateCriteriaRequirement = (
    criteriaPublicId: string,
    form: {
      mandatory: boolean
      requiredChildrenCount: number
      evidenceType: EvidenceType | null
    },
  ) =>
    request<Criteria>(`/admin/criteria/${criteriaPublicId}/requirement`, {
      method: 'PATCH',
      body: form,
    })

  return {
    fetchCampaigns,
    fetchCampaign,
    createCampaign,
    updateCampaign,
    deleteCampaign,

    fetchStandards,
    createStandard,
    updateStandard,
    deleteStandard,
    reorderStandard,

    fetchCriteria,
    createCriteria,
    updateCriteria,
    deleteCriteria,
    reorderCriteria,
    updateCriteriaRequirement,
  }
}
