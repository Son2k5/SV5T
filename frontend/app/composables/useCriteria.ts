import { GetCriteriaEndpoint } from '~/constants/endpoints'
import type { ApiResponse } from '~/types/auth'
import type { CriteriaStandard } from '~/types/criteria'
import { getErrorMessage } from '~/utils/errors'

export const useCriteria = async () => {
  const { authFetch } = useAuth()

  const toast = useToast()

  const fetchCriteria = async () => {
    try {
      const { data } = await authFetch<ApiResponse<CriteriaStandard[]>>(`${GetCriteriaEndpoint()}/1`, {
        method: 'GET',
      })

      return data
    }
    catch (error) {
      toast.add({
        title: 'Thất bại nhận thông tin!',
        description: getErrorMessage(error, 'Vui lòng thử lại sau.'),
        color: 'error',
      })
    }
  }

  const { data } = await useAsyncData('criteria', () => fetchCriteria())

  return { data }
}
