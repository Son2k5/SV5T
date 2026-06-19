export type Role = 'USER' | 'ADMIN'
export type Gender = 'MALE' | 'FEMALE' | 'OTHER'
export type PoliticalStatus = 'NONE' | 'UNION_MEMBER' | 'PARTY_MEMBER'

export type ProfileAddress = {
  provinceCity: string | null
  district: string | null
  detailAddress: string | null
}

export type ProfileData = {
  publicId: string
  email: string
  role: Role
  avatar: string | null
  fullName: string | null
  birthDay: string | null
  gender: Gender | null
  identityCardNumber: string | null
  ethnicity: string | null
  school: string | null
  major: string | null
  academicYear: number | null
  studentCode: string | null
  administrativeClass: string | null
  faculty: string | null
  currentPosition: string | null
  permanentAddress: ProfileAddress | null
  temporaryAddress: ProfileAddress | null
  contactEmail: string | null
  phoneNumber: string | null
  unionPosition: string | null
  politicalStatus: PoliticalStatus | null
  verified: boolean
  active: boolean
  createdAt: string | null
  updatedAt: string | null
}

export type UserProfileAddressRequest = {
  provinceCity: string
  district: string
  detailAddress: string
}

export type UserProfileUpdateRequest = {
  fullName: string
  birthDay: string
  gender: Gender
  identityCardNumber: string
  ethnicity: string
  school: string
  major?: string | null
  academicYear: number
  studentCode: string
  administrativeClass: string
  faculty: string
  currentPosition: string
  permanentAddress: UserProfileAddressRequest
  temporaryAddress: UserProfileAddressRequest
  contactEmail: string
  phoneNumber: string
  unionPosition?: string | null
  politicalStatus: PoliticalStatus
}

const hasText = (value: string | null | undefined) =>
  typeof value === 'string' && value.trim().length > 0

const hasAddress = (address: ProfileAddress | null | undefined) =>
  hasText(address?.provinceCity)
  && hasText(address?.district)
  && hasText(address?.detailAddress)

export const isProfileComplete = (profile: ProfileData | null | undefined) =>
  Boolean(
    profile
    && hasText(profile.fullName)
    && hasText(profile.birthDay)
    && profile.gender
    && hasText(profile.identityCardNumber)
    && hasText(profile.ethnicity)
    && hasText(profile.school)
    && profile.academicYear !== null
    && profile.academicYear !== undefined
    && hasText(profile.studentCode)
    && hasText(profile.administrativeClass)
    && hasText(profile.faculty)
    && hasText(profile.currentPosition)
    && hasAddress(profile.permanentAddress)
    && hasAddress(profile.temporaryAddress)
    && hasText(profile.contactEmail)
    && hasText(profile.phoneNumber)
    && profile.politicalStatus,
  )
