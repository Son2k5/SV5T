export const formatGender: Record<string, string> = {
  MALE: 'Nam',
  FEMALE: 'Nữ',
  OTHER: 'Khác',
}

export const formatPoliticalStatus: Record<string, string> = {
  NONE: 'Chưa là Đoàn viên/Đảng viên',
  UNION_MEMBER: 'Đoàn viên',
  PARTY_MEMBER: 'Đảng viên',
}

export const formatDate = (date: string | undefined | null) => {
  if (!date) return ''
  const [year, month, day] = date.split('-')
  const safeDay = day?.padStart(2, '0')
  const safeMonth = month?.padStart(2, '0')
  return `${safeDay}/${safeMonth}/${year}`
}

export const formatAddress = (
  address?: {
    provinceCity?: string | null
    district?: string | null
    detailAddress?: string | null
  } | null,
) => {
  const parts = [
    address?.detailAddress,
    address?.district,
    address?.provinceCity,
  ].filter(Boolean)

  return parts.join(', ')
}
