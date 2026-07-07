export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}

export type ApplicationStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED'
export type CampaignLevel = 'UNIVERSITY' | 'CITY' | 'NATION' | 'UNI_CITY' | 'UNI_NATION' | 'CITY_NATION' | 'ALL'
export type EvidenceStatus = 'PENDING' | 'APPROVED' | 'REJECTED'
export type EvidenceType = 'IMAGE' | 'PDF' | 'FILE' | 'LINK' | 'NONE'

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
  description: string | null
  isMandatory: boolean
  requiredChildrenCount: number
  evidenceType: EvidenceType | null
  evidenceUrl: string | null
  evidenceStatus: EvidenceStatus | null
  reviewerComment: string | null
  evidenceOriginalFilename?: string | null
  evidencePublicId?: string | null
  subCriteriaList: CriteriaDTO[]
}

export interface StandardDTO {
  publicId: string
  name: string
  description: string | null
  criteriaDTOList: CriteriaDTO[]
}
