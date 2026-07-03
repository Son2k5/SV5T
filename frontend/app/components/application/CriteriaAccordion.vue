<template>
  <div class="space-y-8">
    <div
      v-for="standard in standards"
      :key="standard.publicId"
      class="sv-card p-6 bg-white border border-slate-100 shadow-sm rounded-2xl"
    >
      <!-- Standard Header exactly like mockup 2 -->
      <div class="flex items-center justify-between gap-4 border-b border-slate-100 pb-4 mb-6">
        <div class="flex items-center gap-3">
          <!-- Heart Icon badge in light indigo circle -->
          <div class="flex size-11 items-center justify-center rounded-xl bg-indigo-50 text-indigo-500 border border-indigo-100/50 shadow-sm">
            <UIcon
              name="i-heroicons-heart"
              class="size-6"
            />
          </div>
          <div>
            <h3 class="text-lg font-black uppercase text-slate-800 tracking-wide leading-tight">
              {{ standard.name }}
            </h3>
            <p class="text-xs text-blue-500 font-bold mt-1">
              {{ levelLabel }} — Đính kèm minh chứng để xét duyệt
            </p>
          </div>
        </div>
        <UButton
          size="xs"
          variant="outline"
          label="Nội dung minh chứng"
          class="cursor-pointer font-bold px-3 rounded-lg border border-slate-200 text-slate-600 bg-slate-50/50 hover:bg-slate-50 transition-all text-xs"
        />
      </div>

      <!-- Partition: ĐIỀU KIỆN BẮT BUỘC -->
      <div
        v-if="getMandatoryCriteria(standard).length > 0"
        class="space-y-4 mb-6"
      >
        <div class="flex items-center justify-between border-l-[6px] border-blue-600 pl-3 py-1.5 bg-blue-50/50 rounded-r-xl pr-3">
          <div class="flex items-center gap-2.5">
            <h4 class="text-sm md:text-base font-black text-blue-700 uppercase tracking-wide">
              ĐIỀU KIỆN BẮT BUỘC
            </h4>
            <span class="text-xs text-slate-500 font-black uppercase bg-blue-100/60 text-[10px] px-2 py-0.5 rounded shadow-sm">Bạn phải hoàn thành TẤT CẢ mục này</span>
          </div>
          <UButton
            size="xs"
            variant="ghost"
            icon="i-heroicons-question-mark-circle"
            label="Hướng dẫn nộp"
            class="text-[#075EA8] hover:bg-blue-50 font-bold text-xs p-1"
          />
        </div>

        <div class="space-y-4">
          <div
            v-for="(criteria, index) in getMandatoryCriteria(standard)"
            :key="criteria.publicId"
            class="border border-slate-100 rounded-2xl overflow-hidden bg-white shadow-sm transition-all duration-300"
            :class="openCriteriaId === criteria.publicId && 'border-blue-200 ring-2 ring-blue-50'"
          >
            <!-- Criteria Card Header -->
            <div class="p-5">
              <div class="flex items-start gap-4">
                <!-- Index circle -->
                <div class="size-8 rounded-full bg-slate-100 flex items-center justify-center font-extrabold text-slate-700 text-sm shrink-0 shadow-inner">
                  {{ index + 1 }}
                </div>

                <!-- Shield Checkmark Icon -->
                <div class="size-7 rounded-lg bg-blue-50 text-blue-600 border border-blue-100/50 flex items-center justify-center shrink-0 mt-0.5">
                  <UIcon
                    name="i-heroicons-shield-check"
                    class="size-4.5"
                  />
                </div>

                <!-- Criteria Title -->
                <div class="flex-1 min-w-0">
                  <h5 class="text-sm font-extrabold text-slate-800 leading-snug">
                    {{ criteria.name }}
                  </h5>
                  <p class="mt-1 text-xs text-slate-400 leading-normal font-semibold">
                    {{ criteria.description || 'Chi tiết tiêu chí bắt buộc để hoàn thành tiêu chuẩn.' }}
                  </p>
                </div>
              </div>

              <!-- MENTOR BOX RIGHT INSIDE CARD HEADER (Mockup style) -->
              <div class="mt-4 bg-slate-50/70 border border-slate-100 rounded-xl p-4">
                <div class="flex items-center gap-2 mb-2">
                  <!-- Red Avatar with 'M' -->
                  <div class="size-6 rounded-full bg-rose-500 text-white flex items-center justify-center font-extrabold text-[10px] shadow-sm">
                    M
                  </div>
                  <span class="text-xs font-black text-slate-700">Mentor</span>
                  <!-- Blue check tag -->
                  <span class="inline-flex items-center gap-0.5 bg-blue-50 text-blue-600 border border-blue-100 rounded px-1.5 py-0.5 text-[9px] font-black uppercase">
                    <UIcon
                      name="i-heroicons-check-badge"
                      class="size-3"
                    />
                    Nhận xét
                  </span>
                </div>
                <p class="text-xs font-semibold text-slate-500 leading-relaxed">
                  {{ criteria.reviewerComment || 'Chưa có nhận xét' }}
                </p>
              </div>

              <!-- Footer of Card -->
              <div class="mt-4 pt-4 border-t border-slate-100/60 flex items-center justify-between gap-4">
                <div class="flex items-center gap-2">
                  <span class="text-[10px] font-black text-slate-400 uppercase tracking-wider">TRẠNG THÁI:</span>
                  <span
                    :class="[
                      'text-[10px] font-extrabold px-2 py-0.5 rounded border shadow-sm uppercase tracking-wider',
                      isCriteriaSubmitted(criteria)
                        ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                        : 'bg-orange-50 text-orange-600 border-orange-200',
                    ]"
                  >
                    {{ isCriteriaSubmitted(criteria) ? 'Đã nộp' : 'Chưa nộp' }}
                  </span>
                </div>

                <UButton
                  size="sm"
                  :label="openCriteriaId === criteria.publicId ? 'Thu gọn' : (isCriteriaSubmitted(criteria) ? 'Chỉnh sửa' : '+ Thêm')"
                  :class="[
                    'cursor-pointer font-bold px-4 rounded-lg text-xs shadow-sm transition-all border-0',
                    openCriteriaId === criteria.publicId
                      ? 'bg-slate-600 hover:bg-slate-700 text-white'
                      : (isCriteriaSubmitted(criteria)
                        ? 'bg-amber-600 hover:bg-amber-700 text-white'
                        : 'bg-[#075EA8] hover:bg-[#064f8d] text-white'),
                  ]"
                  @click="toggleCriteria(criteria.publicId)"
                />
              </div>
            </div>

            <!-- Criteria Card Body Form & Upload (Only visible when expanded) -->
            <div
              v-if="openCriteriaId === criteria.publicId"
              class="border-t border-slate-100 bg-slate-50/20 p-5 space-y-5"
            >
              <!-- Info Box -->
              <div
                v-if="requiresEvidence(criteria)"
                class="bg-blue-50/40 rounded-xl p-3 border border-blue-100/30"
              >
                <p class="text-xs text-slate-500 leading-relaxed font-semibold">
                  Tải lên tệp minh chứng định dạng PDF, JPG, PNG hoặc DOCX để được xét duyệt. Khai báo trung thực các nội dung dưới đây.
                </p>
              </div>

              <!-- Form fields -->
              <div class="space-y-4 bg-white p-4 rounded-xl border border-slate-100 shadow-inner">
                <UFormGroup
                  label="Giải trình cho Mentor"
                  name="additionalExplanation"
                  required
                >
                  <UTextarea
                    v-model="formStates[criteria.publicId]!.additionalExplanation"
                    placeholder="Giải trình thêm cho Mentor nếu cần..."
                    :rows="3"
                    size="sm"
                    :disabled="disabled"
                  />
                </UFormGroup>
              </div>

              <!-- Upload space -->
              <EvidenceUploader
                v-if="requiresEvidence(criteria)"
                v-model:files="formStates[criteria.publicId]!.files"
                :disabled="disabled"
              />

              <!-- Card Action Save -->
              <div
                v-if="!disabled"
                class="flex justify-end pt-3 border-t border-slate-100/50"
              >
                <UButton
                  label="Lưu tiêu chí"
                  icon="i-heroicons-check-circle"
                  :loading="savingId === criteria.publicId"
                  class="cursor-pointer font-bold px-4 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white text-xs shadow-sm"
                  @click="saveCriteriaData(criteria.publicId)"
                />
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Partition: TIÊU CHÍ LỰA CHỌN -->
      <div
        v-if="getSelectiveCriteria(standard).length > 0"
        class="space-y-4"
      >
        <div class="flex items-center justify-between border-l-[6px] border-purple-600 pl-3 py-1.5 bg-purple-50/50 rounded-r-xl pr-3">
          <div class="flex items-center gap-2.5">
            <h4 class="text-sm md:text-base font-black text-purple-700 uppercase tracking-wide">
              TIÊU CHÍ TỰ CHỌN
            </h4>
            <span class="text-xs text-slate-500 font-black uppercase bg-purple-100/60 text-[10px] px-2 py-0.5 rounded shadow-sm">Đạt ít nhất 01 tiêu chí trong danh sách</span>
          </div>
          <UButton
            size="xs"
            variant="ghost"
            icon="i-heroicons-question-mark-circle"
            label="Hướng dẫn nộp"
            class="text-[#075EA8] hover:bg-blue-50 font-bold text-xs p-1"
          />
        </div>

        <div class="space-y-4">
          <div
            v-for="(criteria, index) in getSelectiveCriteria(standard)"
            :key="criteria.publicId"
            class="border border-slate-100 rounded-2xl overflow-hidden bg-white shadow-sm transition-all duration-300"
            :class="openCriteriaId === criteria.publicId && 'border-purple-200 ring-2 ring-purple-50'"
          >
            <!-- Criteria Card Header -->
            <div class="p-5">
              <div class="flex items-start gap-4">
                <!-- Index circle -->
                <div class="size-8 rounded-full bg-slate-100 flex items-center justify-center font-extrabold text-slate-700 text-sm shrink-0 shadow-inner">
                  {{ index + 1 }}
                </div>

                <!-- Purple Plus Circle Icon -->
                <div class="size-7 rounded-lg bg-purple-50 text-purple-600 border border-purple-100/50 flex items-center justify-center shrink-0 mt-0.5">
                  <UIcon
                    name="i-heroicons-plus-circle"
                    class="size-5"
                  />
                </div>

                <!-- Criteria Title -->
                <div class="flex-1 min-w-0">
                  <h5 class="text-sm font-extrabold text-slate-800 leading-snug">
                    {{ criteria.name }}
                  </h5>
                  <p class="mt-1 text-xs text-slate-400 leading-normal font-semibold">
                    {{ criteria.description || 'Chọn kê khai nếu có thành tích nổi bật tương ứng.' }}
                  </p>
                </div>
              </div>

              <!-- MENTOR BOX RIGHT INSIDE CARD HEADER (Mockup style) -->
              <div class="mt-4 bg-slate-50/70 border border-slate-100 rounded-xl p-4">
                <div class="flex items-center gap-2 mb-2">
                  <!-- Red Avatar with 'M' -->
                  <div class="size-6 rounded-full bg-rose-500 text-white flex items-center justify-center font-extrabold text-[10px] shadow-sm">
                    M
                  </div>
                  <span class="text-xs font-black text-slate-700">Mentor</span>
                  <!-- Blue check tag -->
                  <span class="inline-flex items-center gap-0.5 bg-blue-50 text-blue-600 border border-blue-100 rounded px-1.5 py-0.5 text-[9px] font-black uppercase">
                    <UIcon
                      name="i-heroicons-check-badge"
                      class="size-3"
                    />
                    Nhận xét
                  </span>
                </div>
                <p class="text-xs font-semibold text-slate-500 leading-relaxed">
                  {{ criteria.reviewerComment || 'Chưa có nhận xét' }}
                </p>
              </div>

              <!-- Footer of Card -->
              <div class="mt-4 pt-4 border-t border-slate-100/60 flex items-center justify-between gap-4">
                <div class="flex items-center gap-2">
                  <span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded text-[10px] font-extrabold bg-purple-50 text-purple-700 border border-purple-200 uppercase tracking-wider">
                    Tùy chọn
                  </span>
                  <span
                    :class="[
                      'text-[10px] font-extrabold px-2 py-0.5 rounded border shadow-sm uppercase tracking-wider',
                      isCriteriaSubmitted(criteria)
                        ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                        : 'bg-orange-50 text-orange-600 border-orange-200',
                    ]"
                  >
                    {{ isCriteriaSubmitted(criteria) ? 'Đã nộp' : 'Chưa nộp' }}
                  </span>
                </div>

                <UButton
                  size="sm"
                  :label="openCriteriaId === criteria.publicId ? 'Thu gọn' : (isCriteriaSubmitted(criteria) ? 'Chỉnh sửa' : '+ Thêm')"
                  :class="[
                    'cursor-pointer font-bold px-4 rounded-lg text-xs shadow-sm transition-all',
                    openCriteriaId === criteria.publicId
                      ? 'bg-slate-100 border border-slate-200 text-slate-600'
                      : (isCriteriaSubmitted(criteria)
                        ? 'bg-amber-50 border border-amber-200 text-amber-700 hover:bg-amber-100 hover:text-amber-800'
                        : 'bg-slate-50 border border-slate-200 text-slate-600 hover:bg-slate-100 hover:text-slate-800'),
                  ]"
                  @click="toggleCriteria(criteria.publicId)"
                />
              </div>
            </div>

            <!-- Criteria Card Body Form & Upload (Only visible when expanded) -->
            <div
              v-if="openCriteriaId === criteria.publicId"
              class="border-t border-slate-100 bg-slate-50/20 p-5 space-y-5"
            >
              <!-- Form fields -->
              <div class="space-y-4 bg-white p-4 rounded-xl border border-slate-100 shadow-inner">
                <UFormGroup
                  label="Giải trình cho Mentor"
                  name="additionalExplanation"
                  required
                >
                  <UTextarea
                    v-model="formStates[criteria.publicId]!.additionalExplanation"
                    placeholder="Giải trình thêm cho Mentor nếu cần..."
                    :rows="3"
                    size="sm"
                    :disabled="disabled"
                  />
                </UFormGroup>
              </div>

              <!-- Upload space -->
              <EvidenceUploader
                v-if="requiresEvidence(criteria)"
                v-model:files="formStates[criteria.publicId]!.files"
                :disabled="disabled"
              />

              <!-- Card Action Save -->
              <div
                v-if="!disabled"
                class="flex justify-end pt-3 border-t border-slate-100/50"
              >
                <UButton
                  label="Lưu tiêu chí"
                  icon="i-heroicons-check-circle"
                  :loading="savingId === criteria.publicId"
                  class="cursor-pointer font-bold px-4 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white text-xs shadow-sm"
                  @click="saveCriteriaData(criteria.publicId)"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import type { StandardDTO } from '~/types/applicationRecord'
