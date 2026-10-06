<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { Send, Square } from 'lucide-vue-next'

const props = defineProps({
  streaming: { type: Boolean, default: false },
  placeholder: { type: String, default: '说点什么…' },
})

const emit = defineEmits(['send', 'stop'])

const text = ref('')
const textarea = ref(null)

const canSend = computed(() => !props.streaming && text.value.trim().length > 0)

const resize = () => {
  const el = textarea.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = `${Math.min(el.scrollHeight, 168)}px`
  el.style.overflowY = el.scrollHeight > 168 ? 'auto' : 'hidden'
}

watch(text, () => nextTick(resize))

const submit = () => {
  if (!canSend.value) return
  emit('send', text.value.trim())
  text.value = ''
  nextTick(resize)
}

const onKeydown = (event) => {
  if (event.key !== 'Enter' || event.shiftKey || event.isComposing) return
  event.preventDefault()
  submit()
}
</script>

<template>
  <form class="composer" @submit.prevent="submit">
    <textarea
      ref="textarea"
      v-model="text"
      class="composer__input"
      rows="1"
      :placeholder="placeholder"
      @keydown="onKeydown"
    />

    <button
      v-if="streaming"
      type="button"
      class="composer__btn composer__btn--stop"
      title="停止生成"
      @click="emit('stop')"
    >
      <Square :size="15" />
    </button>
    <button v-else type="submit" class="composer__btn" :disabled="!canSend" title="发送">
      <Send :size="17" />
    </button>
  </form>
</template>

<style scoped>
.composer {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  width: 100%;
  max-width: 880px;
  margin: 0 auto;
  padding: 8px 8px 8px 14px;
  background: var(--surface);
  border: 1px solid var(--border-strong);
  border-radius: 8px;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.composer:focus-within {
  border-color: var(--accent);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--accent) 16%, transparent);
}

.composer__input {
  flex: 1;
  min-width: 0;
  min-height: 24px;
  max-height: 168px;
  padding: 6px 0;
  border: 0;
  outline: none;
  resize: none;
  background: transparent;
  color: var(--text);
  font: inherit;
  line-height: 1.5;
}

.composer__input::placeholder {
  color: var(--text-soft);
}

.composer__btn {
  flex: none;
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border: 0;
  border-radius: 6px;
  background: var(--accent);
  color: #fff;
  cursor: pointer;
  transition: opacity 0.15s ease, background 0.15s ease;
}

.composer__btn:hover:not(:disabled) {
  opacity: 0.9;
}

.composer__btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.composer__btn--stop {
  background: #1f2430;
}
</style>
