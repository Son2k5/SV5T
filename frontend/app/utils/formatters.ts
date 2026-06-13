export const formatGender: Record<string, string> = {
  MALE: 'Nam',
  FEMALE: 'Nữ',
  OTHER: 'Khác',
}

export const formatDate = (date: string | undefined | null) => {
  if (!date) return ''
  const [year, month, day] = date.split('-')
  const safeDay = day?.padStart(2, '0')
  const safeMonth = month?.padStart(2, '0')
  return `${safeDay}/${safeMonth}/${year}`
}