import { useApplicationRecord, type CriteriaDeclaration } from '~/composables/useApplicationRecord'
import EvidenceUploader from '~/components/application/EvidenceUploader.vue'

const props = defineProps<{
  standards: StandardDTO[]
  disabled?: boolean
  level?: string
}>()

const emit = defineEmits<{
  save: [criteriaId: string, declaration: CriteriaDeclaration]
}>()

const { getDeclaration, saveDeclaration } = useApplicationRecord()

const openCriteriaId = ref<string | null>(null)
const savingId = ref<string | null>(null)

// Local form states
const formStates = reactive<Record<string, CriteriaDeclaration>>({})

// Initialize form states
const initFormStates = () => {
  props.standards.forEach((std) => {
    std.criteriaDTOList.forEach((crit) => {
      const dec = getDeclaration(crit.publicId)
      formStates[crit.publicId] = reactive({
        activityName: dec.activityName || '',
        organizingUnit: dec.organizingUnit || '',
        time: dec.time || '',
        achievementDesc: dec.achievementDesc || '',
        additionalExplanation: dec.additionalExplanation || '',
        files: dec.files ? [...dec.files] : [],
      })
    })
  })
}

// Watch props to re-init
watch(() => props.standards, () => {
  initFormStates()
}, { immediate: true, deep: true })

