<template>
  <div class="h-full flex flex-col min-h-0 bg-slate-50/50">
    <div class="p-4 border-b border-slate-100 bg-white shrink-0">
      <h3 class="font-bold text-[#1E293B] text-sm flex items-center gap-2">
        <UIcon
          name="i-lucide-layers"
          class="text-blue-600 size-4.5"
        />
        Danh mục Tiêu chuẩn & Tiêu chí
      </h3>
      <p class="text-[11px] text-[#64748B] mt-0.5">
        Chọn một tiêu chí có minh chứng để duyệt
      </p>
    </div>

    <!-- Scrollable Tree List -->
    <div class="flex-1 overflow-y-auto p-3 space-y-3 min-h-0">
      <div
        v-for="standard in tree"
        :key="standard.publicId"
        class="border border-slate-200/80 rounded-xl bg-white overflow-hidden shadow-xs"
      >
        <!-- Standard Header Toggle -->
        <button
          class="w-full flex items-center justify-between gap-3 bg-slate-50/60 px-4 py-3 hover:bg-slate-50 transition text-left cursor-pointer"
          @click="toggleStandard(standard.publicId)"
        >
          <div class="min-w-0 flex-1">
            <span class="text-[9px] font-bold uppercase tracking-wider text-blue-600">Tiêu chuẩn</span>
            <h4 class="font-semibold text-xs text-[#1E293B] mt-0.5 truncate">
              {{ standard.name }}
            </h4>
          </div>
          <UIcon
            name="i-lucide-chevron-down"
            class="size-4 text-[#64748B] transition-transform duration-200"
            :class="{ 'rotate-180': expandedStandards.includes(standard.publicId) }"
          />
        </button>

        <!-- Criteria List inside Standard -->
        <div
          v-show="expandedStandards.includes(standard.publicId)"
          class="divide-y divide-slate-100 border-t border-slate-100"
        >
          <template v-if="standard.criteriaDTOList && standard.criteriaDTOList.length > 0">
            <div
              v-for="criteria in standard.criteriaDTOList"
              :key="criteria.publicId"
              class="flex flex-col"
            >
              <!-- Primary Criteria Item -->
              <div
                class="flex flex-col gap-2 p-3 transition-colors select-none relative"
                :class="[
                  criteria.evidenceUrl ? 'cursor-pointer hover:bg-slate-50/80' : 'opacity-85',
                  selectedCriteriaPublicId === criteria.publicId ? 'bg-indigo-50/80 hover:bg-indigo-50 border-l-4 border-indigo-600' : '',
                ]"
                @click="onCriteriaClick(criteria, standard)"
              >
                <div class="flex items-start justify-between gap-2.5">
                  <div class="flex items-start gap-2 min-w-0 flex-1">
                    <span class="flex size-5 shrink-0 items-center justify-center rounded-md bg-blue-50 text-[10px] font-bold text-[#2563EB] mt-0.5">
                      {{ criteria.isMandatory ? '★' : 'o' }}
                    </span>
                    <div class="min-w-0">
                      <h5 class="font-semibold text-xs text-[#1E293B] leading-tight flex flex-wrap items-center gap-1">
                        {{ criteria.name }}
                        <span
                          v-if="criteria.isMandatory"
                          class="text-[9px] text-amber-600 bg-amber-50 border border-amber-200/50 px-1.5 py-0.2 rounded-full font-bold"
                        >
                          Bắt buộc
                        </span>
                      </h5>
                      <p
                        v-if="criteria.description"
                        class="text-[10px] text-[#64748B] mt-1 line-clamp-2 leading-relaxed"
                      >
                        {{ criteria.description }}
                      </p>
                    </div>
                  </div>

                  <!-- Right Side: Status Badge -->
                  <div class="shrink-0 flex flex-col items-end gap-1.5 mt-0.5">
                    <AdminEvidenceStatusBadge
                      :status="criteria.evidenceUrl ? criteria.evidenceStatus : 'NOT_SUBMITTED'"
                      size="xs"
                    />
                    <!-- Has Evidence Indicator -->
                    <div
                      v-if="criteria.evidenceUrl"
                      class="text-[10px] text-blue-600 font-bold flex items-center gap-0.5"
                    >
                      <UIcon
                        name="i-lucide-file-text"
                        class="size-3"
                      />
                      Có tệp
                    </div>
                  </div>
                </div>
              </div>

              <!-- Sub Criteria List -->
              <div
                v-if="criteria.subCriteriaList && criteria.subCriteriaList.length > 0"
                class="bg-slate-50/30 pl-4 border-l border-slate-100"
              >
                <div
                  v-for="sub in criteria.subCriteriaList"
                  :key="sub.publicId"
                  class="flex flex-col gap-2 p-3 border-t border-slate-100/50 transition-colors select-none relative"
                  :class="[
                    sub.evidenceUrl ? 'cursor-pointer hover:bg-slate-50/80' : 'opacity-85',
                    selectedCriteriaPublicId === sub.publicId ? 'bg-indigo-50/80 hover:bg-indigo-50 border-l-4 border-indigo-600' : '',
                  ]"
                  @click="onCriteriaClick(sub, standard)"
                >
                  <div class="flex items-start justify-between gap-2.5">
                    <div class="flex items-start gap-2 min-w-0 flex-1">
                      <span class="text-slate-400 mt-0.5 font-bold shrink-0 text-xs">-</span>
                      <div class="min-w-0">
                        <h6 class="font-medium text-xs text-[#334155] leading-tight flex flex-wrap items-center gap-1">
                          {{ sub.name }}
                          <span
                            v-if="sub.isMandatory"
                            class="text-[8px] text-amber-600 bg-amber-50 border border-amber-200/50 px-1 py-0.2 rounded-full font-bold"
                          >
                            Bắt buộc
                          </span>
                        </h6>
                        <p
                          v-if="sub.description"
                          class="text-[10px] text-[#64748B] mt-1 line-clamp-2 leading-relaxed"
                        >
                          {{ sub.description }}
                        </p>
                      </div>
                    </div>

                    <!-- Right Side: Status Badge -->
                    <div class="shrink-0 flex flex-col items-end gap-1.5 mt-0.5">
                      <AdminEvidenceStatusBadge
                        :status="sub.evidenceUrl ? sub.evidenceStatus : 'NOT_SUBMITTED'"
                        size="xs"
                      />
                      <!-- Has Evidence Indicator -->
                      <div
                        v-if="sub.evidenceUrl"
                        class="text-[10px] text-blue-600 font-bold flex items-center gap-0.5"
                      >
                        <UIcon
                          name="i-lucide-file-text"
                          class="size-3"
                        />
                        Có tệp
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </template>
          <div
            v-else
            class="py-4 text-center text-xs text-[#94A3B8]"
          >
            Không có tiêu chí nào trong tiêu chuẩn này.
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { StandardResultDTO, CriteriaResultDTO } from '~/types/admin'

const props = defineProps<{
  tree: StandardResultDTO[]
  selectedCriteriaPublicId?: string | null
}>()

const emit = defineEmits<{
  'select-criteria': [data: { criteria: CriteriaResultDTO, standard: StandardResultDTO }]
}>()

const expandedStandards = ref<string[]>([])

// Auto expand the first standard with items on load/tree update
watch(
  () => props.tree,
  (newTree) => {
    if (newTree && newTree.length > 0 && expandedStandards.value.length === 0) {
      const firstId = newTree[0]?.publicId
      if (firstId) {
        expandedStandards.value = [firstId]
      }
    }
  },
  { immediate: true },
)

function toggleStandard(publicId: string) {
  const index = expandedStandards.value.indexOf(publicId)
  if (index >= 0) {
    expandedStandards.value.splice(index, 1)
  }
  else {
    expandedStandards.value.push(publicId)
  }
}

function onCriteriaClick(criteria: CriteriaResultDTO, standard: StandardResultDTO) {
  // We allow selecting any criteria that has an evidenceUrl
  if (criteria.evidenceUrl) {
    emit('select-criteria', { criteria, standard })
  }
}
</script>
