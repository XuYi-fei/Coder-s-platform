<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { useAgentStore, type StepEvent } from '@/stores/agentStore'
import { marked } from 'marked'

const agentStore = useAgentStore()
const scrollRef = ref<HTMLElement | null>(null)

// Auto-scroll when new steps arrive
watch(() => agentStore.steps.length, async () => {
  await nextTick()
  if (scrollRef.value) {
    scrollRef.value.scrollTop = scrollRef.value.scrollHeight
  }
})

function renderMarkdown(text: string): string {
  if (!text) return ''
  try {
    return marked.parse(text) as string
  } catch {
    return text
  }
}

const stepConfig: Record<string, { icon: string; label: string; bg: string; border: string; textClass: string }> = {
  THOUGHT: {
    icon: '🧠',
    label: '思考',
    bg: 'bg-gray-50',
    border: 'border-gray-200',
    textClass: 'text-gray-600 italic',
  },
  ACTION: {
    icon: '🔧',
    label: '调用工具',
    bg: 'bg-blue-50',
    border: 'border-blue-200',
    textClass: 'text-blue-800',
  },
  OBSERVATION: {
    icon: '👁️',
    label: '观察结果',
    bg: 'bg-green-50',
    border: 'border-green-200',
    textClass: 'text-green-800',
  },
  ANSWER: {
    icon: '✅',
    label: '最终回答',
    bg: 'bg-amber-50',
    border: 'border-amber-200',
    textClass: 'text-gray-800',
  },
  ERROR: {
    icon: '❌',
    label: '错误',
    bg: 'bg-red-50',
    border: 'border-red-200',
    textClass: 'text-red-700',
  },
}

const collapsed = ref<Record<number, boolean>>({})

function toggleCollapse(index: number) {
  collapsed.value[index] = !collapsed.value[index]
}

function isCollapsible(step: StepEvent): boolean {
  return step.type === 'THOUGHT' || step.type === 'OBSERVATION'
}
</script>

<template>
  <div ref="scrollRef" class="flex-1 overflow-y-auto p-4 space-y-3">
    <!-- Empty state -->
    <div v-if="agentStore.steps.length === 0 && !agentStore.isExecuting"
         class="flex flex-col items-center justify-center h-full text-gray-400">
      <div class="text-5xl mb-4">🤖</div>
      <div class="text-lg font-medium mb-2">Agent 就绪</div>
      <div class="text-sm text-center max-w-sm">
        选择一个 Agent 并在下方输入消息，Agent 将一步步推理并调用工具来完成任务。
      </div>
    </div>

    <!-- Execution steps -->
    <transition-group name="step-list">
      <div
        v-for="(step, index) in agentStore.steps"
        :key="`${step.type}-${step.stepOrder}-${index}`"
        :class="[
          'rounded-lg border p-3 transition-all',
          stepConfig[step.type]?.bg ?? 'bg-gray-50',
          stepConfig[step.type]?.border ?? 'border-gray-200'
        ]"
      >
        <!-- Step header -->
        <div
          class="flex items-center gap-2 mb-1"
          :class="isCollapsible(step) ? 'cursor-pointer' : ''"
          @click="isCollapsible(step) && toggleCollapse(index)"
        >
          <span class="text-base">{{ stepConfig[step.type]?.icon ?? '•' }}</span>
          <span class="font-semibold text-xs uppercase tracking-wide text-gray-500">
            {{ stepConfig[step.type]?.label ?? step.type }}
          </span>
          <span v-if="step.stepOrder !== undefined" class="text-xs text-gray-400">#{{ step.stepOrder }}</span>
          <el-tag v-if="step.toolName" size="small" type="primary" class="ml-1">{{ step.toolName }}</el-tag>
          <span v-if="isCollapsible(step)" class="ml-auto text-gray-400 text-xs">
            {{ collapsed[index] ? '展开 ▼' : '折叠 ▲' }}
          </span>
        </div>

        <!-- Step content -->
        <div v-show="!collapsed[index]" :class="['text-sm', stepConfig[step.type]?.textClass ?? 'text-gray-700']">
          <!-- ANSWER: render as markdown -->
          <div v-if="step.type === 'ANSWER'" class="prose prose-sm max-w-none"
               v-html="renderMarkdown(step.content || '')"></div>
          <!-- Other types: pre-formatted text -->
          <div v-else-if="step.content" class="whitespace-pre-wrap break-words">{{ step.content }}</div>
        </div>
      </div>
    </transition-group>

    <!-- Executing indicator -->
    <div v-if="agentStore.isExecuting" class="flex items-center gap-2 text-blue-500 text-sm p-2">
      <span class="flex gap-1">
        <span class="w-2 h-2 bg-blue-400 rounded-full animate-bounce" style="animation-delay:0ms"></span>
        <span class="w-2 h-2 bg-blue-400 rounded-full animate-bounce" style="animation-delay:150ms"></span>
        <span class="w-2 h-2 bg-blue-400 rounded-full animate-bounce" style="animation-delay:300ms"></span>
      </span>
      <span>Agent 正在思考...</span>
    </div>
  </div>
</template>

<style scoped>
.step-list-enter-active {
  transition: all 0.3s ease;
}
.step-list-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
</style>
