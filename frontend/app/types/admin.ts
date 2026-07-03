export type AdminRole = 'ADMIN' | 'MENTOR' | 'USER'
export type CampaignStatus = 'DRAFT' | 'ACTIVE' | 'CLOSED' | 'ARCHIVED'
export type CampaignLevel = 'UNIVERSITY' | 'CITY' | 'NATION' | 'UNI_CITY' | 'UNI_NATION' | 'CITY_NATION' | 'ALL'
export type EvidenceStatus = 'PENDING' | 'APPROVED' | 'REJECTED'
export type EvidenceType = 'IMAGE' | 'PDF' | 'FILE' | 'LINK' | 'NONE'

export interface AdminApiResponse<T> {
  success: boolean
  message: string
  data: T
}

export interface AdminPage<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface AdminUser {
  id: number
  publicId: string
  email: string
  role: AdminRole
  active: boolean
  verified: boolean
  fullName: string | null
  studentCode: string | null
  phoneNumber: string | null
  createdAt: string
  updatedAt: string
}

export interface AdminUserForm {
  email: string
  password?: string
  role: AdminRole
  active: boolean
  fullName?: string
  studentCode?: string
}

export interface Campaign {
  id: number
  publicId: string
  name: string
  description: string | null
  academicYear: number
  level: CampaignLevel
  status: CampaignStatus
  startDate: string | null
  endDate: string | null
  criteriaCount: number
  isGroup: 'INDIVIDUAL' | 'GROUP' | 'BOTH'
}

export interface CampaignStats {
  campaignId: string
  totalParticipants: number
  pendingResults: number
  passResults: number
  failResults: number
  pendingEvidences: number
  approvedEvidences: number
  rejectedEvidences: number
  passRate: number
  failRate: number
}

export interface CampaignForm {
  name: string
  description: string
  academicYear: number
  level: CampaignLevel
  startDate: string
  endDate: string
  status: CampaignStatus
  isGroup: 'INDIVIDUAL' | 'GROUP' | 'BOTH'
}

export interface Standard {
  publicId: string
  name: string
  description: string | null
  isGroup?: boolean
  level?: CampaignLevel
}

export interface Criteria {
  publicId: string
  name: string
  description: string | null

  parentPublicId: string | null

  orderIndex: number
  mandatory: boolean
  requiredChildrenCount: number
  evidenceType: EvidenceType | null
}

export interface CriteriaForm {
  name: string
  description: string
  orderIndex: number
  mandatory: boolean
  requiredChildrenCount: number
  evidenceType: EvidenceType
  parentPublicId: string | null
}

export interface CriteriaNode extends Criteria {
  children: CriteriaNode[]
}

export interface StandardWithCriteria extends Standard {
  criteria: CriteriaNode[]
}

export interface Evidence {
  publicId: string

  evidenceUrl: string
  evidenceOriginalFilename: string | null
  evidenceFormat: string | null

  status: EvidenceStatus
  reviewerComment: string | null

  criteriaPublicId: string
  criteriaName: string

  userPublicId: string
  userEmail: string
  studentName: string | null

  campaignPublicId: string
  campaignName: string
}

export interface DashboardStats {
  totalUsers: number
  activeCampaigns: number
  pendingEvidences: number
  passRate: number
  failRate: number
}

export interface CampaignProgress {
  campaignId: string
  campaignName: string
  percent: number
  reviewed: number
  total: number
}

export interface AuditLog {
  id: number
  entity: string
  entityId: number | null
  action: string
  actorId: number | null
  actorEmail: string | null
  oldValue: string | null
  newValue: string | null
  createdAt: string
}

export interface SystemSetting {
  keyName: string
  value: string
  description: string | null
}

export interface SystemSettingForm {
  keyName: string
  value: string
  description?: string
}

export interface StudentApplication {
  applicationPublicId: string
  userPublicId: string
  userEmail: string
  studentName: string | null
  studentCode: string | null
  campaignPublicId: string
  campaignName: string
  level: CampaignLevel
  status: string
  createdAt: string
  updatedAt: string
  totalEvidences: number
}

export interface CriteriaResultDTO {
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
  subCriteriaList: CriteriaResultDTO[]
}

export interface StandardResultDTO {
  publicId: string
  name: string
  description: string | null
  criteriaDTOList: CriteriaResultDTO[]
}
