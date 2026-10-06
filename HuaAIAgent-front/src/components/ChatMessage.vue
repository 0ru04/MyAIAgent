<script setup>
import { computed } from 'vue'
import { Bot, User } from 'lucide-vue-next'

const props = defineProps({
  message: { type: Object, required: true },
  assistantName: { type: String, default: 'AI 助手' },
  assistantIcon: { type: [Object, Function], default: null },
})

const isUser = computed(() => props.message.role === 'user')
const isStreaming = computed(() => props.message.status === 'streaming')

const emptyText = computed(() => {
  if (props.message.status === 'error') return '回复失败'
  if (props.message.status === 'stopped') return '已停止生成'
  return '暂无内容'
})
</script>

<template>
  <div class="msg" :class="isUser ? 'msg--user' : 'msg--ai'">
    <div v-if="!isUser" class="msg__avatar msg__avatar--ai">
      <component :is="assistantIcon || Bot" :size="18" />
    </div>

    <div class="msg__body">
      <div class="msg__name">{{ isUser ? '我' : assistantName }}</div>
      <div class="msg__bubble" :class="`is-${message.status}`">
        <template v-if="message.content">
          <span class="msg__text">{{ message.content }}</span>
          <span v-if="isStreaming" class="caret" aria-hidden="true" />
        </template>
        <span v-else-if="isStreaming" class="typing" aria-label="正在生成">
          <i /><i /><i />
        </span>
        <span v-else class="msg__empty">{{ emptyText }}</span>
      </div>
    </div>

    <div v-if="isUser" class="msg__avatar msg__avatar--user">
      <User :size="18" />
    </div>
  </div>
</template>

<style scoped>
.msg {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.msg--user {
  justify-content: flex-end;
}

.msg__avatar {
  flex: none;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: grid;
  place-items: center;
}

.msg__avatar--ai {
  background: var(--accent-soft);
  color: var(--accent);
}

.msg__avatar--user {
  background: #e5e7eb;
  color: #4b5563;
}

.msg__body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: min(76%, 640px);
}

.msg--user .msg__body {
  align-items: flex-end;
}

.msg__name {
  padding: 0 4px;
  font-size: 12px;
  color: var(--text-muted);
}

.msg__bubble {
  padding: 10px 14px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--surface);
  box-shadow: var(--shadow-sm);
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.msg--user .msg__bubble {
  background: var(--accent);
  border-color: var(--accent);
  color: #fff;
}

.msg__bubble.is-error {
  background: #fef2f2;
  border-color: #fecaca;
  color: #b91c1c;
}

.msg__empty {
  color: var(--text-soft);
}

.msg--user .msg__empty {
  color: rgba(255, 255, 255, 0.85);
}

.caret {
  display: inline-block;
  width: 2px;
  height: 1em;
  margin-left: 2px;
  vertical-align: -2px;
  background: currentColor;
  animation: caret-blink 1s steps(2, start) infinite;
}

.typing {
  display: inline-flex;
  gap: 4px;
  padding: 3px 0;
}

.typing i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--text-soft);
  animation: typing 1.2s infinite ease-in-out;
}

.typing i:nth-child(2) {
  animation-delay: 0.15s;
}

.typing i:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes caret-blink {
  to {
    visibility: hidden;
  }
}

@keyframes typing {
  0%,
  60%,
  100% {
    transform: translateY(0);
    opacity: 0.45;
  }
  30% {
    transform: translateY(-3px);
    opacity: 1;
  }
}

@media (max-width: 640px) {
  .msg__body {
    max-width: 82%;
  }
}
</style>
