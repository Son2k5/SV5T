<template>
  <div class="h-full flex flex-col bg-white">
    <!-- No criteria selected -->
    <div
      v-if="!criteria || !standard"
      class="flex-1 flex flex-col items-center justify-center p-8 text-center text-[#64748B]"
    >
      <div class="size-16 rounded-full bg-slate-50 flex items-center justify-center text-slate-400 mb-4 border border-slate-100 shadow-sm">
        <UIcon
          name="i-lucide-file-text"
          class="size-8"
        />
      </div>
      <h4 class="text-base font-semibold text-[#1E293B] mb-1">
        Chưa chọn minh chứng
      </h4>
      <p class="text-sm max-w-sm">
        Chọn một tiêu chí có biểu tượng xem minh chứng ở danh mục bên trái để hiển thị chi tiết và xét duyệt.
      </p>
    </div>

    <!-- Criteria has no evidence -->
    <div
      v-else-if="!criteria.evidenceUrl"
      class="flex-1 flex flex-col items-center justify-center p-8 text-center text-[#64748B]"
    >
      <div class="size-16 rounded-full bg-amber-50/50 flex items-center justify-center text-amber-500 mb-4 border border-amber-100/50 shadow-sm">
        <UIcon
          name="i-lucide-alert-circle"
          class="size-8"
        />
      </div>
      <h4 class="text-base font-semibold text-[#1E293B] mb-1">
        Chưa nộp minh chứng
      </h4>
      <p class="text-sm max-w-sm">
        Tiêu chí <strong>{{ criteria.name }}</strong> này hiện tại sinh viên chưa tải lên tệp minh chứng nào.
      </p>
    </div>

    <!-- Active Evidence Review -->
    <div
      v-else
      class="flex-1 flex flex-col min-h-0 overflow-y-auto"
    >
      <!-- Section 1: Header / Metadata -->
      <div class="p-5 border-b border-slate-100 bg-slate-50/40">
        <div class="flex items-start justify-between gap-4">
          <div class="space-y-1">
            <span class="text-[10px] font-bold uppercase tracking-wider text-slate-400">Đang xem minh chứng</span>
            <h3 class="text-lg font-bold text-[#1E293B] leading-snug">
              {{ criteria.name }}
            </h3>
            <p class="text-xs text-[#64748B] flex items-center gap-1">
              <UIcon
                name="i-lucide-folder"
                class="size-3.5"
              />
              Thuộc: <span class="font-medium text-[#475569]">{{ standard.name }}</span>
            </p>
          </div>
          <div class="shrink-0">
            <AdminEvidenceStatusBadge
              :status="criteria.evidenceStatus"
              size="md"
              show-icon
            />
          </div>
        </div>

        <!-- Short Metadata List -->
        <div class="mt-4 grid grid-cols-2 gap-x-4 gap-y-2 text-xs border-t border-slate-100/80 pt-3">
          <div class="flex items-center gap-2">
            <span class="text-slate-400 font-medium w-24">Loại yêu cầu:</span>
            <span class="font-semibold text-slate-700 bg-slate-100 px-2 py-0.5 rounded">{{ criteria.evidenceType || 'N/A' }}</span>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-slate-400 font-medium w-24">Tính bắt buộc:</span>
            <UBadge
              :color="criteria.isMandatory ? 'success' : 'neutral'"
              variant="subtle"
              size="xs"
              class="rounded-full font-bold"
            >
              {{ criteria.isMandatory ? 'Bắt buộc' : 'Tùy chọn' }}
            </UBadge>
          </div>
        </div>
      </div>

      <!-- Section 2: Preview Content Area -->
      <div class="flex-1 p-5 min-h-[300px] flex flex-col">
        <h4 class="text-xs font-bold text-[#64748B] uppercase tracking-wider mb-2 flex items-center gap-1.5">
          <UIcon
            name="i-lucide-eye"
            class="size-4"
          />
          Nội dung minh chứng
        </h4>

        <div class="flex-1 border border-slate-200/80 rounded-2xl overflow-hidden bg-slate-900/[0.01] flex flex-col justify-center items-center p-4 relative min-h-[280px]">
          <!-- IMAGE Preview -->
          <div
            v-if="detectedType === 'IMAGE'"
            class="w-full h-full flex flex-col justify-center items-center py-2"
          >
            <div
              class="relative group cursor-zoom-in hover:opacity-95 transition-opacity"
              @click="isLightboxOpen = true"
            >
              <img
                :src="criteria.evidenceUrl"
                alt="Minh chứng ảnh"
                class="max-h-[320px] max-w-full object-contain rounded-xl shadow-md border border-slate-200/30 bg-white"
              >
              <div class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center rounded-xl">
                <span class="text-white text-xs font-bold flex items-center gap-1.5 bg-black/60 px-3 py-1.5 rounded-full">
                  <UIcon
                    name="i-lucide-maximize-2"
                    class="size-3.5"
                  />
                  Bấm để phóng to
                </span>
              </div>
            </div>
            <p class="text-[11px] text-[#64748B] mt-2 font-medium">
              Bấm vào ảnh để phóng to như Facebook
            </p>
          </div>

          <!-- PDF Preview (Open in new tab) -->
          <div
            v-else-if="detectedType === 'PDF'"
            class="py-8 px-4 text-center max-w-sm"
          >
            <div class="mx-auto flex size-14 items-center justify-center rounded-full bg-red-50 text-red-600 shadow-xs mb-4 border border-red-100">
              <UIcon
                name="i-lucide-file-text"
                class="size-7"
              />
            </div>
            <h5 class="text-sm font-bold text-[#1E293B] mb-1">
              Tệp tài liệu PDF
            </h5>
            <p class="text-xs text-[#64748B] mb-4">
              Minh chứng được lưu dưới dạng tệp tin tài liệu PDF.
            </p>
            <div class="bg-white border border-slate-200 rounded-xl p-2.5 mb-5 text-left break-all font-mono text-xs text-slate-600">
              {{ criteria.evidenceUrl.split('/').pop() || 'tai-lieu.pdf' }}
            </div>
            <div class="flex gap-3 justify-center">
              <UButton
                color="info"
                variant="outline"
                icon="i-lucide-external-link"
                label="Xem PDF (Tab mới)"
                class="rounded-xl shadow-xs font-semibold cursor-pointer text-xs"
                @click="openInNewTab"
              />
              <UButton
                color="info"
                variant="solid"
                icon="i-lucide-download"
                label="Tải file về máy"
                class="rounded-xl shadow-xs font-semibold cursor-pointer text-xs animate-pulse"
                @click="downloadFileDirectly"
              />
            </div>
          </div>

          <!-- LINK Preview -->
          <div
            v-else-if="detectedType === 'LINK'"
            class="py-8 px-4 text-center max-w-sm"
          >
            <div class="mx-auto flex size-14 items-center justify-center rounded-full bg-blue-50 text-[#2563EB] shadow-xs mb-4 border border-blue-100">
              <UIcon
                name="i-lucide-link"
                class="size-7"
              />
            </div>
            <h5 class="text-sm font-bold text-[#1E293B] mb-1">
              Đường dẫn liên kết
            </h5>
            <p class="text-xs text-[#64748B] mb-4">
              Minh chứng được sinh viên nộp dưới dạng liên kết ngoài.
            </p>
            <div class="bg-white border border-slate-200 rounded-xl p-2.5 mb-5 text-left break-all font-mono text-xs text-[#2563EB]">
              {{ criteria.evidenceUrl }}
            </div>
            <div class="flex gap-3 justify-center">
              <UButton
                color="info"
                variant="outline"
                icon="i-lucide-external-link"
                label="Mở liên kết"
                class="rounded-xl shadow-xs font-semibold cursor-pointer text-xs"
                @click="openInNewTab"
              />
              <UButton
                color="info"
                variant="solid"
                icon="i-lucide-download"
                label="Tải file về"
                class="rounded-xl shadow-xs font-semibold cursor-pointer text-xs"
                @click="downloadFileDirectly"
              />
            </div>
          </div>

          <!-- TEXT Preview -->
          <div
            v-else-if="detectedType === 'TEXT'"
            class="w-full h-[320px] overflow-y-auto bg-slate-950 text-slate-200 font-mono text-xs p-4 rounded-xl text-left whitespace-pre-wrap select-all"
          >
            {{ criteria.evidenceUrl }}
          </div>

          <!-- FILE/FALLBACK Download Preview -->
          <div
            v-else
            class="py-10 px-4 text-center max-w-sm"
          >
            <div class="mx-auto flex size-14 items-center justify-center rounded-full bg-slate-50 text-slate-600 shadow-xs mb-4 border border-slate-100">
              <UIcon
                name="i-lucide-file-archive"
                class="size-7"
              />
            </div>
            <h5 class="text-sm font-bold text-[#1E293B] mb-1">
              Tệp đính kèm khác
            </h5>
            <p class="text-xs text-[#64748B] mb-4">
              Vui lòng tải tệp tin này xuống máy tính hoặc mở trong tab mới để kiểm tra.
            </p>
            <div class="bg-white border border-slate-200 rounded-xl p-2.5 mb-5 text-left break-all font-mono text-xs text-slate-700">
              {{ criteria.evidenceUrl.split('/').pop() || 'tệp-minh-chứng' }}
            </div>
            <div class="flex gap-3 justify-center">
              <UButton
                color="info"
                variant="outline"
                icon="i-lucide-external-link"
                label="Mở tệp tin"
                class="rounded-xl shadow-xs font-semibold cursor-pointer text-xs"
                @click="openInNewTab"
              />
              <UButton
                color="info"
                variant="solid"
                icon="i-lucide-download"
                label="Tải file về máy"
                class="rounded-xl shadow-xs font-semibold cursor-pointer text-xs"
                @click="downloadFileDirectly"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- Section 3: Review Form & Actions -->
      <div class="p-5 border-t border-slate-100 bg-slate-50/50 space-y-4">
        <div>
          <h4 class="text-xs font-bold text-[#64748B] uppercase tracking-wider mb-2 flex items-center gap-1.5">
            <UIcon
              name="i-lucide-message-square"
              class="size-4 text-blue-600"
            />
            Đánh giá & Nhận xét của Admin
          </h4>
          <div
            v-if="readonly"
            class="bg-white border border-slate-200 rounded-xl p-3.5 text-sm text-[#475569] min-h-[60px]"
          >
            {{ criteria.reviewerComment || 'Chưa có nhận xét nào từ giám khảo.' }}
          </div>
          <UTextarea
            v-else
            v-model="comment"
            placeholder="Nhập nhận xét để sinh viên có thể đọc..."
            class="w-full bg-white text-sm"
            :rows="3"
          />
        </div>

        <div
          v-if="!readonly"
          class="flex items-center justify-between gap-3 pt-2"
        >
          <!-- Left: Save Comment -->
          <UButton
            color="neutral"
            variant="outline"
            icon="i-lucide-save"
            label="Lưu nhận xét"
            :loading="loading"
            class="rounded-xl font-medium cursor-pointer"
            @click="handleSaveComment"
          />

          <!-- Right: Evaluate Actions -->
          <div class="flex items-center gap-2">
            <UButton
              color="error"
              variant="solid"
              icon="i-lucide-x-circle"
              label="Từ chối"
              :loading="loading"
              class="rounded-xl font-semibold px-4 cursor-pointer"
              @click="handleReject"
            />
            <UButton
              color="success"
              variant="solid"
              icon="i-lucide-check-circle-2"
              label="Chấp thuận"
              :loading="loading"
              class="rounded-xl font-semibold px-4 cursor-pointer"
              @click="handleApprove"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- Image Lightbox Overlay (Facebook style) -->
    <Teleport to="body">
      <Transition
        enter-active-class="transition-opacity duration-200 ease-out"
        leave-active-class="transition-opacity duration-150 ease-in"
        enter-from-class="opacity-0"
        leave-to-class="opacity-0"
      >
        <div
          v-if="isLightboxOpen && criteria?.evidenceUrl"
          class="fixed inset-0 z-[9999] flex flex-col bg-black/95 select-none"
          @click.self="closeLightbox"
        >
          <!-- Lightbox Top bar -->
          <div
            class="h-14 w-full flex items-center justify-between px-5 text-white shrink-0 bg-gradient-to-b from-black/40 to-transparent relative z-10"
            @click.stop
          >
            <div class="text-xs font-semibold flex items-center gap-2">
              <UIcon
                name="i-lucide-image"
                class="size-4.5 text-blue-400"
              />
              <span>{{ criteria.name }}</span>
            </div>

            <!-- Zoom and Close Actions -->
            <div
              class="relative z-20 flex items-center gap-2 pointer-events-auto"
              @click.stop
              @pointerdown.stop
            >
              <button
                type="button"
                class="flex size-9 items-center justify-center rounded-full text-white transition hover:bg-white/10 focus:outline-none focus:ring-2 focus:ring-white/60 disabled:cursor-not-allowed disabled:opacity-40"
                title="Thu nhỏ"
                :disabled="zoomScale <= MIN_ZOOM"
                @click.stop.prevent="zoomOut"
              >
                <UIcon
                  name="i-lucide-zoom-out"
                  class="size-5"
                />
              </button>
              <span class="text-xs font-mono min-w-10 text-center text-slate-300 select-none">
                {{ Math.round(zoomScale * 100) }}%
              </span>
              <button
                type="button"
                class="flex size-9 items-center justify-center rounded-full text-white transition hover:bg-white/10 focus:outline-none focus:ring-2 focus:ring-white/60 disabled:cursor-not-allowed disabled:opacity-40"
                title="Phóng to"
                :disabled="zoomScale >= MAX_ZOOM"
                @click.stop.prevent="zoomIn"
              >
                <UIcon
                  name="i-lucide-zoom-in"
                  class="size-5"
                />
              </button>
              <button
                type="button"
                class="flex size-9 items-center justify-center rounded-full text-white transition hover:bg-white/10 focus:outline-none focus:ring-2 focus:ring-white/60"
                title="Đặt lại"
                @click.stop.prevent="resetZoom"
              >
                <UIcon
                  name="i-lucide-rotate-ccw"
                  class="size-5"
                />
              </button>
              <button
                type="button"
                class="mr-2 flex size-9 items-center justify-center rounded-full text-white transition hover:bg-white/10 focus:outline-none focus:ring-2 focus:ring-white/60"
                title="Tải ảnh về máy"
                @click.stop.prevent="downloadImage"
              >
                <UIcon
                  name="i-lucide-download"
                  class="size-5"
                />
              </button>

              <div class="h-6 w-px bg-white/20 mr-1" />

              <button
                type="button"
                class="flex size-9 items-center justify-center rounded-full text-white transition hover:bg-white/10 focus:outline-none focus:ring-2 focus:ring-white/60"
                aria-label="Đóng ảnh"
                title="Đóng ảnh"
                @click.stop.prevent="closeLightbox"
              >
                <UIcon
                  name="i-lucide-x"
                  class="size-5"
                />
              </button>
            </div>
          </div>

          <!-- Lightbox Main Image (Scrollable container supporting zoom) -->
          <div
            class="flex-1 overflow-hidden flex items-center justify-center p-6 min-h-0 bg-slate-950/20 touch-none"
            :class="zoomScale > 1 ? (isPanningLightbox ? 'cursor-grabbing' : 'cursor-grab') : 'cursor-zoom-in'"
            @click.self="handleLightboxViewportClick"
          >
            <div
              class="relative flex shrink-0 items-center justify-center"
              :class="isPanningLightbox ? 'transition-none' : 'transition-transform duration-150 ease-out'"
              :style="{
                transform: `translate3d(${panOffset.x}px, ${panOffset.y}px, 0) scale(${zoomScale})`,
                transformOrigin: 'center center',
              }"
              @click.stop.prevent="handleLightboxImageClick"
              @mousedown.stop.prevent="startLightboxPan"
            >
              <img
                :src="criteria.evidenceUrl"
                alt="Ảnh phóng to"
                draggable="false"
                class="pointer-events-none max-h-[80vh] max-w-[85vw] object-contain shadow-2xl rounded select-none"
                @dragstart.prevent
              >
            </div>
          </div>

          <!-- Lightbox Footer -->
          <div
            class="h-16 w-full flex items-center justify-center px-4 shrink-0 bg-gradient-to-t from-black/40 to-transparent"
            @click.stop
          >
            <p class="text-xs text-slate-300 max-w-md text-center truncate select-text">
              {{ criteria.evidenceOriginalFilename || 'Ảnh minh chứng' }}
            </p>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onBeforeUnmount, watch } from 'vue'
