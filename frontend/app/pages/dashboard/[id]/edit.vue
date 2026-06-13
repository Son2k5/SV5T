<template>
  <div class="flex flex-col lg:flex-row-reverse gap-10 h-full">
    <ProfileEditFloaterBox class="w-full lg:w-80 lg:sticky shrink-0 top-16 self-start" />

    <UForm
      :state="formState"
      :schema="schema"
      class="w-full flex flex-col gap-10"
      @submit="onSubmit"
    >
      <CommonPageSection
        title="Thông tin Cá nhân"
        title-icon="i-heroicons-user-solid"
        inner-class="grid grid-cols-3 gap-x-8 gap-y-4"
      >
        <UFormField
          label="Họ"
          name="lastName"
          required
          class="w-full"
        >
          <UInput
            v-model="formState.lastName"
            class="w-full"
          />
        </UFormField>
        <UFormField
          label="Tên"
          name="firstName"
          required
          class="w-full col-span-2"
        >
          <UInput
            v-model="formState.firstName"
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
          name="dob"
          class="w-full col-span-2 md:col-span-1"
        >
          <UPopover class="w-full h-full">
            <UButton
              :label="formatDate(formState.dob)"
              color="neutral"
              class="bg-white hover:bg-neutral-100 text-black ring ring-inset ring-accented justify-between"
              trailing-icon="i-heroicons-calendar-solid"
            />
            <template #content>
              <UCalendar
                v-model="computedDob"
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
          name="idenNumber"
          required
          class="w-full col-span-2"
        >
          <UInput
            v-model="formState.idenNumber"
            class="w-full"
          />
        </UFormField>
      </CommonPageSection>

      <CommonPageSection
        title="Thông tin Học tập"
        title-icon="i-heroicons-book-open-solid"
        inner-class="grid grid-cols-2 gap-x-8 gap-y-4"
      >
        <UFormField
          label="Trường"
          name="university"
          required
          class="w-full col-span-2 md:col-span-1"
        >
          <UInput
            v-model="formState.university"
            disabled
            class="w-full"
          />
        </UFormField>
        <div class="grid grid-cols-2 gap-4 col-span-2 md:col-span-1">
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
            name="fieldOfStudy"
            required
            class="w-full"
          >
            <UInput
              v-model="formState.fieldOfStudy"
              class="w-full"
            />
          </UFormField>
        </div>
        <div class="grid grid-cols-2 gap-4 col-span-2 md:col-span-1">
          <UFormField
            label="Năm nhập học"
            name="courseYear"
            required
            class="w-full"
          >
            <UInput
              v-model="formState.courseYear"
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
        </div>
        <div class="grid grid-cols-2 gap-4 col-span-2 md:col-span-1">
          <UFormField
            label="Lớp hành chính"
            name="classCode"
            required
            class="w-full"
          >
            <UInput
              v-model="formState.classCode"
              class="w-full"
            />
          </UFormField>
          <UFormField
            label="Chức vụ Hiện tại"
            name="currentPosition"
            required
            class="w-full"
          >
            <UInput
              v-model="formState.currentPosition"
              class="w-full"
            />
          </UFormField>
        </div>
      </CommonPageSection>
      <CommonPageSection
        title="Địa chi & Tổ chức"
        title-icon="i-heroicons-map-pin-solid"
        inner-class="flex flex-col gap-8"
      >
        <div
          v-for="n in 2"
          :key="n"
          class="w-full"
        >
          <h3
            v-if="n === 1"
            class="mb-4 text-lg text-info font-bold"
          >
            Địa chỉ thường trú
          </h3>
          <div
            v-else
            class="flex mb-4 text-lg text-info font-bold"
          >
            <h3>Địa chỉ tạm trú</h3>
            <UButton
              label="Giống thường trú"
              variant="ghost"
              color="info"
              class="ml-auto cursor-pointer"
              @click="copyAddress"
            />
          </div>
          <div class="grid grid-cols-2 gap-x-8 gap-y-2">
            <UFormField
              label="Tỉnh / Thành phố"
              :name="n === 1 ? 'province' : 'provinceTemp'"
              required
              class="w-full"
            >
              <USelect
                v-model="formState[n === 1 ? 'province' : 'provinceTemp']"
                :items="provinces"
                value-key="label"
                class="w-full"
              />
            </UFormField>
            <UFormField
              label="Quận / Huyện"
              :name="n === 1 ? 'commune' : 'communeTemp'"
              required
              class="w-full"
            >
              <UInput
                v-model="formState[n === 1 ? 'commune' : 'communeTemp']"
                class="w-full"
              />
            </UFormField>
            <UFormField
              label="Địa chỉ cụ thể"
              :name="n === 1 ? 'specificAddress' : 'specificAddressTemp'"
              required
              class="w-full col-span-2"
            >
              <UTextarea
                v-model="formState[n === 1 ? 'specificAddress' : 'specificAddressTemp']"
                class="w-full"
                autoresize
              />
            </UFormField>
          </div>
        </div>
        {{ districts }}
      </CommonPageSection>

      <CommonPageSection
        title="Thông tin Liên hệ"
        title-icon="i-heroicons-phone-solid"
        inner-class="grid grid-cols-1 md:grid-cols-2 gap-8"
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
          label="Email"
          name="email"
          required
          class="w-full"
        >
          <UInput
            v-model="profile!.email"
            class="w-full"
            disabled
          />
        </UFormField>
      </CommonPageSection>
      <CommonPageSection
        title="Thông tin Liên quan"
        title-icon="i-heroicons-cube-solid"
        inner-class="grid grid-cols-2 gap-8"
      >
        <UFormField
          label="Chức vụ Đoàn hội"
          name="organPosition"
          class="w-full"
        >
          <UInput
            v-model="formState.organPosition"
            class="w-full"
          />
        </UFormField>
        <UFormField
          label="Đảng viên / Đoàn viên"
          name="ydMember"
          class="w-full"
        >
          <UInput
            v-model="formState.ydMember"
            class="w-full"
          />
        </UFormField>
      </CommonPageSection>
      <div class="flex justify-end gap-4">
        <UButton
          color="neutral"
          to="/dashboard/me"
          size="lg"
          label="Hủy"
        />
        <UButton
          type="submit"
          color="info"
          class="cursor-pointer"
          icon="i-heroicons-check"
          size="lg"
          label="Lưu thông tin"
          :loading="isLoading"
          :ui="{ label: ['mx-auto text-lg', isLoading && 'hidden'], leadingIcon: 'mx-auto' }"
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