const toggleCriteria = (id: string) => {
  openCriteriaId.value = openCriteriaId.value === id ? null : id
}

// Helper filters
const getMandatoryCriteria = (standard: StandardDTO) => {
  return standard.criteriaDTOList.filter(c => c.isMandatory)
}

const getSelectiveCriteria = (standard: StandardDTO) => {
  return standard.criteriaDTOList.filter(c => !c.isMandatory)
}

const levelLabel = computed(() => {
  switch (props.level) {
    case 'CITY': return 'Cấp Thành phố'
    case 'NATION': return 'Cấp Trung ương'
    default: return 'Cấp Trường'
  }
})

const requiresEvidence = (crit: CriteriaDTO) => {
  return crit.evidenceType && crit.evidenceType !== 'NONE'
}

const isCriteriaSubmitted = (crit: CriteriaDTO) => {
  const state = formStates[crit.publicId]
  if (!state) return false
  if (requiresEvidence(crit)) {
    return state.files && state.files.length > 0
  }
  return state.additionalExplanation && state.additionalExplanation.trim().length > 0
}

// Save single criteria data
const saveCriteriaData = async (criteriaPublicId: string) => {
  const state = formStates[criteriaPublicId]
  if (!state) return

  if (!state.additionalExplanation.trim()) {
    useToast().add({
      title: 'Thiếu thông tin bắt buộc',
      description: 'Vui lòng điền giải trình cho Mentor.',
      color: 'error',
    })
    return
  }

  savingId.value = criteriaPublicId

  try {
    const rawDeclaration = {
      activityName: '',
      organizingUnit: '',
      time: '',
      achievementDesc: state.additionalExplanation,
      additionalExplanation: state.additionalExplanation,
      files: [...state.files],
    }

    await saveDeclaration(criteriaPublicId, rawDeclaration)
    emit('save', criteriaPublicId, rawDeclaration)
    openCriteriaId.value = null
  }
  catch {
    useToast().add({
      title: 'Lỗi khi lưu',
      description: 'Không thể lưu thông tin tiêu chí.',
      color: 'error',
    })
  }
  finally {
    savingId.value = null
  }
}

const saveAll = async () => {
  for (const std of props.standards) {
    for (const crit of std.criteriaDTOList) {
      const state = formStates[crit.publicId]
      if (state) {
        const hasExplanation = state.additionalExplanation && state.additionalExplanation.trim().length > 0
        const hasFiles = state.files && state.files.length > 0
        if (hasExplanation || hasFiles) {
          const rawDeclaration = {
            activityName: '',
            organizingUnit: '',
            time: '',
            achievementDesc: state.additionalExplanation,
            additionalExplanation: state.additionalExplanation,
            files: [...state.files],
          }
          await saveDeclaration(crit.publicId, rawDeclaration)
          emit('save', crit.publicId, rawDeclaration)
        }
      }
    }
  }
}

defineExpose({
  saveAll,
})
</script>
