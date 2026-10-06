import { computed, nextTick, reactive, ref } from 'vue'
import { http } from '@/api/http'
import { createId } from '@/utils/id'
import { streamSse } from '@/utils/sse'

export function useChatRoom({ endpoint, buildParams }) {
  const messages = ref([])
  const isStreaming = ref(false)
  const errorMessage = ref('')
  const scrollEl = ref(null)

  let stream = null
  let finalized = true

  const hasMessages = computed(() => messages.value.length > 0)

  const scrollToBottom = () => {
    nextTick(() => {
      const el = scrollEl.value
      if (el) el.scrollTop = el.scrollHeight
    })
  }

  const finalize = (status) => {
    if (finalized) return
    finalized = true
    isStreaming.value = false

    const last = messages.value[messages.value.length - 1]
    if (last && last.role === 'assistant') last.status = status

    stream = null
    scrollToBottom()
  }

  const stop = () => {
    if (!isStreaming.value) return
    stream?.abort()
    finalize('stopped')
  }

  const send = (rawText) => {
    const text = (rawText || '').trim()
    if (!text || isStreaming.value) return

    errorMessage.value = ''
    finalized = false

    messages.value.push({
      id: createId(),
      role: 'user',
      content: text,
      status: 'done',
    })

    const reply = reactive({
      id: createId(),
      role: 'assistant',
      content: '',
      status: 'streaming',
    })
    messages.value.push(reply)

    isStreaming.value = true
    scrollToBottom()

    const params = { message: text }
    if (typeof buildParams === 'function') {
      Object.assign(params, buildParams() || {})
    }

    stream = streamSse({
      client: http,
      url: endpoint,
      params,
      onMessage: ({ data }) => {
        if (data.trim() === '[DONE]') {
          stream?.abort()
          finalize('done')
          return
        }
        reply.content += data
        scrollToBottom()
      },
      onEnd: () => finalize('done'),
      onError: (error) => {
        errorMessage.value = error?.message || '请求失败，请稍后重试'
        finalize('error')
      },
    })
  }

  const clear = () => {
    stop()
    messages.value = []
    errorMessage.value = ''
  }

  return {
    messages,
    isStreaming,
    errorMessage,
    hasMessages,
    scrollEl,
    send,
    stop,
    clear,
    scrollToBottom,
  }
}
