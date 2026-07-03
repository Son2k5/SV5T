<template>
  <div class="space-y-4">
    <!-- Drag and drop zone -->
    <div
      v-if="!disabled"
      :class="[
        'relative flex flex-col items-center justify-center border-2 border-dashed rounded-2xl p-6 text-center cursor-pointer transition-all duration-300',
        isDragging ? 'border-[#075EA8] bg-blue-50/30' : 'border-slate-200 hover:border-[#075EA8]/50 hover:bg-slate-50/50',
      ]"
      @dragover.prevent="onDragOver"
      @dragleave="onDragLeave"
      @drop.prevent="onDrop"
      @click="triggerFileInput"
    >
      <input
        ref="fileInput"
        type="file"
        multiple
        class="hidden"
        accept="image/png, image/jpeg, image/jpg, application/pdf, .doc, .docx"
        @change="onFileChange"
      >

      <div class="flex size-14 items-center justify-center rounded-full bg-slate-50 text-slate-500 shadow-sm border border-slate-100/50 mb-3 group-hover:scale-110 transition-transform">
        <UIcon
          name="i-heroicons-cloud-arrow-up"
          class="size-7 text-[#075EA8]"
        />
      </div>

      <p class="text-sm font-bold text-slate-800">
        Kéo thả tài liệu vào đây hoặc <span class="text-[#075EA8] underline">chọn tệp</span>
      </p>
      <p class="mt-1 text-xs text-slate-400">
        Hỗ trợ định dạng: PDF, PNG, JPG, DOCX (Tối đa 10 file, 20MB/file)
      </p>
    </div>

    <!-- Error message display -->
    <p
      v-if="error"
      class="text-xs font-semibold text-red-500 bg-red-50 p-2 rounded-lg border border-red-100 flex items-center gap-1.5"
    >
      <UIcon
        name="i-heroicons-exclamation-triangle"
        class="size-4 shrink-0"
      />
      {{ error }}
    </p>

    <!-- File Preview list -->
    <div
      v-if="files && files.length > 0"
      class="space-y-2"
    >
      <h4 class="text-xs font-bold text-slate-500 uppercase tracking-wider">
        Danh sách minh chứng ({{ files.length }} tệp)
      </h4>

      <div
        v-for="(file, idx) in files"
        :key="idx"
        class="flex items-center justify-between p-3 rounded-xl border border-slate-100 bg-slate-50/40 hover:bg-slate-50 transition-colors"
      >
        <div class="flex items-center gap-3 min-w-0">
          <!-- File type Icon -->
          <div :class="['flex size-10 shrink-0 items-center justify-center rounded-lg text-white font-black text-xs shadow-sm', getFileIconClass(file.name)]">
            <UIcon
              :name="getFileIcon(file.name)"
              class="size-5"
            />
          </div>

          <div class="min-w-0">
            <p class="text-sm font-bold text-slate-800 truncate pr-2">
              {{ file.name }}
            </p>
            <p class="text-xs text-slate-400 font-semibold mt-0.5">
              {{ formatSize(file.size) }} · {{ file.uploadDate }}
            </p>
          </div>
        </div>

        <!-- File actions -->
        <div class="flex items-center gap-1.5 shrink-0">
          <UButton
            color="neutral"
            variant="ghost"
            icon="i-heroicons-eye"
            size="xs"
            class="cursor-pointer hover:bg-slate-100 rounded-lg text-slate-500"
            @click="viewFile(file.url)"
          />
          <UButton
            v-if="!disabled"
            color="error"
            variant="ghost"
            icon="i-heroicons-trash"
            size="xs"
            class="cursor-pointer hover:bg-red-50 rounded-lg text-red-500"
            @click="removeFile(idx)"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{
  files: Array<{
    name: string
    size: number
    uploadDate: string
    url: string
    rawFile?: File
  }>
  disabled?: boolean
}>()

const emit = defineEmits<{
  'update:files': [files: Array<{ name: string, size: number, uploadDate: string, url: string, rawFile?: File }>]
}>()

const fileInput = ref<HTMLInputElement | null>(null)
const isDragging = ref(false)
const error = ref('')

const onDragOver = () => {
  if (props.disabled) return
  isDragging.value = true
}

const onDragLeave = () => {
  isDragging.value = false
}

const triggerFileInput = () => {
  if (props.disabled) return
  fileInput.value?.click()
}

const validateAndProcessFiles = (fileList: FileList) => {
  error.value = ''
  const currentFiles = [...props.files]

  if (currentFiles.length + fileList.length > 10) {
    error.value = 'Vượt quá giới hạn tối đa 10 tệp tin cho mỗi tiêu chí.'
    return
  }

  const allowedTypes = [
    'image/png', 'image/jpeg', 'image/jpg', 'application/pdf',
    'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  ]

  const allowedExtensions = ['.png', '.jpg', '.jpeg', '.pdf', '.doc', '.docx']

  for (let i = 0; i < fileList.length; i++) {
    const file = fileList[i]
    if (!file) continue

    // Check size (20MB = 20 * 1024 * 1024)
    if (file.size > 20 * 1024 * 1024) {
      error.value = `Tệp "${file.name}" vượt quá dung lượng tối đa cho phép (20MB).`
      return
    }

    // Check extension
    const ext = file.name.substring(file.name.lastIndexOf('.')).toLowerCase()
    if (!allowedTypes.includes(file.type) && !allowedExtensions.includes(ext)) {
      error.value = `Định dạng tệp "${file.name}" không hợp lệ. Vui lòng chọn PDF, PNG, JPG, DOCX.`
      return
    }

    // Add raw file reference
    currentFiles.push({
      name: file.name,
      size: file.size,
      uploadDate: new Date().toLocaleDateString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      }),
      url: URL.createObjectURL(file),
      rawFile: file,
    })
  }

  emit('update:files', currentFiles)
}

const onFileChange = (e: Event) => {
  const target = e.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    validateAndProcessFiles(target.files)
  }
}

const onDrop = (e: DragEvent) => {
  if (props.disabled) return
  isDragging.value = false
  if (e.dataTransfer?.files && e.dataTransfer.files.length > 0) {
    validateAndProcessFiles(e.dataTransfer.files)
  }
}

const removeFile = (index: number) => {
  const currentFiles = [...props.files]
  currentFiles.splice(index, 1)
  emit('update:files', currentFiles)
}

const viewFile = (url: string) => {
  window.open(url, '_blank')
}

// Icon Helper based on file extension
const getFileIcon = (filename: string) => {
  const ext = filename.split('.').pop()?.toLowerCase()
  if (ext === 'pdf') return 'i-heroicons-document-text'
  if (['png', 'jpg', 'jpeg'].includes(ext || '')) return 'i-heroicons-photo'
  if (['doc', 'docx'].includes(ext || '')) return 'i-heroicons-document-text'
  return 'i-heroicons-document'
}

const getFileIconClass = (filename: string) => {
  const ext = filename.split('.').pop()?.toLowerCase()
  if (ext === 'pdf') return 'bg-rose-500'
  if (['png', 'jpg', 'jpeg'].includes(ext || '')) return 'bg-emerald-500'
  if (['doc', 'docx'].includes(ext || '')) return 'bg-blue-500'
  return 'bg-slate-500'
}

// Format byte size
const formatSize = (bytes: number) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}
</script>
