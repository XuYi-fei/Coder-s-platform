<script setup lang="ts">
import { useAgentStore } from '@/stores/agentStore'

const agentStore = useAgentStore()

const modeLabel: Record<string, string> = {
  REACT: 'ReAct',
  CHAIN: 'Chain',
  ROUTE: 'Route',
  PLANNER_CRITIC: 'Planner-Critic',
  SUPERVISOR_WORKER: 'Supervisor',
}

const modeColor: Record<string, string> = {
  REACT: 'primary',
  CHAIN: 'success',
  ROUTE: 'warning',
  PLANNER_CRITIC: 'danger',
  SUPERVISOR_WORKER: 'info',
}
</script>

<template>
  <div class="flex items-center px-4 py-3 border-b border-gray-100 bg-white">
    <div class="flex items-center gap-3 flex-1">
      <div v-if="agentStore.selectedAgent?.avatarUrl"
           class="w-8 h-8 rounded-full overflow-hidden">
        <img :src="agentStore.selectedAgent.avatarUrl" alt="agent" class="w-full h-full object-cover" />
      </div>
      <div v-else class="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-semibold text-sm">
        {{ agentStore.selectedAgent?.name?.[0] ?? 'A' }}
      </div>
      <div>
        <div class="font-medium text-gray-800 text-sm">
          {{ agentStore.selectedAgent?.name ?? '选择 Agent' }}
        </div>
        <div v-if="agentStore.selectedAgent?.description"
             class="text-xs text-gray-400 truncate max-w-xs">
          {{ agentStore.selectedAgent.description }}
        </div>
      </div>
      <el-tag
        v-if="agentStore.selectedAgent?.executionMode"
        :type="modeColor[agentStore.selectedAgent.executionMode] as any"
        size="small" class="ml-1"
      >
        {{ modeLabel[agentStore.selectedAgent.executionMode] ?? agentStore.selectedAgent.executionMode }}
      </el-tag>
    </div>
    <div v-if="agentStore.isExecuting" class="flex items-center gap-2 text-blue-500 text-sm">
      <span class="animate-pulse">●</span>
      <span>执行中...</span>
    </div>
  </div>
</template>
