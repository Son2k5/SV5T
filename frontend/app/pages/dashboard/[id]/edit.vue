<template>
  <div class="flex h-full flex-col gap-6 xl:flex-row-reverse">
    <ProfileEditFloaterBox class="w-full shrink-0 xl:sticky xl:top-24 xl:w-80 xl:self-start" />

    <UForm
      :state="formState"
      :schema="schema"
      class="flex w-full flex-col gap-6"
      @submit="onSubmit"
    >
      <fieldset
        :disabled="!isEditing"
        class="contents"
      >
        <CommonPageSection
          title="Thông tin Cá nhân"
          title-icon="i-heroicons-user-solid"
          inner-class="grid grid-cols-1 md:grid-cols-3 gap-x-6 gap-y-5"
        >
          <UFormField
            label="Họ và tên"
            name="fullName"
            required
            class="w-full md:col-span-2"
          >
            <UInput
              v-model="formState.fullName"
              class="w-full"
            />
          </UFormField>

          <UFormField
            label="Giới tính"
            name="gender"
            required
            class="w-full"
          >
            <USelect
              v-model="formState.gender"
              class="w-full"
              :items="genderOptions"
              color="info"
            />
          </UFormField>

          <UFormField
            label="Ngày sinh"
            name="birthDay"
            required
            class="w-full"
          >
            <UPopover class="w-full h-full">
              <UButton
                :label="formatDate(formState.birthDay) || 'Chọn ngày sinh'"
                color="neutral"
                class="w-full justify-between bg-white text-[#1E293B] ring ring-inset ring-accented hover:bg-neutral-100 disabled:cursor-default disabled:bg-white disabled:text-[#64748B] disabled:opacity-100 disabled:hover:bg-white"
                trailing-icon="i-heroicons-calendar-solid"
              />
              <template #content>
                <UCalendar
                  v-model="computedBirthDay"
                  color="neutral"
                />
              </template>
            </UPopover>
          </UFormField>

          <UFormField
            label="Dân tộc"
            name="ethnicity"
            required
            class="w-full"
          >
            <UInput
              v-model="formState.ethnicity"
              class="w-full"
            />
          </UFormField>

          <UFormField
            label="Số CCCD/CMND"
            name="identityCardNumber"
            required
            class="w-full"
          >
            <UInput
              v-model="formState.identityCardNumber"
              class="w-full"
            />
          </UFormField>
        </CommonPageSection>

        <CommonPageSection
          title="Thông tin Học tập"
          title-icon="i-heroicons-book-open-solid"
          inner-class="grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-5"
        >
          <UFormField
            label="Trường"
            name="school"
            required
            class="w-full"
          >
            <UInput
              v-model="formState.school"
              disabled
              class="w-full"
            />
          </UFormField>

          <UFormField
            label="Khoa"
            name="faculty"
            required
            class="w-full"
          >
            <UInput
              v-model="formState.faculty"
              class="w-full"
            />
          </UFormField>

          <UFormField
            label="Ngành"
            name="major"
            class="w-full"
          >
            <UInput
              v-model="formState.major"
              class="w-full"
            />
          </UFormField>

          <UFormField
            label="Sinh viên năm"
            name="academicYear"
            required
            class="w-full"
          >
            <UInput
              v-model.number="formState.academicYear"
              type="number"
              min="1"
              max="10"
              class="w-full"
            />
          </UFormField>

          <UFormField
            label="Mã sinh viên"
            name="studentCode"
            required
            class="w-full"
          >
            <UInput
              v-model="formState.studentCode"
              class="w-full"
            />
          </UFormField>

          <UFormField
            label="Lớp hành chính"
            name="administrativeClass"
            required
            class="w-full"
          >
            <UInput
              v-model="formState.administrativeClass"
              class="w-full"
            />
          </UFormField>

          <UFormField
            label="Chức vụ hiện tại"
            name="currentPosition"
            required
            class="w-full md:col-span-2"
          >
            <UInput
              v-model="formState.currentPosition"
              class="w-full"
            />
          </UFormField>
        </CommonPageSection>

        <CommonPageSection
          title="Địa chỉ"
          title-icon="i-heroicons-map-pin-solid"
          inner-class="flex flex-col gap-5"
        >
          <div class="w-full rounded-2xl border border-[#E5E7EB] bg-[#F8FAFC] p-4">
            <h3 class="mb-4 flex items-center gap-2 text-base font-bold text-[#1E293B]">
              <UIcon
                name="i-lucide-home"
                class="size-5 text-[#3B82F6]"
              />
              Địa chỉ thường trú
            </h3>
            <div class="grid grid-cols-1 gap-x-6 gap-y-5 md:grid-cols-2">
              <UFormField
                label="Tỉnh / Thành phố"
                name="permanentAddress.provinceCity"
                required
                class="w-full"
              >
                <USelect
                  v-model="formState.permanentAddress.provinceCity"
                  :items="provinces ?? []"
                  value-key="label"
                  class="w-full"
                />
              </UFormField>

              <UFormField
                label="Quận / Huyện"
                name="permanentAddress.district"
                required
                class="w-full"
              >
                <UInput
                  v-model="formState.permanentAddress.district"
                  class="w-full"
                />
              </UFormField>

              <UFormField
                label="Địa chỉ cụ thể"
                name="permanentAddress.detailAddress"
                required
                class="w-full md:col-span-2"
              >
                <UTextarea
                  v-model="formState.permanentAddress.detailAddress"
                  class="w-full"
                  autoresize
                />
              </UFormField>
            </div>
          </div>

          <div class="w-full rounded-2xl border border-[#E5E7EB] bg-[#F8FAFC] p-4">
            <div class="mb-4 flex items-center gap-3">
              <h3 class="flex items-center gap-2 text-base font-bold text-[#1E293B]">
                <UIcon
                  name="i-lucide-map-pin"
                  class="size-5 text-[#3B82F6]"
                />
                Địa chỉ tạm trú
              </h3>
              <UButton
                label="Giống thường trú"
                variant="soft"
                color="info"
                size="sm"
                class="ml-auto cursor-pointer"
                @click="copyAddress"
              />
            </div>
            <div class="grid grid-cols-1 gap-x-6 gap-y-5 md:grid-cols-2">
              <UFormField
                label="Tỉnh / Thành phố"
                name="temporaryAddress.provinceCity"
                required
                class="w-full"
              >
                <USelect
                  v-model="formState.temporaryAddress.provinceCity"
                  :items="provinces ?? []"
                  value-key="label"
                  class="w-full"
                />
              </UFormField>

              <UFormField
                label="Quận / Huyện"
                name="temporaryAddress.district"
                required
                class="w-full"
              >
                <UInput
                  v-model="formState.temporaryAddress.district"
                  class="w-full"
                />
              </UFormField>

              <UFormField
                label="Địa chỉ cụ thể"
                name="temporaryAddress.detailAddress"
                required
                class="w-full md:col-span-2"
              >
                <UTextarea
                  v-model="formState.temporaryAddress.detailAddress"
                  class="w-full"
                  autoresize
                />
              </UFormField>
            </div>
          </div>
        </CommonPageSection>

        <CommonPageSection
          title="Thông tin Liên hệ"
          title-icon="i-heroicons-phone-solid"
          inner-class="grid grid-cols-1 md:grid-cols-2 gap-6"
        >
          <UFormField
            label="Số điện thoại"
            name="phoneNumber"
            required
            class="w-full"
          >
            <UInput
              v-model="formState.phoneNumber"
              class="w-full"
            />
          </UFormField>

          <UFormField
            label="Email liên hệ"
            name="contactEmail"
            required
            class="w-full"
          >
            <UInput
              v-model="formState.contactEmail"
              class="w-full"
            />
          </UFormField>

          <UFormField
            label="Email tài khoản"
            name="email"
            class="w-full md:col-span-2"
          >
            <UInput
              :model-value="profile?.email ?? ''"
              class="w-full"
              disabled
            />
          </UFormField>
        </CommonPageSection>

        <CommonPageSection
          title="Thông tin Liên quan"
          title-icon="i-heroicons-cube-solid"
          inner-class="grid grid-cols-1 md:grid-cols-2 gap-6"
        >
          <UFormField
            label="Chức vụ Đoàn hội"
            name="unionPosition"
            class="w-full"
          >
            <UInput
              v-model="formState.unionPosition"
              class="w-full"
            />
          </UFormField>

          <UFormField
            label="Đảng viên / Đoàn viên"
            name="politicalStatus"
            required
            class="w-full"
          >
            <USelect
              v-model="formState.politicalStatus"
              :items="politicalStatusOptions"
              class="w-full"
              color="info"
            />
          </UFormField>
        </CommonPageSection>
      </fieldset>

      <div class="sticky bottom-4 z-10 flex justify-end gap-3 rounded-2xl">
        <UButton
          v-if="isEditing"
          type="button"
          color="neutral"
          size="lg"
          label="Hủy chỉnh sửa"
          variant="outline"
          class="px-6"
          @click="cancelEditing"
        />
        <UButton
          v-else
          color="neutral"
          to="/dashboard"
          size="lg"
          label="Quay lại"
          variant="outline"
          class="px-6"
        />
        <UButton
          v-if="isEditing"
          type="submit"
          color="info"
          class="cursor-pointer px-6 shadow-sm"
          icon="i-lucide-check"
          size="lg"
          label="Lưu thông tin"
          :loading="isLoading"
          :ui="{ label: ['mx-auto text-lg', isLoading && 'hidden'], leadingIcon: 'mx-auto' }"
        />
        <UButton
          v-else
          type="button"
          color="info"
          class="cursor-pointer px-6 shadow-sm"
          icon="i-lucide-pen-line"
          size="lg"
          label="Thay đổi"
          @click="enableEditing"
        />
      </div>
    </UForm>
  </div>
