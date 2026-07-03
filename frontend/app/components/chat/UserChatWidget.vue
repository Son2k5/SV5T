<template>
  <div
    v-if="accessToken && !isAdmin"
    class="fixed bottom-5 right-5 z-50 flex flex-col items-end sm:bottom-6 sm:right-6"
  >
    <Transition
      enter-from-class="translate-y-3 opacity-0"
      enter-active-class="transition duration-200"
      leave-active-class="transition duration-150"
      leave-to-class="translate-y-3 opacity-0"
    >
      <section
        v-if="open"
        class="mb-4 flex h-[min(34rem,calc(100vh-7rem))] w-[calc(100vw-2.5rem)] max-w-[24rem] flex-col overflow-hidden rounded-2xl border border-[#D7E3F8] bg-white shadow-[0_24px_70px_rgba(15,23,42,0.22)]"
        aria-label="Chat hỗ trợ"
      >
        <header class="flex h-16 shrink-0 items-center gap-3 bg-[#2563EB] px-4 text-white">
          <div class="flex size-10 items-center justify-center rounded-xl bg-white/15">
            <UIcon
              name="i-lucide-message-circle"
              class="size-5"
            />
          </div>
          <div class="min-w-0 flex-1">
            <h2 class="truncate text-sm font-bold">
              Chat hỗ trợ
            </h2>
            <p class="truncate text-xs text-blue-100">
              {{ connected ? 'Đang trực tuyến' : 'Đang kết nối...' }}
            </p>
          </div>
          <UButton
            color="neutral"
            variant="ghost"
            icon="i-lucide-x"
            class="h-9 w-9 justify-center rounded-xl !p-0 text-white hover:bg-white/15 hover:text-white"
            aria-label="Dong chat"
            @click="closeChat"
          />
        </header>

        <div
          ref="messageList"
          class="min-h-0 flex-1 space-y-3 overflow-y-auto bg-[#F8FAFC] px-4 py-4"
        >
          <div
            v-if="loading && !userMessages.length"
            class="space-y-3"
          >
            <div
              v-for="index in 4"
              :key="index"
              class="h-12 animate-pulse rounded-2xl bg-white"
            />
          </div>
          <div
            v-else-if="!userMessages.length"
            class="flex h-full flex-col items-center justify-center text-center"
          >
            <div class="mb-3 flex size-12 items-center justify-center rounded-2xl bg-blue-50 text-[#2563EB]">
              <UIcon
                name="i-lucide-headphones"
                class="size-6"
              />
            </div>
            <p class="text-sm font-semibold text-[#334155]">
              Bắt đầu cuộc trò chuyện
            </p>
            
          </div>
          <div
            v-for="message in userMessages"
            :key="message.publicId"
            class="flex"
            :class="message.mine ? 'justify-end' : 'justify-start'"
          >
            <div
              class="max-w-[82%] rounded-2xl px-3 py-2 text-sm leading-5 shadow-sm"
              :class="message.mine ? 'rounded-br-md bg-[#2563EB] text-white' : 'rounded-bl-md border border-[#E5E7EB] bg-white text-[#1E293B]'"
            >
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
          class="flex shrink-0 items-end gap-2 border-t border-[#E5E7EB] bg-white p-3"
          @submit.prevent="send"
        >
          <UTextarea
            v-model="draft"
            class="min-w-0 flex-1"
            :rows="1"
            autoresize
            :maxrows="4"
            placeholder="Nhap tin nhan..."
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
            aria-label="Gửi tin nhắn"
          />
        </form>
      </section>
    </Transition>

    <UButton
      color="info"
      icon="i-lucide-message-circle"
      class="relative h-14 w-14 justify-center rounded-2xl !p-0 shadow-[0_18px_45px_rgba(37,99,235,0.28)]"
      :ui="{ leadingIcon: 'size-6' }"
      aria-label="Mở chat hỗ trợ "
      @click="toggle"
    >
      <span
        v-if="unreadCount > 0"
        class="absolute -right-1 -top-1 flex min-w-5 items-center justify-center rounded-full bg-red-500 px-1.5 text-[10px] font-bold leading-5 text-white"
      >
        {{ unreadCount > 99 ? '99+' : unreadCount }}
      </span>
    </UButton>
  </div>
</template>

<script setup lang="ts">
import { useAdminAccess } from '~/composables/admin/useAdminAccess'

const { accessToken } = useAuth()
const { isAdmin } = useAdminAccess()
const {
  fetchMyMessages,
  fetchMyRoom,
  loading,
  markMyRoomRead,
  sendMyMessage,
  sending,
  userMessages,
  userRoom,
} = useChat()
const { connected, subscribe, unsubscribe } = useChatConnection()

const open = ref(false)
const draft = ref('')
const messageList = ref<HTMLElement | null>(null)
const subscribedRoom = ref<string | null>(null)

const unreadCount = computed(() => userRoom.value?.unreadForUser || 0)

const closeChat = () => {
  open.value = false
}

const toggle = async () => {
  open.value = !open.value
  if (open.value) {
    await load()
    await markMyRoomRead().catch(() => {})
    await scrollToBottom()
  }
}

const load = async () => {
  if (!accessToken.value || isAdmin.value) return

  const room = userRoom.value || await fetchMyRoom()
  subscribeRoom(room.publicId)
  if (!userMessages.value.length) {
    await fetchMyMessages()
  }
}

const subscribeRoom = (roomPublicId: string) => {
  const destination = `/topic/chat/rooms/${roomPublicId}`
  if (subscribedRoom.value === destination) return

  if (subscribedRoom.value) {
    unsubscribe(subscribedRoom.value)
  }
  subscribedRoom.value = destination
  subscribe(destination)
}

const send = async () => {
  const content = draft.value.trim()
  if (!content) return

  draft.value = ''
  await sendMyMessage(content)
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
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

watch(userMessages, () => {
  if (open.value) {
    void scrollToBottom()
  }
}, { deep: true })

watch(accessToken, (token) => {
  if (!token && subscribedRoom.value) {
    unsubscribe(subscribedRoom.value)
    subscribedRoom.value = null
  }
})

onMounted(() => {
  void load().catch(() => {})
})

onBeforeUnmount(() => {
  if (subscribedRoom.value) {
    unsubscribe(subscribedRoom.value)
  }
})
</script>
