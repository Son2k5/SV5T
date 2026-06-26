import type {
  AdminPage,
  Campaign,
  CampaignStatus,
  Criteria,
  CriteriaForm,
  EvidenceType,
  Standard,
} from '~/types/admin'
import { useAdminClient } from '~/composables/admin/useAdminClient'

const assertPublicId = (value: string | null | undefined, name: string) => {
  if (!value) {
    throw new Error(`Missing ${name}`)
  }
}

export const useAdminCriteria = () => {
  const { request } = useAdminClient()

  const fetchCampaigns = (page = 0, status?: CampaignStatus) =>
    request<AdminPage<Campaign>>('/admin/campaigns', {
      query: { page, size: 100, status },
    })

  const fetchStandards = async (campaignPublicId: string, isGroup?: boolean) => {
    assertPublicId(campaignPublicId, 'campaignPublicId')

    const response = await request<Standard[]>(
      `/admin/campaigns/${campaignPublicId}/standards`,
      {
        query: isGroup !== undefined ? { isGroup } : undefined,
      },
    )

    return response.data
  }

  const createStandard = (
    campaignPublicId: string,
    form: Pick<Standard, 'name' | 'description' | 'level'> & { isGroup?: boolean },
  ) => {
    assertPublicId(campaignPublicId, 'campaignPublicId')

    return request<Standard>(`/admin/campaigns/${campaignPublicId}/standards`, {
      method: 'POST',
      body: form,
    })
  }

  const updateStandard = (
    standardPublicId: string,
    form: Pick<Standard, 'name' | 'description' | 'level'> & { isGroup?: boolean },
  ) => {
    assertPublicId(standardPublicId, 'standardPublicId')

    return request<Standard>(`/admin/standards/${standardPublicId}`, {
      method: 'PUT',
      body: form,
    })
  }

  const deleteStandard = (standardPublicId: string) => {
    assertPublicId(standardPublicId, 'standardPublicId')

    return request<undefined>(`/admin/standards/${standardPublicId}`, {
      method: 'DELETE',
    })
  }

  const reorderStandard = (standardPublicId: string, orderIndex: number) => {
    assertPublicId(standardPublicId, 'standardPublicId')

    return request<undefined>(`/admin/standards/${standardPublicId}/reorder`, {
      method: 'PATCH',
      query: { orderIndex },
    })
  }

  const fetchCriteria = async (standardPublicId: string) => {
    assertPublicId(standardPublicId, 'standardPublicId')

    const response = await request<Criteria[]>(
      `/admin/standards/${standardPublicId}/criteria`,
    )

    return response.data
  }

  const createCriteria = (standardPublicId: string, form: CriteriaForm) => {
    assertPublicId(standardPublicId, 'standardPublicId')

    return request<Criteria>(`/admin/standards/${standardPublicId}/criteria`, {
      method: 'POST',
      body: form,
    })
  }

  const updateCriteria = (criteriaPublicId: string, form: CriteriaForm) => {
    assertPublicId(criteriaPublicId, 'criteriaPublicId')

    return request<Criteria>(`/admin/criteria/${criteriaPublicId}`, {
      method: 'PUT',
      body: form,
    })
  }

  const deleteCriteria = (criteriaPublicId: string) => {
    assertPublicId(criteriaPublicId, 'criteriaPublicId')

    return request<undefined>(`/admin/criteria/${criteriaPublicId}`, {
      method: 'DELETE',
    })
  }

  const reorderCriteria = (criteriaPublicId: string, orderIndex: number) => {
    assertPublicId(criteriaPublicId, 'criteriaPublicId')

    return request<undefined>(`/admin/criteria/${criteriaPublicId}/reorder`, {
      method: 'PATCH',
      query: { orderIndex },
    })
  }
  const updateCriteriaRequirement = (
    criteriaPublicId: string,
    form: {
      mandatory: boolean
      requiredChildrenCount: number
      evidenceType: EvidenceType | null
    },
  ) => {
    assertPublicId(criteriaPublicId, 'criteriaPublicId')
    return request<Criteria>(`/admin/criteria/${criteriaPublicId}/requirement`, {
      method: 'PATCH',
      body: form,
    })
  }

  return {
    fetchCampaigns,
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
