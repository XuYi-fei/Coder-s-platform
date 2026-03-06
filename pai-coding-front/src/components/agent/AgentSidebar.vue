<script setup lang="ts">
import { onMounted } from 'vue'
import { useAgentStore } from '@/stores/agentStore'
import { ElMessage, ElMessageBox } from 'element-plus'

const agentStore = useAgentStore()

onMounted(async () => {
  await agentStore.loadAgents()
  if (agentStore.selectedAgentId) {
    await agentStore.loadSessions(agentStore.selectedAgentId)
  }
})

function onSelectAgent(agentId: number) {
  agentStore.selectAgent(agentId)
}

function newSession() {
  agentStore.clearSteps()
}

function formatTime(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const now = new Date()
  const diff = (now.getTime() - d.getTime()) / 1000
  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)} 分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)} 小时前`
  return d.toLocaleDateString()
}

function statusType(status: string) {
  const map: Record<string, string> = {
    COMPLETED: 'success',
    EXECUTING: 'primary',
    FAILED: 'danger',
    PLANNING: 'warning',
  }
  return map[status] ?? 'info'
}

async function onSelectSession(sessionId: number) {
  agentStore.currentSessionId = sessionId
  await agentStore.loadSessionDetail(sessionId)
}
</script>

<template>
  <div class="w-64 border-r border-gray-100 bg-white flex flex-col h-full">
    <!-- Agent selector -->
    <div class="p-3 border-b border-gray-100">
      <div class="text-xs text-gray-500 mb-2 font-medium uppercase tracking-wide">Agent</div>
      <el-select
        :model-value="agentStore.selectedAgentId"
        @update:model-value="onSelectAgent"
        placeholder="选择 Agent"
        size="small"
        class="w-full"
      >
        <el-option
          v-for="agent in agentStore.agents"
          :key="agent.agentId"
          :label="agent.name"
          :value="agent.agentId"
          :disabled="agent.status !== 2"
        >
          <span>{{ agent.name }}</span>
          <span v-if="agent.status !== 2" class="text-xs text-gray-400 ml-1">(未激活)</span>
        </el-option>
      </el-select>
    </div>

    <!-- New session button -->
    <div class="p-3 border-b border-gray-100">
      <el-button size="small" class="w-full" @click="newSession" :disabled="!agentStore.selectedAgentId">
        + 新对话
      </el-button>
    </div>

    <!-- Session history -->
    <div class="flex-1 overflow-y-auto">
      <div v-if="agentStore.sessions.length === 0" class="p-4 text-center text-gray-400 text-sm">
        暂无历史记录
      </div>
      <div
        v-for="session in agentStore.sessions"
        :key="session.sessionId"
        :class="[
          'p-3 border-b border-gray-50 cursor-pointer hover:bg-gray-50 transition-colors',
          agentStore.currentSessionId === session.sessionId ? 'bg-blue-50' : ''
        ]"
        @click="onSelectSession(session.sessionId)"
      >
        <div class="flex items-center justify-between mb-1">
          <el-tag :type="statusType(session.status)" size="small" class="text-xs">
            {{ session.status }}
          </el-tag>
          <span class="text-xs text-gray-400">{{ formatTime(session.startedAt) }}</span>
        </div>
        <div class="text-sm text-gray-700 truncate">
          {{ session.userInput }}
        </div>
        <div v-if="session.finalOutput" class="text-xs text-gray-400 truncate mt-0.5">
          {{ session.finalOutput }}
        </div>
      </div>
    </div>
  </div>
</template>