</template>

<script lang="ts" setup>
import { z } from 'zod'
import type { CalendarDate } from '@internationalized/date'
import { getLocalTimeZone, parseDate, today } from '@internationalized/date'
import { useCurrentUser } from '~/composables/profile/useCurrentUser'
import type { Gender, PoliticalStatus, UserProfileUpdateRequest } from '~/types/profile'

const { profile, updateCurrentUser, isLoading } = await useCurrentUser()
const { provinces } = await useLocation()

const genderOptions = [
  { label: 'Nam', value: 'MALE' },
  { label: 'Nữ', value: 'FEMALE' },
  { label: 'Khác', value: 'OTHER' },
]

const politicalStatusOptions = [
  { label: 'Chưa là Đoàn viên/Đảng viên', value: 'NONE' },
  { label: 'Đoàn viên', value: 'UNION_MEMBER' },
  { label: 'Đảng viên', value: 'PARTY_MEMBER' },
]

const currentDay = today(getLocalTimeZone())
const isEditing = ref(false)

const formState = reactive<Omit<UserProfileUpdateRequest, 'major' | 'unionPosition'> & { major: string, unionPosition: string }>({
  fullName: profile.value?.fullName ?? '',
  birthDay: profile.value?.birthDay ?? '',
  gender: profile.value?.gender ?? 'MALE',
  identityCardNumber: profile.value?.identityCardNumber ?? '',
  ethnicity: profile.value?.ethnicity ?? '',
  school: profile.value?.school ?? 'Đại học Hà Nội',
  major: profile.value?.major ?? '',
  academicYear: profile.value?.academicYear ?? 1,
  studentCode: profile.value?.studentCode ?? '',
  administrativeClass: profile.value?.administrativeClass ?? '',
  faculty: profile.value?.faculty ?? '',
  currentPosition: profile.value?.currentPosition ?? '',
  permanentAddress: {
    provinceCity: profile.value?.permanentAddress?.provinceCity ?? '',
    district: profile.value?.permanentAddress?.district ?? '',
    detailAddress: profile.value?.permanentAddress?.detailAddress ?? '',
  },
  temporaryAddress: {
    provinceCity: profile.value?.temporaryAddress?.provinceCity ?? '',
    district: profile.value?.temporaryAddress?.district ?? '',
    detailAddress: profile.value?.temporaryAddress?.detailAddress ?? '',
  },
  contactEmail: profile.value?.contactEmail ?? profile.value?.email ?? '',
  phoneNumber: profile.value?.phoneNumber ?? '',
  unionPosition: profile.value?.unionPosition ?? '',
  politicalStatus: profile.value?.politicalStatus ?? 'NONE',
})

