export type Criteria = {
  id: number
  isMandatory: boolean
  name: string
  description: string
  evidenceStatus: boolean
  evidenceType: string
  reviewerComment: string | null
}

export type CriteriaStandard = {
  id: number
  criteriaDTOList: Criteria[]
}