import type { CriteriaResultDTO, StandardResultDTO } from '~/types/admin'

const props = withDefaults(
  defineProps<{
    criteria: CriteriaResultDTO | null
    standard: StandardResultDTO | null
    loading?: boolean
    readonly?: boolean
  }>(),
  {
    readonly: false,
  },
)

const emit = defineEmits<{
  'approve': [data: { criteria: CriteriaResultDTO, comment: string }]
  'reject': [data: { criteria: CriteriaResultDTO, reason: string }]
  'save-comment': [data: { criteria: CriteriaResultDTO, comment: string }]
}>()

const toast = useToast()
const comment = ref('')
const isLightboxOpen = ref(false)
const zoomScale = ref(1)
const panOffset = ref({ x: 0, y: 0 })
const isPanningLightbox = ref(false)
const didPanLightbox = ref(false)
const MIN_ZOOM = 0.5
const MAX_ZOOM = 4
const ZOOM_STEP = 0.25
let panStartX = 0
let panStartY = 0
let panStartOffsetX = 0
let panStartOffsetY = 0

// Close lightbox & reset zoom if criteria changes
watch(
  () => props.criteria?.publicId,
  () => {
    comment.value = props.criteria?.reviewerComment || ''
    stopLightboxPan()
    isLightboxOpen.value = false
    zoomScale.value = 1
    resetPanOffset()
  },
  { immediate: true },
)

