export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}

export type ApplicationStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED'
export type CampaignLevel = 'UNIVERSITY' | 'CITY' | 'NATION' | 'UNI_CITY' | 'UNI_NATION' | 'CITY_NATION' | 'ALL'

export interface ApplicationRecordResponse {
  publicId: string
  campaignPublicId: string
  campaignName: string
  level: string
  status: ApplicationStatus
  note?: string
  isGroup?: boolean
  createdAt: string
  updatedAt: string
}

export type CreateApplicationRecordRequest = {
  campaignPublicId: string
  note?: string
  isGroup?: boolean
  level?: CampaignLevel
}

export interface CampaignSummaryResponse {
  publicId: string
  name: string
  academicYear: number
  level: CampaignLevel
  status?: string
  startDate?: string
  endDate?: string
  description?: string | null
  isGroup?: 'INDIVIDUAL' | 'GROUP' | 'BOTH'
}

export type SaveEvidenceRequest = {
  criteriaPublicId: string
  evidenceUrl: string
}

export interface CriteriaDTO {
  publicId: string
  name: string
  description?: string
  isMandatory: boolean
  requiredChildrenCount: number
  evidenceType?: string
  evidenceUrl?: string
  evidenceStatus?: string
  reviewerComment?: string
  subCriteriaList: CriteriaDTO[]
}

export interface StandardDTO {
  publicId: string
  name: string
  description?: string
  criteriaDTOList: CriteriaDTO[]
}
