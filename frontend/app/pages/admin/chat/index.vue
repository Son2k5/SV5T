<template>
  <div class="space-y-6">
    <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
      <div>
        <h1 class="text-2xl font-bold text-[#1E293B]">
          Chat hỗ trợ
        </h1>
      </div>
      <div class="flex items-center gap-2 text-sm text-[#64748B]">
        <span
          class="size-2.5 rounded-full"
          :class="connected ? 'bg-emerald-500' : 'bg-slate-300'"
        />
        {{ connected ? 'Đang trực tuyến' : 'Đang kết nối' }}
      </div>
    </div>

    <div class="grid min-h-[calc(100vh-13rem)] gap-4 lg:grid-cols-[22rem_minmax(0,1fr)]">
      <aside class="flex min-h-[32rem] flex-col overflow-hidden rounded-2xl border border-[#E5E7EB] bg-white">
        <div class="border-b border-[#E5E7EB] p-4">
          <UInput
            v-model="keyword"
            icon="i-lucide-search"
            placeholder="Tim theo ten, email, MSSV"
            :ui="{ base: 'rounded-2xl' }"
          />
        </div>

        <div class="min-h-0 flex-1 overflow-y-auto">
          <div
            v-if="loading && !adminRooms.length"
            class="space-y-2 p-3"
          >
            <div
              v-for="index in 5"
              :key="index"
              class="h-16 animate-pulse rounded-2xl bg-slate-100"
            />
          </div>
          <div
            v-else-if="!adminRooms.length"
            class="flex h-full flex-col items-center justify-center px-6 text-center"
          >
            <div class="mb-3 flex size-12 items-center justify-center rounded-2xl bg-blue-50 text-[#2563EB]">
              <UIcon
                name="i-lucide-inbox"
                class="size-6"
              />
            </div>
            <p class="text-sm font-semibold text-[#334155]">
              Chua co phong chat
            </p>
          </div>
          <button
            v-for="room in adminRooms"
            v-else
            :key="room.publicId"
            type="button"
            class="flex w-full gap-3 border-b border-[#F1F5F9] px-4 py-3 text-left transition hover:bg-blue-50/60"
            :class="room.publicId === activeAdminRoomPublicId ? 'bg-blue-50' : 'bg-white'"
            @click="selectRoom(room.publicId)"
          >
            <img
              :src="room.userAvatar || '/profilePlaceholder.png'"
              :alt="room.userName"
              class="size-11 shrink-0 rounded-full object-cover"
            >
            <span class="min-w-0 flex-1">
              <span class="flex items-center gap-2">
                <span class="truncate text-sm font-bold text-[#1E293B]">{{ room.userName }}</span>
                <span
                  v-if="room.unreadForAdmin > 0"
                  class="ml-auto flex min-w-5 shrink-0 items-center justify-center rounded-full bg-red-500 px-1.5 text-[10px] font-bold leading-5 text-white"
                >
                  {{ room.unreadForAdmin > 99 ? '99+' : room.unreadForAdmin }}
                </span>
              </span>
              <span class="mt-0.5 block truncate text-xs text-[#64748B]">{{ room.userEmail }}</span>
              <span class="mt-1 block truncate text-xs text-[#94A3B8]">
                {{ room.lastMessage || 'Chua co tin nhan' }}
              </span>
            </span>
          </button>
        </div>

        <div
          v-if="adminRoomsPage.totalElements > adminRoomsPage.size"
          class="border-t border-[#E5E7EB] p-3"
        >
          <UPagination
            v-model:page="uiPage"
            :total="adminRoomsPage.totalElements"
            :items-per-page="adminRoomsPage.size"
            size="sm"
          />
        </div>
      </aside>

      <section class="flex min-h-[32rem] flex-col overflow-hidden rounded-2xl border border-[#E5E7EB] bg-white">
        <template v-if="activeRoom">
          <header class="flex h-16 shrink-0 items-center gap-3 border-b border-[#E5E7EB] px-4">
            <img
              :src="activeRoom.userAvatar || '/profilePlaceholder.png'"
              :alt="activeRoom.userName"
              class="size-10 rounded-full object-cover"
            >
            <div class="min-w-0 flex-1">
              <h2 class="truncate text-sm font-bold text-[#1E293B]">
                {{ activeRoom.userName }}
              </h2>
              <p class="truncate text-xs text-[#64748B]">
                {{ activeRoom.userEmail }}
              </p>
            </div>
            <UButton
              color="neutral"
              variant="ghost"
              icon="i-lucide-check-check"
              class="rounded-xl"
              aria-label="Danh dau da doc"
              @click="readActiveRoom"
            />
          </header>

          <div
            ref="messageList"
            class="min-h-0 flex-1 space-y-3 overflow-y-auto bg-[#F8FAFC] px-4 py-5"
          >
            <div
              v-if="loading && !adminMessages.length"
              class="space-y-3"
            >
              <div
                v-for="index in 6"
                :key="index"
                class="h-12 animate-pulse rounded-2xl bg-white"
              />
            </div>
            <div
              v-else-if="!adminMessages.length"
              class="flex h-full flex-col items-center justify-center text-center"
            >
              <div class="mb-3 flex size-12 items-center justify-center rounded-2xl bg-blue-50 text-[#2563EB]">
                <UIcon
                  name="i-lucide-message-square"
                  class="size-6"
                />
              </div>
              <p class="text-sm font-semibold text-[#334155]">
                Chua co tin nhan
              </p>
            </div>
            <div
              v-for="message in adminMessages"
              :key="message.publicId"
              class="flex"
              :class="message.mine ? 'justify-end' : 'justify-start'"
            >
              <div
                class="max-w-[72%] rounded-2xl px-3 py-2 text-sm leading-5 shadow-sm"
                :class="message.mine ? 'rounded-br-md bg-[#2563EB] text-white' : 'rounded-bl-md border border-[#E5E7EB] bg-white text-[#1E293B]'"
              >
                <p
                  v-if="!message.mine"
                  class="mb-1 text-[11px] font-semibold text-[#64748B]"
                >
                  {{ message.senderName }}
                </p>
                <p class="whitespace-pre-wrap break-words">
                  {{ message.content }}
                </p>
                <p
                  class="mt-1 text-[10px]"
                  :class="message.mine ? 'text-blue-100' : 'text-[#94A3B8]'"
                >
                  {{ formatTime(message.createdAt) }}
                </p>
              </div>
            </div>
          </div>

          <form
            class="flex shrink-0 items-end gap-2 border-t border-[#E5E7EB] p-4"
            @submit.prevent="send"
          >
            <UTextarea
              v-model="draft"
              class="min-w-0 flex-1"
              :rows="1"
              autoresize
              :maxrows="5"
              placeholder="Nhap phan hoi..."
              :ui="{ base: 'rounded-2xl resize-none' }"
              @keydown.enter.exact.prevent="send"
            />
            <UButton
              type="submit"
              color="info"
              icon="i-lucide-send"
              class="h-10 w-10 justify-center rounded-2xl !p-0"
              :loading="sending"
              :disabled="!draft.trim()"
              aria-label="Gui tin nhan"
            />
          </form>
        </template>

        <div
          v-else
          class="flex min-h-0 flex-1 flex-col items-center justify-center px-6 text-center"
        >
          <div class="mb-3 flex size-14 items-center justify-center rounded-2xl bg-blue-50 text-[#2563EB]">
            <UIcon
              name="i-lucide-messages-square"
              class="size-7"
            />
          </div>
          <p class="text-base font-bold text-[#1E293B]">
            Chon mot phong chat
          </p>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'admin', middleware: 'require-admin' })

