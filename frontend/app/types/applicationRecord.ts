export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}

export type ApplicationStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED'
export type CampaignLevel = 'UNIVERSITY' | 'CITY' | 'NATION'

export interface ApplicationRecordResponse {
  publicId: string
  campaignPublicId: string
  campaignName: string
  level: string
  status: ApplicationStatus
  note?: string
  createdAt: string
  updatedAt: string
}

export interface CreateApplicationRecordRequest {
  campaignPublicId: string
  note?: string
}

export interface CampaignSummaryResponse {
  publicId: string
  name: string
  academicYear: number
  level: CampaignLevel
  status?: string
  startDate?: string
  endDate?: string
}

export interface SaveEvidenceRequest {
  criteriaId: number
  evidenceUrl: string
}

export interface CriteriaDTO {
  id: number
  name: string
  description?: string
  isMandatory: boolean
  requiredChildrenCount: number
  evidenceType?: string
  evidenceUrl?: string
  evidenceStatus?: boolean
  reviewerComment?: string
  subCriteriaList: CriteriaDTO[]
}

export interface StandardDTO {
  id: number
  name: string
  description?: string
  criteriaDTOList: CriteriaDTO[]
}
