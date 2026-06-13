export type ProfileData = {
  id: string
  userName: string | null
  email: string
  role: 'USER' | 'ADMIN'
  profileCompleted: boolean
  firstName: string | null
  lastName: string | null
  avatar: string | null
  dob: string | null
  gender: 'MALE' | 'FEMALE' | 'OTHER'
  ethnicity: string | null
  idenNumber: string | null
  university: string | null
  fieldOfStudy: string | null
  courseYear: string | null
  studentCode: string | null
  classCode: string | null
  faculty: string | null
  currentPosition: string | null
  province: string | null
  commune: string | null
  specificAddress: string | null
  provinceTemp: string | null
  communeTemp: string | null
  specificAddressTemp: string | null
  phoneNumber: string | null
  organPosition: string | null
  ydMember: string | null
}