const {
  activeAdminRoomPublicId,
  adminMessages,
  adminRooms,
  adminRoomsPage,
  fetchAdminMessages,
  fetchAdminRooms,
  loading,
  markAdminRoomRead,
  sendAdminMessage,
  sending,
  setActiveAdminRoom,
} = useChat()
const { connected, subscribe, unsubscribe } = useChatConnection()

const keyword = ref('')
const uiPage = ref(1)
const draft = ref('')
const messageList = ref<HTMLElement | null>(null)
const activeRoomDestination = ref<string | null>(null)
const adminDestination = '/topic/chat/admin'

const activeRoom = computed(() =>
  adminRooms.value.find(room => room.publicId === activeAdminRoomPublicId.value) || null,
)

const loadRooms = async () => {
  await fetchAdminRooms({ page: uiPage.value - 1, size: 20, keyword: keyword.value })
  const firstRoom = adminRooms.value[0]
  if (!activeAdminRoomPublicId.value && firstRoom) {
    await selectRoom(firstRoom.publicId)
  }
}

const selectRoom = async (roomPublicId: string) => {
  setActiveAdminRoom(roomPublicId)
  subscribeRoom(roomPublicId)
  await fetchAdminMessages(roomPublicId)
  await scrollToBottom()
}

const subscribeRoom = (roomPublicId: string) => {
  const destination = `/topic/chat/rooms/${roomPublicId}`
  if (activeRoomDestination.value === destination) return

  if (activeRoomDestination.value) {
    unsubscribe(activeRoomDestination.value)
  }
  activeRoomDestination.value = destination
  subscribe(destination)
}

const readActiveRoom = async () => {
  if (!activeAdminRoomPublicId.value) return
  await markAdminRoomRead(activeAdminRoomPublicId.value)
}

const send = async () => {
  if (!activeAdminRoomPublicId.value) return

  const content = draft.value.trim()
  if (!content) return

  draft.value = ''
  await sendAdminMessage(activeAdminRoomPublicId.value, content)
  await scrollToBottom()
}

const scrollToBottom = async () => {
  await nextTick()
  if (!messageList.value) return
  messageList.value.scrollTop = messageList.value.scrollHeight
}

const formatTime = (value: string) => {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return new Intl.DateTimeFormat('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

const debouncedLoadRooms = useDebounceFn(() => {
  uiPage.value = 1
  void loadRooms()
}, 350)

watch(keyword, debouncedLoadRooms)
watch(uiPage, () => {
  void loadRooms()
})
watch(adminMessages, () => {
  void scrollToBottom()
}, { deep: true })

onMounted(async () => {
  subscribe(adminDestination)
  await loadRooms()
})

onBeforeUnmount(() => {
  unsubscribe(adminDestination)
  if (activeRoomDestination.value) {
    unsubscribe(activeRoomDestination.value)
  }
})
</script>