const { profile, updateCurrentUser, isLoading } = await useCurrentUser()
const { provinces } = await useLocation()

const genderOptions = [
  {
    label: 'Nam',
    value: 'MALE',
  },
  {
    label: 'Nữ',
    value: 'FEMALE',
  },
  {
    label: 'Khác',
    value: 'OTHER',
  },
]

const districts = ref()

const currentDay = today(getLocalTimeZone())

const computedDob = computed({
  get: (): CalendarDate => {
    if (!formState.dob) return currentDay
    return parseDate(formState.dob)
  },
  set: (newDate: CalendarDate | undefined) => {
    if (!newDate) {
      formState.dob = ''
      return
    }
    formState.dob = newDate.toString()
  },
})

const formState = reactive({
  lastName: profile.value?.lastName ?? '',
  firstName: profile.value?.firstName ?? '',
  dob: profile.value?.dob ?? currentDay.toString(),
  gender: profile.value?.gender ?? 'MALE',
  ethnicity: profile.value?.ethnicity ?? '',
  idenNumber: profile.value?.idenNumber ?? '',
  university: profile.value?.university ?? 'Đại học Hà Nội',
  fieldOfStudy: profile.value?.fieldOfStudy ?? '',
  courseYear: profile.value?.courseYear ?? '',
  studentCode: profile.value?.studentCode ?? '',
  classCode: profile.value?.classCode ?? '',
  faculty: profile.value?.faculty ?? '',
  currentPosition: profile.value?.currentPosition ?? '',
  province: profile.value?.province ?? '',
  commune: profile.value?.commune ?? '',
  specificAddress: profile.value?.specificAddress ?? '',
  provinceTemp: profile.value?.provinceTemp ?? '',
  communeTemp: profile.value?.communeTemp ?? '',
  specificAddressTemp: profile.value?.specificAddressTemp ?? '',
  phoneNumber: profile.value?.phoneNumber ?? '',
  organPosition: profile.value?.organPosition ?? '',
  ydMember: profile.value?.ydMember ?? '',
})

const schema
  = z.object({
    lastName: z.string().min(1, 'Họ không được để trống!'),
    firstName: z.string().min(1, 'Tên không được để trống!'),
    dob: z.string().min(1, 'Vui lòng chọn ngày sinh!')
      .refine((dateString) => {
        try {
          const selectedDate = parseDate(dateString)
          if (selectedDate.compare(currentDay) <= 0) {
            return true
          }
          else {
            return false
          }
        }
        catch {
          return false
        }
      }, {
        message: 'Ngày sinh không hợp lệ hoặc ở tương lai!',
      }),
    gender: z.string().min(1, 'Giới tính không được để trống!'),
    ethnicity: z.string().min(1, 'Dân tộc không được để trống!'),
    idenNumber: z.string().min(1, 'Số CCCD/CMND không được để trống!').length(12, 'Số CCCD/CMND không hợp lệ! (Phải dài 12 số!)').regex(/^\d+$/, 'Số CCCD/CMND không hợp lệ!'),
    university: z.string().min(1, 'Tên trường không được để trống!'),
    fieldOfStudy: z.string().min(1, 'Tên ngành không được để trống!'),
    courseYear: z.string().min(1, 'Năm nhập học không được để trống!').startsWith('20', 'Năm nhập học không hợp lệ!').length(4, 'Năm nhập học không hợp lệ!').regex(/^\d+$/, 'Năm nhập học không hợp lệ!'),
    studentCode: z.string().min(1, 'Mã sinh viên không được để trống!'),
    classCode: z.string().min(1, 'Lớp hành chính không được để trống!'),
    faculty: z.string().min(1, 'Tên khoa không được để trống!'),
    currentPosition: z.string().min(1, 'Chức vụ hiện tại không được để trống!'),
    province: z.string().min(1, 'Vui lòng điền thông tin!'),
    commune: z.string().min(1, 'Vui lòng điền thông tin!'),
    specificAddress: z.string().min(1, 'Vui lòng điền thông tin!'),
    provinceTemp: z.string().min(1, 'Vui lòng điền thông tin!'),
    communeTemp: z.string().min(1, 'Vui lòng điền thông tin!'),
    specificAddressTemp: z.string().min(1, 'Vui lòng điền thông tin!'),
    phoneNumber: z.string().min(1, 'Số điện thoại không được để trống!').min(10, 'Số điện thoại không hợp lệ!').regex(/^\d+$/, 'Số điện thoại không hợp lệ!'),
  })

const copyAddress = () => {
  formState.provinceTemp = formState.province
  formState.communeTemp = formState.commune
  formState.specificAddressTemp = formState.specificAddress
}

const onSubmit = async () => {
  await updateCurrentUser({ ...formState })
}
</script>
