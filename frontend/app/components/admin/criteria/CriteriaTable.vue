<template>
  <div class="space-y-5">
    <CommonPageSection
      v-for="standard in standards"
      :key="standard.publicId"
      :title="standard.name"
      title-icon="i-lucide-list-tree"
      inner-class="!block p-0"
    >
      <template #title>
        <UButton
          class="ml-auto"
          color="info"
          variant="soft"
          size="sm"
          icon="i-lucide-plus"
          label="Thêm tiêu chí"
          @click="emit('create-root', standard.publicId)"
        />
      </template>

      <div
        v-if="flatten(standard.criteria).length"
        class="overflow-x-auto"
      >
        <UTable
          :data="flatten(standard.criteria)"
          :columns="columns"
          class="min-w-190"
        >
          <template #name-cell="{ row }">
            <div
              class="flex min-w-60 items-center"
              :style="{ paddingLeft: `${row.original.depth * 28}px` }"
            >
              <UIcon
                :name="row.original.depth ? 'i-lucide-corner-down-right' : 'i-lucide-circle-dot'"
                class="mr-2 size-4 shrink-0 text-[#60A5FA]"
              />
              <div>
                <p class="font-semibold text-[#1E293B]">
                  {{ row.original.name }}
                </p>
                <p
                  v-if="row.original.description"
                  class="line-clamp-1 text-xs text-[#64748B]"
                >
                  {{ row.original.description }}
                </p>
              </div>
            </div>
          </template>

          <template #mandatory-cell="{ row }">
            <UBadge
              :color="row.original.mandatory ? 'warning' : 'neutral'"
              variant="subtle"
              class="rounded-full"
            >
              {{ row.original.mandatory ? 'Bắt buộc' : 'Tùy chọn' }}
            </UBadge>
          </template>

          <template #evidenceType-cell="{ row }">
            {{ row.original.evidenceType || 'NONE' }}
          </template>

          <template #requiredChildrenCount-cell="{ row }">
            {{ row.original.requiredChildrenCount }}
          </template>

          <template #actions-cell="{ row }">
            <div class="flex justify-end gap-1">
              <UButton
                color="info"
                variant="ghost"
                icon="i-lucide-plus"
                aria-label="Thêm tiêu chí con"
                @click="emit('create-child', standard.publicId, row.original)"
              />
              <UButton
                color="info"
                variant="ghost"
                icon="i-lucide-pen-line"
                aria-label="Sửa tiêu chí"
                @click="emit('edit', standard.publicId, row.original)"
              />
              <UButton
                color="error"
                variant="ghost"
                icon="i-lucide-trash-2"
                aria-label="Xóa tiêu chí"
                @click="emit('delete', row.original)"
              />
            </div>
          </template>
        </UTable>
      </div>
      <div
        v-else
        class="py-10 text-center text-sm text-[#64748B]"
      >
        Chưa có tiêu chí trong standard này.
      </div>
    </CommonPageSection>
  </div>
</template>

<script setup lang="ts">
import type { TableColumn } from '@nuxt/ui'
import type { CriteriaNode, StandardWithCriteria } from '~/types/admin'

type FlatCriteria = CriteriaNode & { depth: number }

defineProps<{ standards: StandardWithCriteria[] }>()

const emit = defineEmits<{
  'create-root': [standardPublicId: string]
  'create-child': [standardPublicId: string, parent: CriteriaNode]
  'edit': [standardPublicId: string, criteria: CriteriaNode]
  'delete': [criteria: CriteriaNode]
}>()

const columns: TableColumn<FlatCriteria>[] = [
  { accessorKey: 'name', header: 'Tiêu chí' },
  { accessorKey: 'mandatory', header: 'Yêu cầu' },
  { accessorKey: 'evidenceType', header: 'Loại minh chứng' },
  { accessorKey: 'requiredChildrenCount', header: 'Số tiêu chí con cần đạt' },
  { id: 'actions', header: '' },
]

const flatten = (nodes: CriteriaNode[], depth = 0): FlatCriteria[] => nodes.flatMap(node => [
  { ...node, depth },
  ...flatten(node.children, depth + 1),
])
</script>
