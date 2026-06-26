export type Criteria = {
  publicId: string
  isMandatory: boolean
  name: string
  description: string
  evidenceStatus: string | null
  evidenceType: string
  reviewerComment: string | null
  subCriteriaList?: Criteria[]
}

export type CriteriaStandard = {
  publicId: string
  name: string
  description?: string
  criteriaDTOList: Criteria[]
}
