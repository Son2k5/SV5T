import type {
  ApiResponse,
  ApplicationRecordResponse,
  CampaignLevel,
  CampaignSummaryResponse,
  CreateApplicationRecordRequest,
  SaveEvidenceRequest,
  StandardDTO,
} from '~/types/applicationRecord'

export const useApplicationRecordApi = () => {
  const config = useRuntimeConfig()
  const configuredBaseUrl = config.public.apiBaseUrl
  const baseUrl = (typeof configuredBaseUrl === 'string' && configuredBaseUrl.length > 0
    ? configuredBaseUrl
    : 'http://localhost:8080').replace(/\/$/, '')
  const { accessToken } = useAuth()

  const getAuthHeaders = () =>
    accessToken.value
      ? { Authorization: `Bearer ${accessToken.value}` }
      : undefined

  const createApplicationRecord = (body: CreateApplicationRecordRequest) => {
    return $fetch<ApiResponse<ApplicationRecordResponse>>(`${baseUrl}/user/application-records`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body,
      credentials: 'include',
    })
  }

  const getCurrentCampaign = (level: CampaignLevel) => {
    return $fetch<ApiResponse<CampaignSummaryResponse>>(`${baseUrl}/user/campaign/current`, {
      method: 'GET',
      query: { level },
      headers: getAuthHeaders(),
      credentials: 'include',
    })
  }

  const getMyApplicationRecord = (campaignPublicId: string) => {
    return $fetch<ApiResponse<ApplicationRecordResponse>>(
      `${baseUrl}/user/application-records/campaigns/${campaignPublicId}/me`,
      {
        method: 'GET',
        headers: getAuthHeaders(),
        credentials: 'include',
      },
    )
  }

  const getCampaignCriteriaTree = (campaignPublicId: string) => {
    return $fetch<ApiResponse<StandardDTO[]>>(
      `${baseUrl}/user/campaign/${campaignPublicId}/criteria-tree`,
      {
        method: 'GET',
        headers: getAuthHeaders(),
        credentials: 'include',
      },
    )
  }

  const saveEvidence = (campaignPublicId: string, body: SaveEvidenceRequest) => {
    return $fetch<ApiResponse<void>>(
      `${baseUrl}/user/application-records/campaigns/${campaignPublicId}/evidences`,
      {
        method: 'POST',
        headers: getAuthHeaders(),
        body,
        credentials: 'include',
      },
    )
  }

  const saveEvidenceFile = (campaignPublicId: string, criteriaId: number, file: File) => {
    const formData = new FormData()
    formData.append('file', file)

    return $fetch<ApiResponse<void>>(
      `${baseUrl}/user/application-records/campaigns/${campaignPublicId}/evidences`,
      {
        method: 'POST',
        query: { criteriaId },
        headers: getAuthHeaders(),
        body: formData,
        credentials: 'include',
      },
    )
  }

  const submitApplicationRecord = (campaignPublicId: string) => {
    return $fetch<ApiResponse<ApplicationRecordResponse>>(
      `${baseUrl}/user/application-records/campaigns/${campaignPublicId}/submit`,
      {
        method: 'PATCH',
        headers: getAuthHeaders(),
        credentials: 'include',
      },
    )
  }

  return {
    createApplicationRecord,
    getCurrentCampaign,
    getMyApplicationRecord,
    getCampaignCriteriaTree,
    saveEvidence,
    saveEvidenceFile,
    submitApplicationRecord,
  }
}
