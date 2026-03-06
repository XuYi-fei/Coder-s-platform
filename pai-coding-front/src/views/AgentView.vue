<script setup lang="ts">
import { onMounted } from 'vue'
import { useAgentStore } from '@/stores/agentStore'
import AgentSidebar from '@/components/agent/AgentSidebar.vue'
import AgentHeader from '@/components/agent/AgentHeader.vue'
import StepViewer from '@/components/agent/StepViewer.vue'
import AgentInput from '@/components/agent/AgentInput.vue'

const agentStore = useAgentStore()

onMounted(async () => {
  await agentStore.loadAgents()
})
</script>

<template>
  <div class="flex h-screen bg-gray-50 overflow-hidden">
    <!-- Left sidebar: agent selector + session history -->
    <AgentSidebar />

    <!-- Main content area -->
    <div class="flex-1 flex flex-col min-w-0">
      <!-- Top bar: agent name + mode badge -->
      <AgentHeader />

      <!-- Step viewer: shows ReAct thought/action/observation/answer steps -->
      <StepViewer />

      <!-- Input bar at bottom -->
      <AgentInput />
    </div>
  </div>
</template>
