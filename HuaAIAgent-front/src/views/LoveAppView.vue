<script setup>
import { computed, onMounted, ref } from 'vue'
import { Check, Copy, Heart, RefreshCw } from 'lucide-vue-next'
import ChatRoom from '@/components/ChatRoom.vue'
import { AI_ENDPOINTS } from '@/api/ai'
import { createChatId } from '@/utils/id'

const chatId = ref('')
const copied = ref(false)
const roomRef = ref(null)

onMounted(() => {
  chatId.value = createChatId()
})

const shortChatId = computed(() => {
  if (!chatId.value) return ''
  return `${chatId.value.slice(0, 8)}…${chatId.value.slice(-4)}`
})

const subtitle = computed(() => (shortChatId.value ? `会话 ${shortChatId.value}` : '正在创建会话…'))

const buildParams = () => ({ chatId: chatId.value })

const newSession = () => {
  chatId.value = createChatId()
  roomRef.value?.clear()
}

const copyChatId = async () => {
  if (!chatId.value) return
  try {
    if (navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(chatId.value)
    } else {
      const helper = document.createElement('textarea')
      helper.value = chatId.value
      helper.style.position = 'fixed'
      helper.style.opacity = '0'
      document.body.appendChild(helper)
      helper.select()
      document.execCommand('copy')
      document.body.removeChild(helper)
    }
    copied.value = true
    setTimeout(() => {
      copied.value = false
    }, 1600)
  } catch {
    copied.value = false
  }
}
</script>

<template>
  <ChatRoom
    ref="roomRef"
    title="AI 恋爱大师"
    :subtitle="subtitle"
    :endpoint="AI_ENDPOINTS.loveChatSse"
    :build-params="buildParams"
    accent="#e11d48"
    accent-soft="#fff1f2"
    assistant-name="恋爱大师"
    :assistant-icon="Heart"
    empty-text="说说你的感情困惑吧"
    placeholder="说点什么…"
  >
    <template #actions>
      <button class="icon-btn" title="复制会话 ID" @click="copyChatId">
        <Check v-if="copied" :size="17" />
        <Copy v-else :size="17" />
      </button>
      <button class="icon-btn" title="新建会话" @click="newSession">
        <RefreshCw :size="17" />
      </button>
    </template>
  </ChatRoom>
</template>
