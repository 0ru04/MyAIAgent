import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { title: 'AI 应用中心' },
    },
    {
      path: '/love',
      name: 'love',
      component: () => import('@/views/LoveAppView.vue'),
      meta: { title: 'AI 恋爱大师' },
    },
    {
      path: '/manus',
      name: 'manus',
      component: () => import('@/views/ManusAppView.vue'),
      meta: { title: 'AI 超级智能体' },
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/',
    },
  ],
  scrollBehavior: () => ({ top: 0 }),
})

router.afterEach((to) => {
  const title = to.meta?.title
  document.title = title ? `${title} · AI 应用中心` : 'AI 应用中心'
})

export default router
