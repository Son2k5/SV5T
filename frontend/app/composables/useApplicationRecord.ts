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
  const { authFetch } = useAuth()

  const createApplicationRecord = (body: CreateApplicationRecordRequest) => {
    return authFetch<ApiResponse<ApplicationRecordResponse>>(`${baseUrl}/user/application-records`, {
      method: 'POST',
      body,
    })
  }

  const getCurrentCampaign = (level: CampaignLevel) => {
    return authFetch<ApiResponse<CampaignSummaryResponse>>(`${baseUrl}/user/campaign/current`, {
      method: 'GET',
      query: { level },
    })
  }

  const getMyApplicationRecord = (campaignPublicId: string) => {
    return authFetch<ApiResponse<ApplicationRecordResponse>>(
      `${baseUrl}/user/application-records/campaigns/${campaignPublicId}/me`,
      {
        method: 'GET',
      },
    )
  }

  const getCampaignCriteriaTree = (campaignPublicId: string) => {
    return authFetch<ApiResponse<StandardDTO[]>>(
      `${baseUrl}/user/campaign/${campaignPublicId}/criteria-tree`,
      {
        method: 'GET',
      },
    )
  }

  const saveEvidence = (campaignPublicId: string, body: SaveEvidenceRequest) => {
    return authFetch<ApiResponse<void>>(
      `${baseUrl}/user/application-records/campaigns/${campaignPublicId}/evidences`,
      {
        method: 'POST',
        body,
      },
    )
  }

  const saveEvidenceFile = (campaignPublicId: string, criteriaId: number, file: File) => {
    const formData = new FormData()
    formData.append('file', file)

    return authFetch<ApiResponse<void>>(
      `${baseUrl}/user/application-records/campaigns/${campaignPublicId}/evidences`,
      {
        method: 'POST',
        query: { criteriaId },
        body: formData,
      },
    )
  }

  const submitApplicationRecord = (campaignPublicId: string) => {
    return authFetch<ApiResponse<ApplicationRecordResponse>>(
      `${baseUrl}/user/application-records/campaigns/${campaignPublicId}/submit`,
      {
        method: 'PATCH',
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
