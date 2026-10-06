<script setup>
import { useRouter } from 'vue-router'
import { ArrowLeft, Trash2 } from 'lucide-vue-next'
import ChatInput from './ChatInput.vue'
import ChatMessage from './ChatMessage.vue'
import { useChatRoom } from '@/composables/useChatRoom'

const props = defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  endpoint: { type: String, required: true },
  buildParams: { type: Function, default: null },
  accent: { type: String, default: '#e11d48' },
  accentSoft: { type: String, default: '#fff1f2' },
  assistantName: { type: String, default: 'AI 助手' },
  assistantIcon: { type: [Object, Function], default: null },
  emptyText: { type: String, default: '开始新的对话' },
  placeholder: { type: String, default: '说点什么…' },
})

const router = useRouter()
const { messages, isStreaming, errorMessage, hasMessages, scrollEl, send, stop, clear } =
  useChatRoom({
    endpoint: props.endpoint,
    buildParams: props.buildParams,
  })

defineExpose({ clear, stop, send })
</script>

<template>
  <div
    class="chat"
    :style="{ '--accent': accent, '--accent-soft': accentSoft }"
  >
    <header class="chat__header">
      <button class="icon-btn" title="返回应用中心" @click="router.push('/')">
        <ArrowLeft :size="18" />
      </button>

      <div class="chat__heading">
        <div class="chat__title">{{ title }}</div>
        <div v-if="subtitle" class="chat__subtitle">{{ subtitle }}</div>
      </div>

      <div class="chat__actions">
        <slot name="actions" />
        <button class="icon-btn" title="清空对话" @click="clear">
          <Trash2 :size="18" />
        </button>
      </div>
    </header>

    <main ref="scrollEl" class="chat__body">
      <div v-if="!hasMessages" class="chat__empty">
        <div class="chat__empty-icon">
          <component :is="assistantIcon" :size="26" v-if="assistantIcon" />
        </div>
        <p>{{ emptyText }}</p>
      </div>

      <ChatMessage
        v-for="message in messages"
        :key="message.id"
        :message="message"
        :assistant-name="assistantName"
        :assistant-icon="assistantIcon"
      />
    </main>

    <div v-if="errorMessage" class="chat__error">{{ errorMessage }}</div>

    <footer class="chat__footer">
      <ChatInput :streaming="isStreaming" :placeholder="placeholder" @send="send" @stop="stop" />
    </footer>
  </div>
</template>

<style scoped>
.chat {
  display: flex;
  flex-direction: column;
  height: 100dvh;
  background: var(--bg);
}

.chat__header {
  flex: none;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  background: var(--surface);
  border-bottom: 1px solid var(--border);
}

.chat__heading {
  flex: 1;
  min-width: 0;
}

.chat__title {
  font-size: 15px;
  font-weight: 600;
  line-height: 1.4;
}

.chat__subtitle {
  font-size: 12px;
  color: var(--text-muted);
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat__actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.chat__body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 20px max(16px, calc((100% - 880px) / 2)) 8px;
  scroll-behavior: smooth;
}

.chat__empty {
  margin: auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: var(--text-soft);
}

.chat__empty p {
  margin: 0;
  font-size: 14px;
}

.chat__empty-icon {
  display: grid;
  place-items: center;
  width: 52px;
  height: 52px;
  border-radius: 8px;
  background: var(--accent-soft);
  color: var(--accent);
}

.chat__error {
  flex: none;
  margin: 0 max(16px, calc((100% - 880px) / 2)) 8px;
  padding: 8px 12px;
  border: 1px solid #fecaca;
  border-radius: 6px;
  background: #fef2f2;
  color: #b91c1c;
  font-size: 13px;
}

.chat__footer {
  flex: none;
  padding: 12px 16px calc(16px + env(safe-area-inset-bottom));
  background: var(--surface);
  border-top: 1px solid var(--border);
}

@media (max-width: 640px) {
  .chat__body {
    padding: 16px 12px 6px;
  }

  .chat__error {
    margin: 0 12px 8px;
  }

  .chat__footer {
    padding: 10px 12px calc(12px + env(safe-area-inset-bottom));
  }
}
</style>
