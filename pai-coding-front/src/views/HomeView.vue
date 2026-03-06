<template>
  <HeaderBar />
  <div class="min-h-screen bg-gray-50">
    <div class="max-w-4xl mx-auto px-4 py-16">
      <!-- Hero -->
      <div class="text-center mb-16">
        <h1 class="text-4xl font-bold text-gray-800 mb-4">技术派平台</h1>
        <p class="text-lg text-gray-500">基于 Spring AI 的 AI 能力集成平台</p>
      </div>

      <!-- Feature cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- Chat V2 -->
        <router-link to="/chat" class="block group">
          <div class="bg-white rounded-2xl border border-gray-100 p-8 hover:shadow-lg transition-all hover:border-blue-200">
            <div class="text-4xl mb-4">💬</div>
            <h2 class="text-xl font-semibold text-gray-800 mb-2 group-hover:text-blue-600 transition-colors">AI 对话</h2>
            <p class="text-gray-500 text-sm leading-relaxed">
              基于 Spring AI 的多模型对话，支持通义千问等大语言模型，带会话记忆与 Token 配额管理。
            </p>
            <div class="mt-4 text-blue-500 text-sm font-medium group-hover:translate-x-1 transition-transform inline-block">
              开始对话 →
            </div>
          </div>
        </router-link>

        <!-- Agent Platform -->
        <router-link to="/agent" class="block group">
          <div class="bg-white rounded-2xl border border-gray-100 p-8 hover:shadow-lg transition-all hover:border-purple-200">
            <div class="text-4xl mb-4">🤖</div>
            <h2 class="text-xl font-semibold text-gray-800 mb-2 group-hover:text-purple-600 transition-colors">Agent 平台</h2>
            <p class="text-gray-500 text-sm leading-relaxed">
              基于 ReAct 架构的智能 Agent，支持可插拔工具调用、多模型供应商配置与执行步骤可视化。
            </p>
            <div class="mt-4 text-purple-500 text-sm font-medium group-hover:translate-x-1 transition-transform inline-block">
              进入平台 →
            </div>
          </div>
        </router-link>
      </div>

      <!-- Tech stack -->
      <div class="mt-12 text-center text-xs text-gray-400 space-x-3">
        <span>Spring Boot 3.5</span>
        <span>·</span>
        <span>Spring AI 1.1</span>
        <span>·</span>
        <span>Vue 3</span>
        <span>·</span>
        <span>ReAct Agent</span>
      </div>
    </div>
  </div>
  <Footer />
  <LoginDialog :clicked="loginDialogClicked" />
</template>

<script setup lang="ts">
import { provide, ref } from 'vue'
import HeaderBar from '@/components/layout/HeaderBar.vue'
import Footer from '@/components/layout/Footer.vue'
import LoginDialog from '@/components/dialog/LoginDialog.vue'

const loginDialogClicked = ref(false)

const changeClicked = () => {
  loginDialogClicked.value = !loginDialogClicked.value
}

provide('loginDialogClicked', changeClicked)
</script>
