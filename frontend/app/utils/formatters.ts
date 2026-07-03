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

export const isCampaignMatchLevel = (campaignLevel: string, targetLevel: string): boolean => {
  if (targetLevel === 'UNIVERSITY') {
    return ['UNIVERSITY', 'UNI_CITY', 'UNI_NATION', 'ALL'].includes(campaignLevel)
  }
  if (targetLevel === 'CITY') {
    return ['CITY', 'UNI_CITY', 'CITY_NATION', 'ALL'].includes(campaignLevel)
  }
  if (targetLevel === 'NATION') {
    return ['NATION', 'UNI_NATION', 'CITY_NATION', 'ALL'].includes(campaignLevel)
  }
  return campaignLevel === targetLevel
}
