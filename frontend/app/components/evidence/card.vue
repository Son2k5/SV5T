<script setup lang="ts">
import type { CriteriaDTO } from '~/types/applicationRecord'

const props = defineProps<{
  criteria: CriteriaDTO
  disabled?: boolean
  loading?: boolean
}>()

const emit = defineEmits<{
  save: [criteriaId: string, evidenceUrl: string]
  saveFile: [criteriaId: string, file: File]
}>()

const isOpen = ref(!props.criteria.evidenceUrl)
const evidenceUrl = ref(props.criteria.evidenceUrl ?? '')
const selectedFile = ref<File | null>(null)
const localError = ref('')

const hasChildren = computed(() => Boolean(props.criteria.subCriteriaList?.length))
const hasEvidence = computed(() => Boolean(props.criteria.evidenceUrl))

watch(
  () => props.criteria.evidenceUrl,
  (value) => {
    evidenceUrl.value = value ?? ''
  },
)

const saveLink = () => {
  localError.value = ''
  const value = evidenceUrl.value.trim()

  if (!value) {
    localError.value = 'Vui lòng nhập link minh chứng.'
    return
  }

  emit('save', props.criteria.publicId, value)
}

const saveFile = () => {
  localError.value = ''

  if (!selectedFile.value) {
    localError.value = 'Vui lòng chọn file minh chứng.'
    return
  }

  emit('saveFile', props.criteria.publicId, selectedFile.value)
}

const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  selectedFile.value = target.files?.[0] ?? null
}
</script>

<template>
  <div class="rounded-lg border border-[#E5E7EB] bg-white shadow-sm">
    <button
      type="button"
      class="flex w-full flex-col gap-4 p-5 text-left transition hover:bg-slate-50 md:flex-row md:items-start md:justify-between"
      @click="isOpen = !isOpen"
    >
      <div class="min-w-0">
        <div class="flex flex-wrap items-center gap-2">
          <h3 class="text-lg font-black text-[#1E293B]">
            {{ criteria.name }}
          </h3>
          <span
            v-if="criteria.isMandatory"
            class="rounded-full bg-red-50 px-3 py-1 text-xs font-black text-red-600"
          >
            Bắt buộc
          </span>
        </div>

        <p
          v-if="criteria.description"
          class="mt-2 text-sm leading-6 text-[#64748B]"
        >
          {{ criteria.description }}
        </p>
      </div>

      <div class="flex shrink-0 items-center gap-3">
        <span
          :class="[
            'rounded-full px-3 py-1 text-xs font-black',
            hasEvidence ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-700',
          ]"
        >
          {{ hasEvidence ? 'Đã có minh chứng' : 'Chưa có minh chứng' }}
        </span>
        <UIcon
          name="i-lucide-chevron-down"
          :class="['size-5 text-[#64748B] transition', isOpen && 'rotate-180']"
        />
      </div>
    </button>

    <div
      v-if="isOpen"
      class="border-t border-[#E5E7EB] p-5"
    >
      <div
        v-if="criteria.evidenceUrl"
        class="mb-4 rounded-md bg-blue-50 p-3 text-sm text-blue-700"
      >
        Minh chứng hiện tại:
        <a
          :href="criteria.evidenceUrl"
          target="_blank"
          class="font-bold underline"
        >
          Xem file/link
        </a>
      </div>

      <div
        v-if="!disabled"
        class="grid gap-4 lg:grid-cols-[minmax(0,1fr)_minmax(0,1fr)]"
      >
        <div class="rounded-md border border-[#E5E7EB] p-4">
          <label class="block text-sm font-bold text-[#1E293B]">
            Link minh chứng
          </label>
          <input
            v-model="evidenceUrl"
            type="url"
            class="mt-2 h-11 w-full rounded-md border border-[#CBD5E1] px-3 text-sm outline-none transition focus:border-blue-500"
            placeholder="https://..."
          >
          <button
            type="button"
            :disabled="loading"
            class="mt-3 inline-flex h-10 items-center justify-center rounded-md bg-[#075EA8] px-4 text-sm font-black text-white transition hover:bg-[#064f8d] disabled:cursor-not-allowed disabled:opacity-60"
            @click="saveLink"
          >
            Lưu link
          </button>
        </div>

        <div class="rounded-md border border-[#E5E7EB] p-4">
          <label class="block text-sm font-bold text-[#1E293B]">
            Upload file minh chứng
          </label>
          <input
            type="file"
            accept="image/png,image/jpeg,image/webp,application/pdf,.doc,.docx"
            class="mt-2 block w-full text-sm text-[#64748B] file:mr-4 file:h-10 file:rounded-md file:border-0 file:bg-blue-50 file:px-4 file:text-sm file:font-black file:text-blue-700"
            @change="handleFileChange"
          >
          <button
            type="button"
            :disabled="loading || !selectedFile"
            class="mt-3 inline-flex h-10 items-center justify-center rounded-md bg-[#16A34A] px-4 text-sm font-black text-white transition hover:bg-[#15803D] disabled:cursor-not-allowed disabled:opacity-60"
            @click="saveFile"
          >
            Upload minh chứng
          </button>
        </div>
      </div>

      <p
        v-if="localError"
        class="mt-3 rounded-md bg-red-50 p-3 text-sm font-semibold text-red-600"
      >
        {{ localError }}
      </p>

      <div
        v-if="hasChildren"
        class="mt-5 space-y-3 border-l-2 border-blue-100 pl-4"
      >
        <EvidenceCard
          v-for="child in criteria.subCriteriaList"
          :key="child.publicId"
          :criteria="child"
          :disabled="disabled"
          :loading="loading"
          @save="(criteriaId, value) => emit('save', criteriaId, value)"
          @save-file="(criteriaId, file) => emit('saveFile', criteriaId, file)"
        />
      </div>
    </div>
  </div>
</template>
