<script setup>
import { ArrowUpRight, Bot, Heart, Sparkles } from 'lucide-vue-next'

const apps = [
  {
    name: 'AI 恋爱大师',
    description: '情感答疑与关系建议，支持多轮连续对话',
    to: '/love',
    icon: Heart,
    accent: '#e11d48',
    soft: '#fff1f2',
  },
  {
    name: 'AI 超级智能体',
    description: '自动规划并调用工具，完成复杂任务',
    to: '/manus',
    icon: Bot,
    accent: '#0d9488',
    soft: '#ecfdf5',
  },
]
</script>

<template>
  <div class="home">
    <div class="home__inner">
      <div class="home__brand">
        <span class="home__logo"><Sparkles :size="18" /></span>
        <span class="home__brand-name">AI 应用中心</span>
      </div>

      <div class="apps">
        <router-link
          v-for="app in apps"
          :key="app.to"
          class="app-card"
          :style="{ '--card-accent': app.accent, '--card-soft': app.soft }"
          :to="app.to"
        >
          <span class="app-card__icon">
            <component :is="app.icon" :size="22" />
          </span>
          <span class="app-card__text">
            <span class="app-card__name">{{ app.name }}</span>
            <span class="app-card__desc">{{ app.description }}</span>
          </span>
          <ArrowUpRight class="app-card__arrow" :size="18" />
        </router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home {
  min-height: 100dvh;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: max(48px, 8vh) 20px 48px;
}

.home__inner {
  width: 100%;
  max-width: 880px;
}

.home__brand {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 28px;
}

.home__logo {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: 8px;
  background: #1f2430;
  color: #fff;
}

.home__brand-name {
  font-size: 16px;
  font-weight: 600;
}

.apps {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.app-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--surface);
  box-shadow: var(--shadow-sm);
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.app-card:hover {
  border-color: var(--card-accent);
  box-shadow: var(--shadow);
}

.app-card__icon {
  flex: none;
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  border-radius: 8px;
  background: var(--card-soft);
  color: var(--card-accent);
}

.app-card__text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.app-card__name {
  font-size: 15px;
  font-weight: 600;
}

.app-card__desc {
  font-size: 13px;
  color: var(--text-muted);
}

.app-card__arrow {
  flex: none;
  color: var(--text-soft);
  transition: color 0.15s ease, transform 0.15s ease;
}

.app-card:hover .app-card__arrow {
  color: var(--card-accent);
  transform: translate(1px, -1px);
}

@media (max-width: 680px) {
  .apps {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
