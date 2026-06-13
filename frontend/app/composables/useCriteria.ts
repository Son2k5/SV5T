import { GetCriteriaEndpoint } from '~/constants/endpoints'
import type { ApiResponse } from '~/types/auth'
import type { CriteriaStandard } from '~/types/criteria'
import { getErrorMessage } from '~/utils/errors'

export const useCriteria = async () => {
  const { accessToken } = useAuth()

  const toast = useToast()

  const fetchCriteria = async () => {
    try {
      const { data } = await $fetch<ApiResponse<CriteriaStandard[]>>(`${GetCriteriaEndpoint()}/1`, {
        method: 'GET',
        headers: {
          Authorization: `Bearer ${accessToken.value}`,
        },
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

  const { data } = await useAsyncData(`criteria-${accessToken}`, () => fetchCriteria())

  return { data }
}