// Watch lightbox open state to reset zoom to 1
watch(isLightboxOpen, (isOpen) => {
  if (!isOpen) {
    zoomScale.value = 1
    resetPanOffset()
  }
})

function zoomIn() {
  if (zoomScale.value < MAX_ZOOM) {
    zoomScale.value = Math.min(MAX_ZOOM, zoomScale.value + ZOOM_STEP)
  }
}

function zoomOut() {
  if (zoomScale.value > MIN_ZOOM) {
    zoomScale.value = Math.max(MIN_ZOOM, zoomScale.value - ZOOM_STEP)
    if (zoomScale.value <= 1) {
      resetPanOffset()
    }
  }
}

function resetZoom() {
  zoomScale.value = 1
  resetPanOffset()
}

function closeLightbox() {
  stopLightboxPan()
  isLightboxOpen.value = false
  zoomScale.value = 1
  didPanLightbox.value = false
  resetPanOffset()
}

function resetPanOffset() {
  panOffset.value = { x: 0, y: 0 }
}

function startLightboxPan(event: MouseEvent) {
  if (zoomScale.value <= 1) return

  isPanningLightbox.value = true
  didPanLightbox.value = false
  panStartX = event.clientX
  panStartY = event.clientY
  panStartOffsetX = panOffset.value.x
  panStartOffsetY = panOffset.value.y

  if (typeof window !== 'undefined') {
    window.addEventListener('mousemove', moveLightboxPan)
    window.addEventListener('mouseup', endLightboxPan)
  }
}

