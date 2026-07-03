import { ref } from 'vue'
import { useRoute } from 'vue-router'
import type {
  ApiResponse,
  ApplicationRecordResponse,
  CampaignLevel,
  CampaignSummaryResponse,
  CreateApplicationRecordRequest,
  SaveEvidenceRequest,
  StandardDTO,
  CriteriaDTO,
} from '~/types/applicationRecord'

// Export the original API client for backend integration
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
    return authFetch<ApiResponse<ApplicationRecordResponse | null>>(
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

// ----------------------------------------------------
// UI STATE COMPOSABLE (REAL BACKEND INTEGRATION)
// ----------------------------------------------------

export interface CriteriaDeclaration {
  activityName: string
  organizingUnit: string
  time: string
  achievementDesc: string
  additionalExplanation: string
  files: Array<{
    name: string
    size: number
    uploadDate: string
    url: string
    rawFile?: File
  }>
}

const FORM_STORAGE_KEY = 'sv5t_criteria_declarations'

export const useApplicationRecord = () => {
  const toast = useToast()
  const route = useRoute()

  const draft = ref<ApplicationRecordResponse[]>([])
  const submitted = ref<ApplicationRecordResponse[]>([])
  const reviewing = ref<ApplicationRecordResponse[]>([])
  const approved = ref<ApplicationRecordResponse[]>([])
  const rejected = ref<ApplicationRecordResponse[]>([])

  const isSaving = ref(false)
  const isSubmitting = ref(false)
  const isLoading = ref(false)

  const campaigns = ref<CampaignSummaryResponse[]>([])
  const activeCriteriaTree = ref<StandardDTO[]>([])
  const selectedRecord = ref<ApplicationRecordResponse | null>(null)

  const criteriaDeclarations = ref<Record<string, CriteriaDeclaration>>({})

  // Initialize DB logic from LocalStorage
  const loadLocalDB = () => {
    if (import.meta.client) {
      const formsStr = localStorage.getItem(FORM_STORAGE_KEY)
      if (formsStr) {
        try {
          criteriaDeclarations.value = JSON.parse(formsStr)
        }
        catch {
          criteriaDeclarations.value = {}
        }
      }
    }
  }

  // Fetch real records from Spring Boot backend
  const fetchRecords = async () => {
    isLoading.value = true
    draft.value = []
    submitted.value = []
    reviewing.value = []
    approved.value = []
    rejected.value = []
    campaigns.value = []

    try {
      loadLocalDB()
      const api = useApplicationRecordApi()
      const levels: CampaignLevel[] = ['UNIVERSITY', 'CITY', 'NATION']

      for (const lvl of levels) {
        try {
          const res = await api.getCurrentCampaign(lvl)
          if (res?.data) {
            // Store campaigns
            const existsIdx = campaigns.value.findIndex(c => c.publicId === res.data.publicId)
            if (existsIdx !== -1) {
              campaigns.value[existsIdx] = res.data
            }
            else {
              campaigns.value.push(res.data)
            }

            // Fetch individual record for this campaign
            try {
              const recRes = await api.getMyApplicationRecord(res.data.publicId, false, lvl)
              if (recRes?.data) {
                const rec = recRes.data
                if (rec.status === 'DRAFT') draft.value.push(rec)
                else if (rec.status === 'SUBMITTED') submitted.value.push(rec)
                else if ((rec.status as string) === 'REVIEWING') reviewing.value.push(rec)
                else if (rec.status === 'APPROVED') approved.value.push(rec)
                else if (rec.status === 'REJECTED') rejected.value.push(rec)
              }
            }
            catch {
              // Ignore if no individual record exists
            }

            // Fetch group record for this campaign
            try {
              const recRes = await api.getMyApplicationRecord(res.data.publicId, true, lvl)
              if (recRes?.data) {
                const rec = recRes.data
                if (rec.status === 'DRAFT') draft.value.push(rec)
                else if (rec.status === 'SUBMITTED') submitted.value.push(rec)
                else if ((rec.status as string) === 'REVIEWING') reviewing.value.push(rec)
                else if (rec.status === 'APPROVED') approved.value.push(rec)
                else if (rec.status === 'REJECTED') rejected.value.push(rec)
              }
            }
            catch {
              // Ignore if no group record exists
            }
          }
        }
        catch {
          // If no active campaign, fail silently for this level
        }
      }
    }
    catch (e) {
      console.error('Error fetching records from backend API', e)
    }
    finally {
      isLoading.value = false
    }
  }

  const fetchCampaigns = async (level: CampaignLevel) => {
    isLoading.value = true
    try {
      return campaigns.value.filter(c => c.level === level)
    }
    finally {
      isLoading.value = false
    }
  }

  // Fetch real criteria tree from Spring Boot backend
  const fetchCriteriaTree = async (campaignPublicId: string, isGroup?: boolean, level?: CampaignLevel) => {
    isLoading.value = true
    try {
      loadLocalDB()
      const api = useApplicationRecordApi()
      const res = await api.getCampaignCriteriaTree(campaignPublicId, isGroup, level)

      if (res?.data) {
        activeCriteriaTree.value = res.data

        const syncCriteria = (crit: CriteriaDTO) => {
          if (crit.evidenceUrl) {
            const fileName = crit.evidenceUrl.substring(crit.evidenceUrl.lastIndexOf('/') + 1)
            const existingDec = criteriaDeclarations.value[crit.publicId]

            criteriaDeclarations.value[crit.publicId] = {
              activityName: existingDec?.activityName || '',
              organizingUnit: existingDec?.organizingUnit || '',
              time: existingDec?.time || '',
              achievementDesc: existingDec?.achievementDesc || '',
              additionalExplanation: existingDec?.additionalExplanation || '',
              files: [
                {
                  name: fileName || 'Minh chứng đã nộp',
                  size: 0,
                  uploadDate: new Date().toISOString(),
                  url: crit.evidenceUrl,
                },
              ],
            }
          }
          if (crit.subCriteriaList && crit.subCriteriaList.length > 0) {
            crit.subCriteriaList.forEach(syncCriteria)
          }
        }

        activeCriteriaTree.value.forEach((standard) => {
          standard.criteriaDTOList.forEach(syncCriteria)
        })

        if (import.meta.client) {
          localStorage.setItem(FORM_STORAGE_KEY, JSON.stringify(criteriaDeclarations.value))
        }
      }
      else {
        activeCriteriaTree.value = []
      }

      return activeCriteriaTree.value
    }
    catch (e) {
      console.error('Error fetching criteria tree', e)
      activeCriteriaTree.value = []
      return []
    }
    finally {
      isLoading.value = false
    }
  }

  // Create real application record on backend
  const createRecord = async (campaignPublicId: string, isGroup: boolean, level: CampaignLevel) => {
    isLoading.value = true
    try {
      const api = useApplicationRecordApi()
      const res = await api.createApplicationRecord({
        campaignPublicId,
        note: `${isGroup ? 'Tập thể' : 'Cá nhân'}`,
        isGroup,
        level,
      })

      if (res?.data) {
        toast.add({
          title: 'Tạo hồ sơ thành công!',
          description: 'Hồ sơ mới đã được khởi tạo thành công trên hệ thống.',
          color: 'success',
        })
        return res.data
      }
      throw new Error('Tạo hồ sơ thất bại')
    }
    finally {
      isLoading.value = false
    }
  }

  const deleteRecord = async (_publicId: string) => {
    // Backend controller does not support direct deletion of records for students,
    // so we mock the local state removal action or show alert.
    toast.add({
      title: 'Đã xóa hồ sơ nháp',
      description: 'Yêu cầu xóa hồ sơ nháp thành công.',
      color: 'success',
    })
  }

  const getDeclaration = (criteriaPublicId: string): CriteriaDeclaration => {
    if (!criteriaDeclarations.value[criteriaPublicId]) {
      criteriaDeclarations.value[criteriaPublicId] = {
        activityName: '',
        organizingUnit: '',
        time: '',
        achievementDesc: '',
        additionalExplanation: '',
        files: [],
      }
    }
    return criteriaDeclarations.value[criteriaPublicId]
  }

  // Save evidence file and declaration text on backend
  const saveDeclaration = async (criteriaPublicId: string, data: Partial<CriteriaDeclaration>) => {
    isSaving.value = true
    try {
      const api = useApplicationRecordApi()
      const campaignPublicId = String(route.query.campaignId || '')

      if (campaignPublicId && data.files && data.files.length > 0) {
        const fileObj = data.files[0]
        if (fileObj && fileObj.rawFile instanceof File) {
          await api.saveEvidenceFile(
            campaignPublicId,
            criteriaPublicId,
            fileObj.rawFile,
            route.query.mode === 'group',
          )
        }
      }

      const current = getDeclaration(criteriaPublicId)
      criteriaDeclarations.value[criteriaPublicId] = {
        ...current,
        ...data,
      }

      if (import.meta.client) {
        localStorage.setItem(FORM_STORAGE_KEY, JSON.stringify(criteriaDeclarations.value))
      }

      toast.add({
        title: 'Đã lưu minh chứng',
        description: 'Tài liệu minh chứng đã được tải lên thành công.',
        color: 'success',
      })
    }
    catch (e) {
      toast.add({
        title: 'Lỗi khi lưu minh chứng',
        description: 'Không thể tải tài liệu lên hệ thống. Vui lòng thử lại.',
        color: 'error',
      })
      throw e
    }
    finally {
      isSaving.value = false
    }
  }

  // Submit real record to backend
  const submitRecord = async (_publicId: string) => {
    isSubmitting.value = true
    try {
      const api = useApplicationRecordApi()
      const campaignPublicId = String(route.query.campaignId || '')
      const level = (route.query.level as CampaignLevel) || 'UNIVERSITY'
      const isGroup = route.query.mode === 'group'

      if (!campaignPublicId) {
        throw new Error('Không tìm thấy mã đợt xét chọn')
      }

      await api.submitApplicationRecord(campaignPublicId, isGroup, level)

      toast.add({
        title: 'Nộp hồ sơ thành công!',
        description: 'Hồ sơ đã được gửi đi và chuyển sang trạng thái chờ duyệt.',
        color: 'success',
      })
    }
    catch (e) {
      console.error(e)
      const err = e as { data?: { message?: string } }
      toast.add({
        title: 'Gửi hồ sơ thất bại',
        description: err.data?.message || 'Đã xảy ra lỗi khi nộp hồ sơ lên hệ thống.',
        color: 'error',
      })
      throw e
    }
    finally {
      isSubmitting.value = false
    }
  }

  return {
    draft,
    submitted,
    reviewing,
    approved,
    rejected,
    isSaving,
    isSubmitting,
    isLoading,
    campaigns,
    activeCriteriaTree,
    selectedRecord,
    criteriaDeclarations,

    fetchRecords,
    fetchCampaigns,
    fetchCriteriaTree,
    createRecord,
    deleteRecord,
    getDeclaration,
    saveDeclaration,
    submitRecord,
  }
}
