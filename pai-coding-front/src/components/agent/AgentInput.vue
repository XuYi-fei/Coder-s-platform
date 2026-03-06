<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useAgentStore } from '@/stores/agentStore'
import { sendAgentMessage } from '@/http/BackendRequests'

const agentStore = useAgentStore()
const inputText = ref('')
const textareaRef = ref<HTMLTextAreaElement | null>(null)

function autoResize() {
  const el = textareaRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 160) + 'px'
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    send()
  }
}

async function send() {
  const message = inputText.value.trim()
  if (!message) return
  if (!agentStore.selectedAgentId) {
    ElMessage.warning('请先选择一个 Agent')
    return
  }
  if (agentStore.isExecuting) {
    ElMessage.warning('Agent 正在执行中，请等待...')
    return
  }

  inputText.value = ''
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto'
  }

  agentStore.clearSteps()
  agentStore.setExecuting(true)

  // Add placeholder session to sidebar
  agentStore.prependSession({
    sessionId: -Date.now(),
    agentId: agentStore.selectedAgentId,
    userInput: message,
    status: 'EXECUTING',
    startedAt: new Date().toISOString(),
  })

  await sendAgentMessage(
    agentStore.selectedAgentId,
    message,
    (event) => {
      agentStore.addStep(event)
      if (event.done && event.sessionId) {
        agentStore.currentSessionId = event.sessionId
      }
    },
    () => {
      agentStore.setExecuting(false)
      // Refresh sessions
      if (agentStore.selectedAgentId) {
        agentStore.loadSessions(agentStore.selectedAgentId)
      }
    },
    (err) => {
      agentStore.setExecuting(false)
      ElMessage.error('执行失败: ' + err.message)
    }
  )
}
</script>

<template>
  <div class="border-t border-gray-100 bg-white p-3">
    <div class="flex gap-2 items-end">
      <div class="flex-1 relative">
        <textarea
          ref="textareaRef"
          v-model="inputText"
          @input="autoResize"
          @keydown="onKeydown"
          :disabled="agentStore.isExecuting || !agentStore.selectedAgentId"
          placeholder="输入消息（Enter 发送，Shift+Enter 换行）..."
          rows="2"
          class="w-full resize-none rounded-lg border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-300 focus:border-transparent transition-all disabled:bg-gray-50 disabled:text-gray-400"
          style="min-height: 60px; max-height: 160px;"
        />
      </div>
      <el-button
        type="primary"
        :disabled="!inputText.trim() || agentStore.isExecuting || !agentStore.selectedAgentId"
        :loading="agentStore.isExecuting"
        @click="send"
        class="h-10"
      >
        发送
      </el-button>
    </div>
    <div class="flex items-center justify-between mt-1.5 px-1">
      <div class="text-xs text-gray-400">
        <span v-if="agentStore.selectedAgent">
          使用 <span class="font-medium">{{ agentStore.selectedAgent.name }}</span>
          · {{ agentStore.selectedAgent.tools?.length ?? 0 }} 个工具
        </span>
        <span v-else>请先在左侧选择 Agent</span>
      </div>
      <button
        v-if="agentStore.steps.length > 0"
        class="text-xs text-gray-400 hover:text-gray-600 transition-colors"
        @click="agentStore.clearSteps()"
      >
        清空
      </button>
    </div>
  </div>
</template>
