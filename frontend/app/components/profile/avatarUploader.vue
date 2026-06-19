<template>
  <div :class="['group relative inline-flex shrink-0', sizeClass]">
    <img
      :src="avatarSrc"
      alt="Ảnh đại diện"
      :class="['size-full', imageClass]"
    >

    <input
      ref="fileInput"
      class="sr-only"
      type="file"
      accept="image/png,image/jpeg,image/webp"
      @change="handleAvatarChange"
    >

    <button
      v-if="showHoverLabel"
      class="absolute inset-0 z-2 flex size-full cursor-pointer items-center justify-center rounded-full border-4 border-white bg-slate-950/60 p-0 text-center text-sm font-bold text-white opacity-0 transition-opacity hover:opacity-100 group-hover:opacity-100 disabled:cursor-wait"
      type="button"
      :disabled="avatarLoading"
      @click="openPicker"
    >
      {{ avatarLoading ? 'Đang tải...' : hoverLabel }}
    </button>

    <button
      :class="buttonClass"
      type="button"
      :disabled="avatarLoading"
      aria-label="Thay ảnh đại diện"
      @click="openPicker"
    >
      <UIcon
        :name="avatarLoading ? 'i-lucide-loader-circle' : 'i-lucide-camera'"
        :class="['size-5 text-white', avatarLoading && 'animate-spin']"
      />
    </button>
  </div>
</template>

<script setup lang="ts">
import { useCurrentUser } from '~/composables/profile/useCurrentUser'

const MAX_AVATAR_FILE_SIZE = 1024 * 1024
const ALLOWED_AVATAR_TYPES = ['image/png', 'image/jpeg', 'image/webp']

const {
  sizeClass = 'size-32',
  imageClass = 'rounded-full border-4 border-white object-cover shadow-[0_18px_45px_rgba(15,23,42,0.18)]',
  buttonClass = 'absolute bottom-2 right-2 z-3 flex size-10 items-center justify-center rounded-full border-2 border-white bg-[#3B82F6] text-white shadow-lg transition hover:bg-[#2563EB] disabled:cursor-wait disabled:opacity-80',
  showHoverLabel = false,
  hoverLabel = 'Thay ảnh',
} = defineProps<{
  sizeClass?: string
  imageClass?: string
  buttonClass?: string
  showHoverLabel?: boolean
  hoverLabel?: string
}>()

const toast = useToast()
const fileInput = ref<HTMLInputElement | null>(null)
const { profile, updateCurrentUserAvatar, avatarLoading } = await useCurrentUser()

const avatarSrc = computed(() => profile.value?.avatar || '/profilePlaceholder.png')

const openPicker = () => {
  if (avatarLoading.value) {
    return
  }

  fileInput.value?.click()
}

const handleAvatarChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]

  if (!file) {
    return
  }

  try {
    if (!ALLOWED_AVATAR_TYPES.includes(file.type)) {
      toast.add({
        title: 'Ảnh không hợp lệ',
        description: 'Vui lòng chọn ảnh PNG, JPG hoặc WebP.',
        color: 'error',
      })
      return
    }

    if (file.size > MAX_AVATAR_FILE_SIZE) {
      toast.add({
        title: 'Ảnh quá lớn',
        description: 'Vui lòng chọn ảnh nhỏ hơn 1MB.',
        color: 'error',
      })
      return
    }

    await updateCurrentUserAvatar(file)
  }
  catch {
    toast.add({
      title: 'Không thể đổi ảnh',
      description: 'Vui lòng thử lại với ảnh khác.',
      color: 'error',
    })
  }
  finally {
    target.value = ''
  }
}
</script>
