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

  const getCurrentCampaign = (level: CampaignLevel, isGroup?: boolean) => {
    return authFetch<ApiResponse<CampaignSummaryResponse>>(`${baseUrl}/user/campaign/current`, {
      method: 'GET',
      query: isGroup !== undefined ? { level, isGroup } : { level },
    })
  }

  const getMyApplicationRecord = (campaignPublicId: string, isGroup?: boolean, level?: CampaignLevel) => {
    return authFetch<ApiResponse<ApplicationRecordResponse>>(
      `${baseUrl}/user/application-records/campaigns/${campaignPublicId}/me`,
      {
        method: 'GET',
        query: {
          ...(isGroup !== undefined ? { isGroup } : {}),
          ...(level ? { level } : {}),
        },
      },
    )
  }

  const getCampaignCriteriaTree = (campaignPublicId: string, isGroup?: boolean, level?: CampaignLevel) => {
    return authFetch<ApiResponse<StandardDTO[]>>(
      `${baseUrl}/user/campaign/${campaignPublicId}/criteria-tree`,
      {
        method: 'GET',
        query: {
          ...(isGroup !== undefined ? { isGroup } : {}),
          ...(level ? { level } : {}),
        },
      },
    )
  }

  const saveEvidence = (campaignPublicId: string, body: SaveEvidenceRequest, isGroup?: boolean) => {
    return authFetch<ApiResponse<void>>(
      `${baseUrl}/user/application-records/campaigns/${campaignPublicId}/evidences`,
      {
        method: 'POST',
        query: isGroup !== undefined ? { isGroup } : {},
        body,
      },
    )
  }

  const saveEvidenceFile = (campaignPublicId: string, criteriaPublicId: string, file: File, isGroup?: boolean) => {
    const formData = new FormData()
    formData.append('file', file)

    return authFetch<ApiResponse<void>>(
      `${baseUrl}/user/application-records/campaigns/${campaignPublicId}/evidences`,
      {
        method: 'POST',
        query: { criteriaPublicId, isGroup: isGroup ?? false },
        body: formData,
      },
    )
  }

  const submitApplicationRecord = (campaignPublicId: string, isGroup?: boolean, level?: CampaignLevel) => {
    return authFetch<ApiResponse<ApplicationRecordResponse>>(
      `${baseUrl}/user/application-records/campaigns/${campaignPublicId}/submit`,
      {
        method: 'PATCH',
        query: {
          ...(isGroup !== undefined ? { isGroup } : {}),
          ...(level ? { level } : {}),
        },
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
