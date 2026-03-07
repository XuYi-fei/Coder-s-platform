import { createRouter, createWebHistory } from 'vue-router'
import { getGlobalStore } from '@/stores/global'
import { doGet } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { GLOBAL_INFO_URL } from '@/http/URL'
import { messageTip } from '@/util/utils'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomeView.vue')
    },
    // AI 聊天页 (Chat V2)
    {
      path: '/chat',
      name: 'chat',
      component: () => import('@/views/ChatViewV2.vue'),
      meta: {
        loginRequired: true
      }
    },
    // 用户主页
    {
      path: '/user/:userId',
      name: 'userHome',
      component: () => import('@/views/UserHomeView.vue'),
      meta: {
        loginRequired: true
      }
    },
    // Agent Platform
    {
      path: '/agent',
      name: 'agent',
      component: () => import('@/views/AgentView.vue'),
      meta: {
        loginRequired: true
      }
    },
    // Admin Management
    {
      path: '/admin',
      component: () => import('@/views/admin/AdminLayout.vue'),
      meta: { loginRequired: true },
      redirect: '/admin/agents',
      children: [
        { path: 'agents', component: () => import('@/views/admin/AgentManagePage.vue') },
        { path: 'tools', component: () => import('@/views/admin/ToolManagePage.vue') },
        { path: 'models', component: () => import('@/views/admin/ModelManagePage.vue') },
        { path: 'skills', component: () => import('@/views/admin/SkillManagePage.vue') },
      ]
    }
  ]
})

router.beforeEach(async (to, from, next) => {
  if (to.meta.loginRequired) {
    try {
      const globalStore = await getGlobalStore()
      await checkLoginStatus(globalStore)
      if (!globalStore.global.isLogin) {
        messageTip('请先登录', 'warning')
        next('/')
        return
      }
    } catch {
      messageTip('请先登录', 'warning')
      next('/')
      return
    }
  }
  next()
})

async function checkLoginStatus(globalStore: any) {
  await doGet<CommonResponse>(GLOBAL_INFO_URL, {}).then((res) => {
    globalStore.setGlobal(res.data.global)
  })
}

export default router
