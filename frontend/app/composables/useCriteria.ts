import type { ApiResponse } from '~/types/auth'
import type { CriteriaStandard } from '~/types/criteria'
import { getErrorMessage } from '~/utils/errors'

export const useCriteria = async (level = 'UNIVERSITY') => {
  const { authFetch } = useAuth()
  const toast = useToast()

  const fetchCriteria = async () => {
    try {
      // 1. Get current campaign
      const campaignRes = await authFetch<ApiResponse<{ publicId: string }>>(`/user/campaign/current`, {
        method: 'GET',
        query: { level },
      })

      if (!campaignRes?.data?.publicId) {
        return []
      }

      const campaignPublicId = campaignRes.data.publicId

      // 2. Fetch criteria tree for this campaign
      const criteriaRes = await authFetch<ApiResponse<CriteriaStandard[]>>(`/user/campaign/${campaignPublicId}/criteria-tree`, {
        method: 'GET',
      })

      return criteriaRes.data || []
    }
    catch (error) {
      toast.add({
        title: 'Thất bại nhận thông tin!',
        description: getErrorMessage(error, 'Vui lòng thử lại sau.'),
        color: 'error',
      })
      return []
    }
  }

  const { data } = await useAsyncData('criteria', () => fetchCriteria())

  return { data }
}
