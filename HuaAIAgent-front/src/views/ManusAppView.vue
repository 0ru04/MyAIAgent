<script setup>
import { Bot } from 'lucide-vue-next'
import ChatRoom from '@/components/ChatRoom.vue'
import { AI_ENDPOINTS } from '@/api/ai'
</script>

<template>
  <ChatRoom
    title="AI 超级智能体"
    subtitle="自动规划、调用工具完成任务"
    :endpoint="AI_ENDPOINTS.manusChat"
    accent="#0d9488"
    accent-soft="#ecfdf5"
    assistant-name="超级智能体"
    :assistant-icon="Bot"
    empty-text="把任务交给智能体试试"
    placeholder="描述你的任务…"
  />
</template>