const cloneFormStateFrom = (state: UserProfileUpdateRequest) =>
  JSON.parse(JSON.stringify(state)) as UserProfileUpdateRequest

const cloneFormState = () =>
  cloneFormStateFrom(toRaw(formState) as UserProfileUpdateRequest)

let formSnapshot = cloneFormState()

const enableEditing = () => {
  formSnapshot = cloneFormState()
  isEditing.value = true
}

const cancelEditing = () => {
  Object.assign(formState, cloneFormStateFrom(formSnapshot))
  isEditing.value = false
}

const computedBirthDay = computed({
  get: (): CalendarDate => {
    if (!formState.birthDay) return currentDay

    try {
      return parseDate(formState.birthDay)
    }
    catch {
      return currentDay
    }
  },
  set: (newDate: CalendarDate | undefined) => {
    formState.birthDay = newDate?.toString() ?? ''
  },
})

const schema = z.object({
  fullName: z.string().trim().min(1, 'Họ và tên là bắt buộc').max(100, 'Họ và tên không được vượt quá 100 ký tự'),
  birthDay: z.string().min(1, 'Ngày sinh là bắt buộc').refine((value) => {
    try {
      return parseDate(value).compare(currentDay) < 0
    }
    catch {
      return false
    }
  }, 'Ngày sinh phải là ngày trong quá khứ'),
  gender: z.enum(['MALE', 'FEMALE', 'OTHER']),
  identityCardNumber: z.string().regex(/^(\d{9}|\d{12})$/, 'Số CCCD/CMND phải gồm 9 hoặc 12 chữ số'),
  ethnicity: z.string().trim().min(1, 'Dân tộc là bắt buộc').max(50, 'Dân tộc không được vượt quá 50 ký tự'),
  school: z.string().trim().min(1, 'Trường là bắt buộc').max(150, 'Trường không được vượt quá 150 ký tự'),
  major: z.string().trim().max(150, 'Ngành học không được vượt quá 150 ký tự'),
  academicYear: z.number().int().min(1, 'Sinh viên năm phải từ 1 trở lên').max(10, 'Sinh viên năm không được vượt quá 10'),
  studentCode: z.string().trim().min(1, 'Mã sinh viên là bắt buộc').max(50).regex(/^[A-Za-z0-9._-]+$/, 'Mã sinh viên chỉ được chứa chữ, số, dấu gạch ngang, gạch dưới hoặc dấu chấm'),
  administrativeClass: z.string().trim().min(1, 'Lớp hành chính là bắt buộc').max(100),
  faculty: z.string().trim().min(1, 'Khoa là bắt buộc').max(150),
  currentPosition: z.string().trim().min(1, 'Chức vụ hiện tại là bắt buộc').max(100),
  permanentAddress: z.object({
    provinceCity: z.string().trim().min(1, 'Tỉnh/thành phố là bắt buộc').max(100, 'Tỉnh/thành phố không được vượt quá 100 ký tự'),
    district: z.string().trim().min(1, 'Quận/huyện là bắt buộc').max(100, 'Quận/huyện không được vượt quá 100 ký tự'),
    detailAddress: z.string().trim().min(1, 'Địa chỉ cụ thể là bắt buộc').max(500, 'Địa chỉ cụ thể không được vượt quá 500 ký tự'),
  }),
  temporaryAddress: z.object({
    provinceCity: z.string().trim().min(1, 'Tỉnh/thành phố là bắt buộc').max(100, 'Tỉnh/thành phố không được vượt quá 100 ký tự'),
    district: z.string().trim().min(1, 'Quận/huyện là bắt buộc').max(100, 'Quận/huyện không được vượt quá 100 ký tự'),
    detailAddress: z.string().trim().min(1, 'Địa chỉ cụ thể là bắt buộc').max(500, 'Địa chỉ cụ thể không được vượt quá 500 ký tự'),
  }),
  contactEmail: z.string().trim().min(1, 'Email liên hệ là bắt buộc').email('Email liên hệ không hợp lệ').max(100, 'Email liên hệ không được vượt quá 100 ký tự'),
  phoneNumber: z.string().regex(/^[0-9+\-\s]{8,20}$/, 'Số điện thoại không hợp lệ'),
  unionPosition: z.string().trim().max(100, 'Chức vụ đoàn hội không được vượt quá 100 ký tự'),
  politicalStatus: z.enum(['NONE', 'UNION_MEMBER', 'PARTY_MEMBER']),
})

const copyAddress = () => {
  if (!isEditing.value) return

  formState.temporaryAddress = { ...formState.permanentAddress }
}

const onSubmit = async () => {
  if (!isEditing.value) return

  const updatedProfile = await updateCurrentUser({
    ...formState,
    gender: formState.gender as Gender,
    politicalStatus: formState.politicalStatus as PoliticalStatus,
    major: formState.major.trim() || null,
    unionPosition: formState.unionPosition.trim() || null,
    permanentAddress: { ...formState.permanentAddress },
    temporaryAddress: { ...formState.temporaryAddress },
  })

  if (updatedProfile) {
    formSnapshot = cloneFormState()
    isEditing.value = false
  }
}
</script>
