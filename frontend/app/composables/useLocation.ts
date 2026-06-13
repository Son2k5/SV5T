import type { ProvinceResponse } from '~/types/location'

export const useLocation = async () => {
  const isLoading = ref(false)

  const fetchProvinces = async () => {
    isLoading.value = true
    try {
      const rawData = await $fetch<ProvinceResponse[]>('https://provinces.open-api.vn/api/v1/p/')
      const data = rawData.map(p => ({ code: p.code, label: p.name }))
      return data
    }
    catch (error) {
      console.error('Failed to fetch provinces', error)
    }
    finally {
      isLoading.value = false
    }
  }

  const { data: provinces } = await useAsyncData('location-provinces', () => fetchProvinces())

  return {
    provinces,
    isLoading,
  }
}