function moveLightboxPan(event: MouseEvent) {
  if (!isPanningLightbox.value) return

  const deltaX = event.clientX - panStartX
  const deltaY = event.clientY - panStartY

  if (Math.abs(deltaX) > 3 || Math.abs(deltaY) > 3) {
    didPanLightbox.value = true
  }

  panOffset.value = {
    x: panStartOffsetX + deltaX,
    y: panStartOffsetY + deltaY,
  }

  event.preventDefault()
}

function endLightboxPan() {
  if (!isPanningLightbox.value) return

  stopLightboxPan()
}

function stopLightboxPan() {
  isPanningLightbox.value = false
  if (typeof window !== 'undefined') {
    window.removeEventListener('mousemove', moveLightboxPan)
    window.removeEventListener('mouseup', endLightboxPan)
  }
}

function handleLightboxViewportClick() {
  if (didPanLightbox.value) {
    didPanLightbox.value = false
    return
  }

  closeLightbox()
}

function handleLightboxImageClick() {
  if (didPanLightbox.value) {
    didPanLightbox.value = false
  }
}

onBeforeUnmount(stopLightboxPan)

function downloadImage() {
  if (props.criteria?.evidenceUrl) {
    window.open(props.criteria.evidenceUrl, '_blank')
  }
}

function downloadFileDirectly() {
  if (!props.criteria?.evidenceUrl) return
  const link = document.createElement('a')
  link.href = props.criteria.evidenceUrl
  link.setAttribute('download', props.criteria.evidenceOriginalFilename || 'minh-chung')
  link.setAttribute('target', '_blank')
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const detectedType = computed(() => {
  if (!props.criteria || !props.criteria.evidenceUrl) return 'NONE'
  const url = props.criteria.evidenceUrl.toLowerCase().trim()
  const type = props.criteria.evidenceType?.toUpperCase()

  if (type === 'IMAGE') return 'IMAGE'
  if (type === 'PDF') return 'PDF'
  if (type === 'LINK') return 'LINK'

  if (url.match(/\.(jpeg|jpg|gif|png|webp|svg)/)) return 'IMAGE'
  if (url.endsWith('.pdf')) return 'PDF'
  if (url.startsWith('http://') || url.startsWith('https://')) return 'LINK'
  if (url.length > 200) return 'TEXT'

  return 'FILE'
})

function openInNewTab() {
  if (props.criteria?.evidenceUrl) {
    window.open(props.criteria.evidenceUrl, '_blank')
  }
}

function handleSaveComment() {
  if (!props.criteria) return
  emit('save-comment', { criteria: props.criteria, comment: comment.value.trim() })
}

function handleApprove() {
  if (!props.criteria) return
  emit('approve', { criteria: props.criteria, comment: comment.value.trim() })
}

function handleReject() {
  if (!props.criteria) return
  const cleanComment = comment.value.trim()
  if (!cleanComment) {
    toast.add({
      title: 'Yêu cầu điền nhận xét',
      description: 'Khi từ chối minh chứng, bạn bắt buộc phải nhập lý do từ chối vào ô nhận xét.',
      color: 'error',
    })
    return
  }
  emit('reject', { criteria: props.criteria, reason: cleanComment })
}
</script>
